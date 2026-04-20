package ao.marco.domingos.minimal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.Room
import ao.marco.domingos.minimal.config.AppDatabase
import ao.marco.domingos.minimal.ui.pages.HomePage
import ao.marco.domingos.minimal.ui.theme.MinimalTheme
import ao.marco.domingos.minimal.ui.viewmodel.AccountViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "account-database"
        ).build()
        val viewModel = AccountViewModel(db);
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MinimalTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomePage(innerPadding, db, viewModel)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MinimalTheme {
        Greeting("Android")
    }
}