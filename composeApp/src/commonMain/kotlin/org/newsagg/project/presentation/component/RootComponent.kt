package org.newsagg.project.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.newsagg.project.domain.usecase.ObserveArticlesUseCase
import org.newsagg.project.domain.usecase.RefreshArticlesUseCase

interface RootComponent {

    val childStack: Value<ChildStack<*, Child>>

    sealed class Child {
        class Feed(val component: NewsFeedComponent): Child()
    }
}

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val observeArticlesUseCase: ObserveArticlesUseCase,
    private val refreshArticlesUseCase: RefreshArticlesUseCase
): RootComponent, ComponentContext by componentContext, KoinComponent {


    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Feed,
        handleBackButton = true,
        childFactory = ::createChild
    )

    private fun createChild(config: Config, context: ComponentContext): RootComponent.Child {
        return when (config) {
            Config.Feed -> RootComponent.Child.Feed(
                DefaultNewsFeedComponent(context, observeArticlesUseCase,refreshArticlesUseCase)
            )
        }
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Feed : Config
    }
}