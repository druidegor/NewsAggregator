package org.newsagg.project.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.newsagg.project.presentation.component.NewsFeedComponent

@Composable
fun NewsFeedScreen(
    component: NewsFeedComponent,
    modifier: Modifier = Modifier
) {
    // Подписываемся на единственный источник состояния компонента
    val state by component.state.subscribeAsState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 1. Ошибка показывается как БАННЕР сверху, только если в кэше уже есть данные
            AnimatedVisibility(visible = state.articles.isNotEmpty() && state.error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Red.copy(alpha = 0.8f)) // Временный цвет баннера до настройки темы
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Проблемы с соединением. Показываем старые новости.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // 2. Индикатор фонового обновления (тот самый refreshing / чкуакуырштп)
            if (state.refreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // 3. Основная логика отображения контента
            if (state.articles.isEmpty()) {
                // Если база данных СОВСЕМ пустая (например, первый запуск приложения)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (state.refreshing) {
                        // Первичный лоадер на весь экран
                        CircularProgressIndicator()
                    } else if (state.error != null) {
                        // Полноэкранная ошибка, только если показывать вообще нечего
                        Text(
                            text = "Не удалось загрузить новости:\n${state.error}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(text = "Нет доступных новостей.")
                    }
                }
            } else {
                // Если в базе ЕСТЬ статьи — они ВСЕГДА на экране, независимо от ошибок сети!
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.articles,
                        key = { it.url } // Используем url как уникальный ключ для оптимизации списков
                    ) { article ->
                        // Здесь пока оставляем простую разметку, дизайн карточки будет на следующих этапах
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = article.title)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = article.description)
                            }
                        }
                    }
                }
            }
        }
    }
}
//@Composable
//fun NewsFeedScreen(
//    component: NewsFeedComponent,
//    modifier: Modifier = Modifier
//) {
//    val state by component.state.subscribeAsState()
//
//    Box(modifier = modifier.fillMaxSize()) {
//        if (state.isLoading) {
//            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//        } else if (state.error != null) {
//            Text(
//                text = "Error: ${state.error}",
//                modifier = Modifier.align(Alignment.Center).padding(16.dp)
//            )
//        } else if (state.articles.isEmpty()) {
//            Text(
//                text = "No articles found.",
//                modifier = Modifier.align(Alignment.Center)
//            )
//        } else {
//            LazyColumn(
//                modifier = Modifier.fillMaxSize(),
//                contentPadding = PaddingValues(16.dp),
//                verticalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                items(
//                    items = state.articles,
//                    key = { it.url }
//                ) { article ->
//                    Text(text = article.title)
//                }
//            }
//        }
//    }
//}
