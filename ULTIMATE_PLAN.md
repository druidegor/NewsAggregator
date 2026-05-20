# Ultimate Plan: Tech News Aggregator — Production-Grade KMP

> Цель этого плана — провести проект от текущего состояния (рабочий фундамент с одним экраном) до состояния, описанного в исходной задумке (полноценное приложение с навигацией, кэшированием, пагинацией, закладками, шарингом и тремя платформами в CI), **избегая повторной переработки.** Каждый этап описывает: что делаем, **почему именно так**, какие ошибки предотвращаем, что считается «готово».
>
> Принцип плана: **never paint yourself into a corner.** Любое архитектурное решение делается один раз и не переделывается. Если есть выбор между «быстрое и придётся переписать» и «на час дольше, но навсегда» — берём второе.

---

## Часть 0. Что уже сделано и что это значит

**Готово (фундамент):** Clean Architecture слои, Room KMP с `expect/actual` builder, Koin со схемой `initKoin(additionalModules)`, `DataResult<T>` с обработкой `CancellationException`, ViewModel со `StateFlow` + state machine в UI, BuildKonfig для API-ключа, CI собирает Android+Desktop, iOS-компиляция на PR.

**Чего нет (что предстоит):** тестов, навигации, экрана деталей, пагинации, pull-to-refresh, экрана подписок, закладок, оффлайн-кэша как Single Source of Truth, шаринга через `expect/actual`, мультиплатформенного логгера, Desktop-адаптивного UI, релизных конфигов.

**Что нужно исправить как технический долг перед новыми фичами:**
- `pageSize: Int = 1` по умолчанию в `composeApp/src/commonMain/kotlin/org/newsagg/project/data/network/api/NewsApi.kt` — баг, должно быть ~20.
- `Article.publishedAt: Long` — заменить на `kotlinx.datetime.Instant`.
- `loadArticles()` swallows errors silently — должен возвращать `DataResult<Unit>`.

Эти три исправления — Этап 1.

---

## Часть 1. Сквозные принципы, которые не нарушаем ни на одном этапе

Это «конституционный закон» проекта. Все технические решения сверяются с ним.

### Принцип 1. Domain не знает ничего о платформе и фреймворках

`domain/` импортирует только Kotlin stdlib, `kotlinx.coroutines`, `kotlinx.datetime`. Никакого Ktor, Room, Compose, Koin, AndroidX. Если в `domain/` появляется `import io.ktor.*` — это баг архитектуры.

**Почему:** доменный слой — единственная точка, где живёт бизнес-логика. Если он чист, его можно полностью переиспользовать на новой платформе или после смены фреймворка. Если он грязен — KMP теряет смысл.

### Принцип 2. Single Source of Truth — это база данных

После Этапа 4 любой UI берёт данные **только** из Flow, который идёт из Room. Сеть — это «насос», который наполняет БД. UI никогда не показывает «свежие данные из сети» напрямую — только через БД.

**Почему:** без этого правила вы получите рассинхронизацию между списком и деталями, мерцание при возврате на экран, и нерешаемые edge-кейсы при offline. С ним — оффлайн «бесплатный».

### Принцип 3. UI = `f(State)`. Никаких side-effects в Compose-функциях

Composable читает `StateFlow`, рисует. Действия пользователя → события в ViewModel. Никаких `LaunchedEffect { repository.foo() }` напрямую — всегда через ViewModel.

### Принцип 4. Каждый `expect/actual` имеет минимальную поверхность

`expect` объявляется только тогда, когда дешевле сделать платформенный код, чем найти кроссплатформенную библиотеку. Поверхность `expect` — это **значения и фабрики**, а не куски логики. Если на трёх платформах разная логика — это уже признак, что нужна общая абстракция.

### Принцип 5. Тестируем доменный слой и репозиторий

UseCase + Repository тестируются в `commonTest` с фейками. ViewModel тестируется с `runTest` + fake UseCase. Compose-снэпшот-тесты — опционально. Тесты пишутся **в той же ветке**, что и фича, не «потом».

### Принцип 6. Никаких больших ветвей

Каждая ветка = ≤ 1 экран или ≤ 1 техническая возможность. Размер PR — ≤ 500 строк диффа. Большие фичи разбиваются на 3–5 PR. CI должен пройти зелёным до merge.

### Принцип 7. Зависимости фиксируются и обосновываются

Новая библиотека добавляется только если решает проблему, которую stdlib + текущие зависимости не решают. Каждое добавление в `libs.versions.toml` сопровождается комментарием в PR — зачем.

---

## Часть 2. Дорожная карта (этапы)

### Этап 1. Технический долг и наблюдаемость (1 PR)

**Что:**
1. Исправить `pageSize: Int = 1` → `20` в `NewsApi.kt`. Это критический баг — сейчас приходит по одной статье.
2. Заменить `Article.publishedAt: Long` на `kotlinx.datetime.Instant`. Маппер `String.toTimestamp()` уже использует `Instant.parse()`, просто прекращаем терять тип.
3. Добавить мультиплатформенный логгер. Выбор — **Kermit** (`co.touchlab:kermit`). Везде, где сейчас стоит `println("KTOR_LOG: ...")` или `// Log error` — заменить на `Logger.withTag("News").e(throwable) { "..." }`.
4. Убрать silent failure в `NewsRepositoryImpl.loadArticles()` — изменить тип на `suspend fun loadArticles(topic: String): DataResult<Unit>`. `addSubscription` теперь возвращает `DataResult<Unit>` и пробрасывает ошибку наверх.
5. Удалить мёртвый код: если subscription-флоу пока не дойдёт до UI на этом этапе — это нормально, оставляем как готовый API. Но `unused` use cases добавляются в Koin только тогда, когда становятся нужны.

**Почему сейчас:** эти три недочёта — мины. Pagination на `pageSize=1` ломает API запросы. `Long` для времени — после первой попытки фильтровать или группировать вы переделаете в `Instant`. Молчаливое глотание ошибок — самый трудноотлавливаемый класс багов.

**Готово, когда:** CI зелёный, `./gradlew check` проходит, в коде нет ни одного `println`/`// TODO error`.

---

### Этап 2. Тестовая инфраструктура (1 PR, до новых фич)

**Это самый важный этап в плане.** Без него все последующие этапы накапливают регрессии.

**Что:**
1. В `composeApp/build.gradle.kts` добавить в `commonTest.dependencies`:
   ```kotlin
   implementation(libs.kotlinx.coroutines.test)
   implementation(libs.turbine)        // Flow-тесты
   implementation(libs.kotlin.test)    // уже есть
   ```
   В `androidUnitTest` и `jvmTest` — `kotlin-test-junit`.

2. Структура `commonTest/`:
   ```
   commonTest/kotlin/org/newsagg/project/
     ├── fake/
     │   ├── FakeNewsApi.kt          // implements NewsApi
     │   └── FakeNewsDao.kt          // implements NewsDao with in-memory MutableStateFlow
     ├── data/
     │   └── NewsRepositoryImplTest.kt
     ├── domain/
     │   └── usecase/
     │       └── GetTopHeadlinesUseCaseTest.kt
     └── presentation/
         └── NewsFeedViewModelTest.kt
   ```

3. Минимум 5 тестов для старта:
   - Repository: success возвращает Success, network exception → Error.
   - Repository: `CancellationException` пробрасывается, а не превращается в Error.
   - UseCase: проксирует репозиторий.
   - ViewModel: при init эмитит Loading → Success.
   - ViewModel: при ошибке эмитит Loading → Error c сообщением.

4. CI: добавить шаг `./gradlew :composeApp:allTests` в Android-Desktop job.

**Почему сейчас:** каждая новая фича после этого этапа = тест в том же PR. Тесты пишутся пока код «горячий». Если отложить — будет 20 use cases без тестов, и никто их не напишет.

**Антипаттерны, которых избегаем:**
- Не используем mockito/MockK в `commonTest` — они не работают на iOS Native. Только ручные фейки на интерфейсах.
- Не пишем интеграционные тесты с реальным Room в `commonTest` — Room требует платформенного драйвера; вместо этого фейкаем `NewsDao` (интерфейс).
- Не пишем тесты на маппер `toDomain()` без бизнес-логики — это тест на присваивания.

**Готово, когда:** `./gradlew allTests` проходит на всех целях, в CI добавлен запуск тестов, code coverage по `domain/` и `data/repository/` ≥ 70%.

---

### Этап 3. Выбор и интеграция навигации (1 PR)

**Решение принимается ОДИН раз.** Поменять навигационную библиотеку после 5 экранов — это переписать половину presentation-слоя.

**Варианты:**

| | navigation-compose 2.8+ | Decompose | Voyager |
|---|---|---|---|
| KMP-зрелость | Beta, официальный Google | Production, отдельная модель | Production, Compose-only |
| iOS lifecycle | Слабо | **Отлично** (ComponentContext знает про iOS) | Слабо |
| Кривая обучения | Низкая (как на Android) | Средняя (новая ментальная модель) | Низкая |
| Тестируемость | Через VM | **Лучшая** (компоненты тестируются без Compose) | Через VM |
| Поддерживает Desktop-окна | Да | Да | Да |

**Рекомендация: Decompose.** Причины:
- Вы хотите iOS как полноценную цель, не как порт. Decompose именно про это.
- Тестируемость экранов — Decompose-компоненты тестируются как обычные классы, без Compose.
- Долгий жизненный путь — Decompose стабилен с 2021, никаких alpha-сюрпризов.

**Если выбираете navigation-compose** — это нормальный выбор, но: ViewModel'и остаются как есть, а на iOS жизненный цикл придётся «эмулировать» — это технический долг, который проявится позже.

**Что делаем (с Decompose):**
1. Заменить `NewsFeedViewModel : ViewModel()` на `NewsFeedComponent(componentContext: ComponentContext)`. ViewModel-слой переименовываем в Component-слой (или оставляем оба, на вкус — но не миксуем).
2. `RootComponent` создаёт child-компоненты через `childStack` (feed/detail/subscriptions).
3. На Android `MainActivity` владеет `DefaultComponentContext` через `defaultComponentContext()`. На iOS `MainViewController` — через `ApplicationLifecycle`. На Desktop — через `LifecycleRegistry`.
4. Composable получают компонент как параметр, не через `koinViewModel()`. Koin инжектит зависимости в фабрику компонента.
5. Сохранение состояния через `instanceKeeper` — пережить configuration change на Android.

**Антипаттерны:**
- Не миксовать Decompose-компоненты с `androidx.lifecycle.ViewModel`. Это два разных подхода к одной проблеме.
- Не хранить состояние навигации в `MutableStateFlow` ViewModel'а — это противоречит идее.

**Готово, когда:** есть Root → Feed экран, навигация на Detail (заглушка) работает на трёх платформах, тесты на Components в `commonTest` есть.

---

### Этап 4. Single Source of Truth: оффлайн-первый репозиторий (1–2 PR)

**Цель:** UI читает только `Flow<List<Article>>` из Room. Сеть — асинхронный «насос».

**Что:**
1. В `NewsDao` добавить:
   ```kotlin
   @Query("SELECT * FROM articles WHERE topic = :topic ORDER BY publishedAt DESC")
   fun observeArticles(topic: String): Flow<List<ArticleDbModel>>
   ```
2. `NewsRepository` меняется:
   ```kotlin
   fun observeArticles(topic: String): Flow<List<Article>>
   suspend fun refreshArticles(topic: String): DataResult<Unit>
   ```
   `getTopHeadlines(): DataResult<List<Article>>` **удаляется**. Top headlines становятся подпиской на topic = "headlines".
3. `refreshArticles` фетчит из сети, мапит в `ArticleDbModel`, делает `dao.upsertArticles(...)`. Никогда не возвращает данные — данные приходят через Flow.
4. ViewModel/Component:
   - подписывается на `repository.observeArticles(topic)` через `flow.stateIn(scope)`,
   - при init и pull-to-refresh зовёт `refreshArticles()`,
   - стейт = `(articles: List<Article>, refreshing: Boolean, error: String?)`.
5. Добавить колонку `cachedAt: Instant` в `ArticleDbModel`. Можно потом делать stale-while-revalidate.
6. **Миграция Room:** так как `version = 1`, и у вас `fallbackToDestructiveMigration` — на этом этапе можно поднять до `version = 2` с тем же fallback. Но **до релиза** надо написать настоящую миграцию для `cachedAt`. Это TODO в коде с явным комментарием.

**Антипаттерны:**
- Не делать `getTopHeadlines(): List<Article>` параллельно с `observeArticles()`. Два пути приведут к рассинхрону.
- Не оставлять `DataResult` в Flow — Flow ошибки обрабатывает через `.catch {}`. `DataResult` остаётся только для одноразовых suspend-операций (refresh).
- Не дублировать данные между network DTO и domain model в UI — UI знает только про `Article`.

**Почему сейчас:** до того как добавили pagination и закладки. Pagination на двух источниках истины — кошмар. Закладки без SSOT — отдельная таблица, которая дублирует данные.

**Готово, когда:** выключенный интернет = последние данные на экране, ошибка показывается как баннер, не уничтожая данные. Тесты на repository проверяют оба сценария (cache hit / network fail).

---

### Этап 5. Экран деталей + навигация (1 PR)

**Что:**
1. `ArticleDetailComponent` принимает `articleId: String` (= URL, у вас он есть как primary candidate). Подписывается на `repository.observeArticle(url)`.
2. В DAO: `@Query("SELECT * FROM articles WHERE url = :url") fun observeArticle(url: String): Flow<ArticleDbModel?>`.
3. Composable читает state, рисует заголовок/описание/картинку/«Поделиться»/«В закладки».
4. Картинки: **Coil 3** (`io.coil-kt.coil3:coil-compose` 3.0+, теперь KMP). Альтернатива — Kamel. Coil лучше: единый API с Android, кэш диска, новейший.
5. Тест на ArticleDetailComponent: рендерит данные из фейкового репо.

**Антипаттерны:**
- Не передавать `Article` целиком в навигационный аргумент — только URL. Иначе придётся сериализовать модели (а навигация и не должна знать про доменные модели).

---

### Этап 6. Закладки (1 PR)

**Что:**
1. Новая таблица `BookmarkDbModel(url: String, savedAt: Instant)`. Простой join с articles или denormalize — на вкус. Рекомендую отдельную таблицу + join.
2. DAO: `observeBookmarks(): Flow<List<ArticleDbModel>>`, `addBookmark(url, savedAt)`, `removeBookmark(url)`, `isBookmarked(url): Flow<Boolean>`.
3. UseCases: `ToggleBookmarkUseCase(url)`, `ObserveBookmarksUseCase`.
4. На детальном экране — кнопка-флажок. Состояние из `isBookmarked(url)`.
5. Новый экран `BookmarksScreen` — список из `observeBookmarks()`. Навигация: Root содержит таб-бар или drawer (Feed / Bookmarks / Subscriptions).
6. Миграция Room → `version = 3`.

**Антипаттерны:**
- Не делать `bookmarked: Boolean` колонкой в `ArticleDbModel` — статья может быть «не закладкой и в фиде», «закладкой и в фиде», «закладкой и удалена из фида». Отдельная таблица решает все три случая.
- Не вызывать `repository.addBookmark()` напрямую из Composable — только через VM/Component.

---

### Этап 7. Pagination (1 PR)

**Цель:** infinite scroll в Feed.

**Что:**
1. **Подход:** `androidx.paging:paging-common` (multiplatform) + `paging-compose`. Они уже у вас в `libs.versions.toml` — выбор уже сделан фундаментом.
2. Room поддерживает `PagingSource<Int, ArticleDbModel>` через `room-paging`. SSOT сохраняется.
3. RemoteMediator (или его multiplatform-эквивалент) — фетчит следующую страницу из сети, заливает в Room.
4. UI: `LazyColumn` + `LazyPagingItems`, обработка load states (Loading/Error на нижнем элементе).
5. На сетевой странице: дедупликация по URL при upsert в Room (PK = url, `OnConflictStrategy.REPLACE`).

**Антипаттерны:**
- Не пытаться сделать пагинацию руками через offset/limit и `combine` нескольких Flow — это велосипед, который сломается. Используйте Paging 3.
- Если выбрали SQLDelight — Paging 3 ему чужой; нужно SQLDelight Paging Extension. **Это ещё один аргумент за Room.**

---

### Этап 8. Pull-to-refresh + индикаторы загрузки (small PR)

**Что:**
1. `PullToRefreshBox` из Compose Material3 (есть multiplatform с 1.7+).
2. Связать с `viewModel.refresh()` который дергает `repository.refreshArticles(topic)`.
3. Ошибки рефреша — показывать `Snackbar`, **не** заменять список (SSOT остаётся в Room).

---

### Этап 9. `expect/actual`: «Поделиться» (1 PR)

**Что:**
```kotlin
// commonMain
interface ShareHandler {
    fun share(url: String, title: String)
}
expect class ShareHandlerFactory {
    fun create(): ShareHandler
}

// androidMain — Intent.ACTION_SEND
// jvmMain — ClipboardManager копирует ссылку + Toast/SnackbarHostState
// iosMain — UIActivityViewController
```
- На каждой платформе своя фабрика — провайдится в платформенном Koin-модуле.
- В Component получаем `ShareHandler` через Koin.

**Антипаттерн:** `expect fun share(url: String)` напрямую. На iOS вам понадобится `UIViewController` для презентации `UIActivityViewController` — это означает зависимость от текущего экрана. Поэтому интерфейс + фабрика.

---

### Этап 10. Desktop-адаптивность (1 PR)

**Что:**
1. `WindowSizeClass` (есть в material3-adaptive multiplatform).
2. Compact → текущий вертикальный список. Expanded → master-detail layout (список слева, деталь справа в одном окне).
3. Размер окна: `Window(state = rememberWindowState(width = 1200.dp, height = 800.dp))`.
4. Скроллбар на JVM (`VerticalScrollbar` из Compose Desktop).

---

### Этап 11. Подписки (UI + интеграция) (1 PR)

**Это та фича, для которой у вас уже есть `addSubscription/deleteSubscription/getAllSubscriptions` в репозитории.**

**Что:**
1. Экран `SubscriptionsScreen`: список топиков, кнопка «+», TextField для нового топика, swipe-to-delete.
2. Feed теперь умеет показывать «headlines» или конкретный topic. Селектор сверху.
3. При добавлении подписки — фоновый `refreshArticles(topic)`, ошибки в Snackbar.
4. Тесты use case'ов раскоментировать в Koin (`AddSubscriptionUseCase` и т.д. — они сейчас зарегистрированы, но не используются — это нарушает Принцип 7).

---

### Этап 12. Релизные конфиги и безопасность (1 PR)

**Что:**
1. Build-варианты: `dev` / `prod`. Разные API-ключи через BuildKonfig flavors.
2. Если ключ = `KEY_NOT_FOUND` — `init` приложения логирует громкое предупреждение через Kermit.
3. R8/Proguard правила для Ktor и Room:
   - `-keep class io.ktor.** { *; }`
   - `-keep class kotlinx.serialization.** { *; }`
   - `-keepclasseswithmembers class * { @kotlinx.serialization.Serializable <init>(...); }`
4. `release` build на Android: `isMinifyEnabled = true`, тестируется отдельным CI job на PR.
5. App-icon, app-name, target SDK review.

**Антипаттерны:**
- Не коммитить настоящий ключ. `.gitignore` уже должен покрывать `local.properties`.
- Не выключать `isMinifyEnabled` навсегда — это техдолг, который к релизу превратится в day-long debug session.

---

### Этап 13. CI зрелость (small PR)

**Что:**
1. `./gradlew check` запускается на каждом PR — включает тесты, ktlint/detekt, room schema validation.
2. detekt с baseline-файлом (не блокировать существующий код, ловить новый).
3. Android release job на PR (проверяет R8).
4. Кэширование `~/.gradle/caches` и `~/.konan` в CI — иначе iOS job — 15 минут.
5. Branch protection: `main` требует прохождения CI.

---

## Часть 3. Контрольные ворота между этапами

Перед merge каждого PR — пробежаться по чек-листу:

1. **Domain чистый?** `grep -r "import io.ktor\|import androidx.room\|import androidx.compose" composeApp/src/commonMain/kotlin/org/newsagg/project/domain/` → пусто.
2. **Тесты есть?** Любой новый класс в `data/repository/`, `domain/usecase/`, `presentation/` имеет тест в той же ветке.
3. **CI зелёный?** Android assembleDebug + Desktop + iOS compile + allTests.
4. **Никаких `println` / silent catches?** `grep -rn "println\|// TODO\|// Log" composeApp/src` → ничего нового.
5. **Никаких неиспользуемых Koin-регистраций?** Если `XUseCase` зарегистрирован, должна быть хотя бы одна точка инъекции.
6. **Размер PR ≤ 500 строк?** Если больше — разбить.
7. **Миграция Room?** Если поменялась схема, version++ и комментарий «TODO real migration before release».

---

## Часть 4. Чего НЕ делаем

Эти решения принимаются один раз — **в пользу не делать**.

1. **Не используем GraphQL/Apollo.** REST + Ktor покрывает 100% задач.
2. **Не пишем свой кастомный image loader.** Coil 3 решает.
3. **Не миксуем Decompose и androidx.lifecycle ViewModel.** Выбрали одно — живём с ним.
4. **Не используем SQLDelight, если выбран Room.** Не миксуем БД-решения.
5. **Не добавляем DataStore до того, как понадобятся настройки.** Настройки → отдельный этап с обоснованием.
6. **Не пишем kotlin-inject / Dagger.** Koin работает, перезд = техдолг ради техдолга.
7. **Не выносим код в отдельные модули (`:data`, `:domain`, `:presentation`)**, пока проект < 50 файлов на модуль. Преждевременная модуляризация = больно в KMP (KSP/Room source set magic).

---

## Часть 5. Долговременные риски и как мы их закрываем

| Риск | Когда проявится | Как закрываем |
|---|---|---|
| Room 2.7 alpha баги | Любой этап | Закрепить версии, не апгрейдить до релиза без причины. На релизе перейти на stable. |
| Compose Multiplatform 1.10 alpha material3 | UI этапы | Та же стратегия. |
| API key утечка | Релиз | Этап 12. До релиза — `KEY_NOT_FOUND` ловится в логах. |
| iOS lifecycle bugs | После Этапа 3 | Decompose решает это by design. Тесты на iOS компиляции в CI на каждом PR. |
| Тесты на iOS Native ломаются (mock-библиотеки) | Этап 2 | Только ручные фейки. Никакого Mockk. |
| Drift между network и domain | Этап 4+ | Маппер тестируется (минимально), DTO живут только в `data/network/`. |
| Громоздкие PR | Любой этап | Жёсткий лимит 500 строк, разбиваем на feature flags если надо. |

---

## Часть 6. Итерационный темп

| Этап | Размер | Срок (соло, ~10ч/нед) |
|---|---|---|
| 1. Tech debt | S | 1 день |
| 2. Тесты | M | 2–3 дня |
| 3. Навигация | L | 3–5 дней |
| 4. SSOT | L | 4–5 дней |
| 5. Detail | S | 1 день |
| 6. Bookmarks | M | 2–3 дня |
| 7. Pagination | L | 4–5 дней |
| 8. Pull-to-refresh | S | 1 день |
| 9. Share expect/actual | M | 2 дня |
| 10. Desktop adaptive | M | 2 дня |
| 11. Subscriptions UI | M | 2–3 дня |
| 12. Release config | M | 2 дня |
| 13. CI maturity | S | 1 день |

**Всего:** ~6–8 недель в темпе «по вечерам». Если выпадает этап — план не разваливается, потому что каждый этап = один PR с зелёным CI.

---

## Часть 7. Что считать «версия 1.0»

После Этапа 12 у вас:
- Три платформы: Android APK (release-signed), Desktop DMG/MSI/Deb, iOS Framework который запускается в Xcode на симуляторе.
- Оффлайн-режим работает.
- Закладки сохраняются.
- Pagination без лагов.
- Pull-to-refresh.
- Подписки и кастомные топики.
- Шаринг работает на трёх платформах.
- Adaptive UI на Desktop.
- Тесты ≥ 70% покрытия domain+data.
- CI на трёх платформах за < 10 минут.

Это **production-grade KMP-проект**, который можно положить в портфолио и в App Store.

---

## Главный совет

**Не пытайтесь пройти план за две недели.** Главная ценность плана — что после каждого PR вы можете остановиться, и проект — в рабочем, минимально-полном состоянии. Если на середине Этапа 7 решите бросить — у вас есть приложение с deтail-экраном и закладками. Это не «черновик», это законченная итерация.

Разница между рабочим прототипом и production-кодом — это процесс, а не объём кода. Здесь — тот же процесс, только распределённый по 13 PR.
