package org.newsagg.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.retainedComponent
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf
import org.newsagg.project.presentation.component.DefaultRootComponent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val root = retainedComponent { context -> get<DefaultRootComponent> { parametersOf(context) } }

        setContent {
            App(rootComponent = root)
        }
    }
}
