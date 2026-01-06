package com.movil.mucamas

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
import com.movil.mucamas.ui.theme.MucamasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MucamasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MucamasApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MucamasApp(modifier: Modifier = Modifier) {
    Text(
        text = "Mucama's",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun MucamasAppPreview() {
    MucamasTheme {
        MucamasApp()
    }
}
