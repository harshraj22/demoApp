package com.example.demoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.demoapp.ui.theme.DemoAppTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      DemoAppTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          DataLoggerApp()
        }
      }
    }
  }
}

@Composable
fun DataLoggerApp() {
  val navController = rememberNavController()

  NavHost(navController = navController, startDestination = "dataEntry") {
    composable("dataEntry") {
      LogDataScreen(
        onNavigateToGraph = {
          navController.navigate("graph")
        }
      )
    }
    composable("graph") {
      GraphScreen(
        onNavigateToEntry = {
          navController.navigate("dataEntry")
        }
      )
    }
  }
}