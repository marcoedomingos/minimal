package ao.marco.domingos.minimal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import ao.marco.domingos.minimal.config.AppDatabase
import ao.marco.domingos.minimal.ui.pages.HomePage
import ao.marco.domingos.minimal.ui.pages.WelcomePage
import ao.marco.domingos.minimal.ui.theme.MinimalTheme
import ao.marco.domingos.minimal.ui.viewmodel.AccountViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "account-database"
        ).build()
        val viewModel = AccountViewModel(db)
        
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MinimalTheme {
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "home") {
                    composable("welcome") {
                        WelcomePage(onGetStarted = {
                            navController.navigate("home") {
                                popUpTo("welcome") { inclusive = true }
                            }
                        })
                    }
                    composable("home") {
                        HomePage(viewModel)
                    }
                }
            }
        }
    }
}