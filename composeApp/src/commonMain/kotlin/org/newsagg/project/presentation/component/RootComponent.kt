package org.newsagg.project.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.newsagg.project.domain.usecase.AddSubscriptionUseCase
import org.newsagg.project.domain.usecase.DeleteSubscriptionUseCase
import org.newsagg.project.domain.usecase.GetAllSubscriptionsUseCase
import org.newsagg.project.domain.usecase.ObserveArticlesPagingUseCase
import org.newsagg.project.presentation.component.RootComponent.Child.Feed

interface RootComponent {

    val childStack: Value<ChildStack<*, Child>>

    sealed class Child {
        class Feed(val component: NewsFeedComponent): Child()
    }
}

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val getAllSubscriptionsUseCase: GetAllSubscriptionsUseCase,
    private val observeArticlesPagingUseCase: ObserveArticlesPagingUseCase,
    private val addSubscriptionUseCase: AddSubscriptionUseCase,
    private val deleteSubscriptionUseCase: DeleteSubscriptionUseCase
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
            Config.Feed -> Feed(
                DefaultNewsFeedComponent(context,
                    observeArticlesPagingUseCase = observeArticlesPagingUseCase,
                    getAllSubscriptionsUseCase = getAllSubscriptionsUseCase,
                    addSubscriptionUseCase = addSubscriptionUseCase,
                    deleteSubscriptionUseCase = deleteSubscriptionUseCase
                )


            )
        }
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Feed : Config
    }
}