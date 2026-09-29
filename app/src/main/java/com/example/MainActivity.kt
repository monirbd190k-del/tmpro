package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ProjectRepository
import com.example.data.TMProDatabase
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainTab
import com.example.viewmodel.TMProViewModel
import com.example.viewmodel.TMProViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = TMProDatabase.getInstance(applicationContext)
        val repository = ProjectRepository(database.projectDao())
        val factory = TMProViewModelFactory(repository)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0D0D0F)
                ) {
                    val viewModel: TMProViewModel = viewModel(factory = factory)
                    TMProApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun TMProApp(viewModel: TMProViewModel) {
    val isEditorOpen by viewModel.isEditorOpen.collectAsState()
    val isGalleryOpen by viewModel.isGalleryOpen.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0F))
    ) {
        if (isEditorOpen) {
            EditorScreen(viewModel = viewModel)
        } else {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabContent"
            ) { tab ->
                when (tab) {
                    MainTab.HOME -> HomeScreen(viewModel = viewModel)
                    MainTab.PROJECTS -> ProjectsScreen(viewModel = viewModel)
                    MainTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }

        if (isGalleryOpen) {
            com.example.ui.components.GalleryImportDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.closeGallery() }
            )
        }
    }
}
