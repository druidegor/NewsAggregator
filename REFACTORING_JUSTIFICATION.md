# Что было сделано в feature-refactoring и почему это лучше, чем main

### 1. Room переведён на «настоящий» KMP
**В main:**
- `NewsDatabase` — обычная абстрактная Room-сущность без аннотаций для KMP.
- БД создавалась только в Android-модуле через `Room.databaseBuilder(context, ...)`, потому что для этого нужен `androidContext()`. Для iOS и JVM ничего нет — приложение запускалось только на Android.
- В `build.gradle.kts` стоял один `ksp(libs.room.compiler)` — это работает только для Android, потому что KMP-проекту нужен KSP-процессор отдельно для каждой платформы.

**В feature-refactoring:**
- На `NewsDatabase` добавлены `@ConstructedBy(NewsDatabaseConstructor::class)` и `expect object NewsDatabaseConstructor : RoomDatabaseConstructor<NewsDatabase>`. Это официальный механизм Room 2.7 для KMP — компилятор Room сам генерирует actual реализации под каждую цель, нам остаётся только объявить expect.
- В `build.gradle.kts` процессор подключён для каждой цели отдельно: `kspAndroid`, `kspIosArm64`, `kspIosSimulatorArm64`, `kspJvm`. Без этого Room не сгенерирует код для iOS и JVM, и приложение просто не скомпилируется на этих целях.
- В `commonMain/di/DatabaseModule.kt` сборка БД вынесена в общий код:
```kotlin
single {
    get<RoomDatabase.Builder<NewsDatabase>>()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
```
Подключён `BundledSQLiteDriver` — кроссплатформенный SQLite-драйвер (без него на iOS/JVM не было бы движка SQLite вообще), и явно задан `Dispatchers.IO` для запросов (на Native Room без явного контекста просто падает).

**Почему лучше:** теперь Room действительно мультиплатформенный. Общий код знает только про `RoomDatabase.Builder<NewsDatabase>`, а каждая платформа отдаёт свою реализацию билдера. В main весь слой данных был привязан к Android.

### 2. DI: одинаковый паттерн для всех платформ
**В main:**
- `initKoin()` принимал только `KoinAppDeclaration` — то есть платформа могла что-то донастроить, но не могла добавить собственные Koin-модули в общий граф.
- В `NewsApp.kt` (Android) модуль `androidDatabaseModule` добавлялся прямо внутри блока `initKoin { modules(androidDatabaseModule) ... }` — это работало, но было «вшито» в Android.
- iOS и JVM вообще не инициализировали Koin (`MainViewController` и `jvmMain/main.kt` были голыми) — при первом обращении к зависимостям приложение бы упало.

**В feature-refactoring:**
- Сигнатура расширена:
```kotlin
fun initKoin(
    appDeclaration: KoinAppDeclaration = {},
    additionalModules: List<Module> = emptyList()
)
```
Появился унифицированный способ передать платформо-специфические модули в общий запуск.
- Android, iOS и JVM теперь подключают свои `*DatabaseModule` через `additionalModules = listOf(...)`. Это симметрично и одинаково читается на всех трёх платформах.
- Добавлены `IosDatabaseModule.kt` и `JvmDatabaseModule.kt`, которые тоже регистрируют `RoomDatabase.Builder<NewsDatabase>` — каждая платформа определяет, где будет лежать файл БД (`NSDocumentDirectory` на iOS, `~/.newsagg/news.db` на JVM).
- iOS-точка входа теперь зовёт `initKoin(...)` внутри `ComposeUIViewController(configure = {...})`, JVM — перед `application {}`.

**Почему лучше:** Koin работает на всех целях, контракт «каждая платформа поставляет свой builder» одинаков, нет специальной логики для Android в общем коде.

### 3. Обработка ошибок: DataResult вместо «упало — поймали ничего»
**В main:**
- `GetTopHeadlinesUseCase.invoke()` возвращал `List<Article>` и бросал исключение наружу.
- В `NewsFeedViewModel` стоял `try/catch (e: Exception) { }` — тело пустое. То есть при ошибке сети loading не сбрасывался, состояние оставалось в `isLoading = true`, и UI висел в этом состоянии вечно.
- Внутри `init` были `println("VIEWMODEL: ...")` — отладочные принты, оставшиеся от разработки.
- В UI: `if (!state.isLoading) { Text(state.articles.first().title) }` — `articles.first()` падает с `NoSuchElementException`, если список пустой (а он пустой при ошибке).

**В feature-refactoring:**
- Появился `DataResult<T>`:
```kotlin
sealed class DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>()
    data class Error(val throwable: Throwable) : DataResult<Nothing>()
}
```
- Репозиторий (`NewsRepositoryImpl.getTopHeadlines`) сам ловит ошибки и возвращает `DataResult`. Важно: ловится `Exception`, но `CancellationException` пробрасывается дальше — иначе отмена корутины ошибочно превращается в «бизнес-ошибку», а ViewModel перестаёт корректно реагировать на отмену скоупа.
- `Result.Error` хранит `Throwable`, а не `Exception` — на iOS Native и в общем коде это безопаснее (Kotlin/Native может приносить разные подклассы).
- ViewModel явно обрабатывает обе ветки `when (result) { Success -> ...; Error -> ... }` и пишет в стейт `error: String?`, выключая `isLoading`.
- Принты убраны.

**Почему лучше:** ошибки теперь часть бизнес-контракта, а не побочный эффект. ViewModel не может «забыть» обработать сбой — компилятор заставит обработать обе ветки sealed class. UI больше не виснет в loading навсегда.

### 4. UI: полноценная state-machine вместо одного Text
**В main:**
```kotlin
if (!state.isLoading) {
    Box(...) { Text(state.articles.first().title) }
}
```
- Показывался только один заголовок (первый!), причём с гарантированным крашем при пустом списке.
- Не было индикатора загрузки, ошибки, состояния «нет данных».

**В feature-refactoring:**
- Все четыре состояния отрисованы явно:
    - `isLoading` → `CircularProgressIndicator`
    - `error != null` → текст ошибки
    - `articles.isEmpty()` → «No articles found»
    - иначе → `LazyColumn` со всеми статьями
- В `LazyColumn` указан `key = { it.url }` — Compose правильно диффит список при обновлениях, не пересоздаёт композиции лишний раз.

**Почему лучше:** UI отражает реальные состояния доменной модели, плюс показывает весь список, а не только первый элемент. И не падает.

### 5. ViewModel и StateFlow
**В main:**
- ViewModel пересоздавал стейт целиком: `_state.value = NewsFeedState(isLoading = true)` — терялись все остальные поля.
- `viewModelScope.launch` стоял прямо в `init` без отдельной функции — нельзя было перезапустить загрузку (например, для «Retry»).
- В `App.kt` лежали кучи неиспользуемых импортов (`Image`, `AnimatedVisibility`, `GlobalScope`, `Ktor Logging`, ресурсы Compose…) — мусор от первой итерации.

**В feature-refactoring:**
- `_state.update { it.copy(...) }` — атомарное обновление, остальные поля стейта сохраняются. Это рекомендованный Kotlin-way работы с `MutableStateFlow`.
- Тип `state: StateFlow<NewsFeedState>` объявлен явно — лучше для API.
- Логика загрузки вынесена в публичный `fun loadNews()`, который дёргается из `init` и потенциально может быть вызван из UI (например, кнопкой Retry).
- `App.kt` почищен: только нужные импорты, инжект ViewModel через `koinViewModel()` (это Android-aware биндинг, который привязывает VM к `ViewModelStoreOwner` — у обычного `koinInject()` этого нет, поэтому конфигурационные изменения на Android убивали бы стейт).

**Почему лучше:** код стал короче, читабельнее, и не теряет состояние при обновлениях. Меньше копипасты, и UI-инфраструктура корректнее интегрирована.

### 6. Бонусы
- В `NewsRepositoryImpl` бывший `private suspend fun loadArticles` был «полу-приватным» и не имел отношения к интерфейсу. Теперь это публичный метод интерфейса (`suspend fun loadArticles(topic: String): List<Article>`) — он по-настоящему вызывается из `addSubscription` и сохраняет статьи в БД через `newsDao.addArticles(...)`. То есть подписка снова реально подгружает статьи (в main это уже было, но было плохо изолировано).
- Конструкторы `SubscriptionDbModel(topic)` теперь используются напрямую, без промежуточного `toDbModel()` — меньше уровней косвенности.

### Итог
В main проект собирался только под Android, и даже там UI не справлялся с ошибками и пустыми списками. В feature-refactoring:
1. Room заработал на трёх платформах — это основная цель ветки.
2. DI унифицирован — каждая платформа предоставляет свой builder, общий код не знает об Android-специфике.
3. Ошибки стали частью типа — через `DataResult`, с правильным пробросом `CancellationException`.
4. UI обрабатывает все состояния и больше не падает на пустом списке.
5. Состояние ViewModel обновляется идиоматично через `update { copy(...) }`.
6. Код почищен от отладочного мусора (принты, неиспользуемые импорты).

Это не косметика — это переход с «работает на одной платформе случайно» на «реально мультиплатформенный код с предсказуемым контрактом», что и есть смысл KMP.
