package org.newsagg.project.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent

interface RootComponent {

    val childStack: Value<ChildStack<*, Child>>

    sealed class Child {
        class Feed(val component: NewsFeedComponent): Child()
    }
}

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val getTopHeadlinesUseCase: GetTopHeadlinesUseCase
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
                DefaultNewsFeedComponent(context, getTopHeadlinesUseCase)
            )
        }
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Feed : Config
    }
}