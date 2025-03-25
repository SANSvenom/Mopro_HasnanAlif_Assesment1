package com.hasnan0062.hasnanalif_607062300062_assesment1

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteApp()
        }
    }
}

@Composable
fun NoteApp() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("note") { NoteScreen(navController) }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(text = "Simple Note App", fontSize = 24.sp, color = Color.Black)
        Button(onClick = { navController.navigate("note") }) {
            Text("Create Note")
        }
    }
}

@Composable
fun NoteScreen(navController: NavController) {
    var noteText by remember { mutableStateOf(TextFieldValue()) }
    var isImportant by remember { mutableStateOf(false) }
    val languageOptions = listOf("English", "Indonesia")
    var selectedLanguage by remember { mutableStateOf(languageOptions[0]) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Create Note", fontSize = 20.sp)
        BasicTextField(
            value = noteText,
            onValueChange = { noteText = it },
            modifier = Modifier.fillMaxWidth().height(100.dp)
        )

        Row {
            Text("Mark as Important")
            Checkbox(
                checked = isImportant,
                onCheckedChange = { isImportant = it }
            )
        }

        DropdownMenuDemo(selectedLanguage, languageOptions) { selectedLanguage = it }

        Button(onClick = {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, noteText.text)
                type = "text/plain"
            }
            navController.context.startActivity(Intent.createChooser(intent, "Share via"))
        }) {
            Text("Share Note")
        }
    }
}

@Composable
fun DropdownMenuDemo(selected: String, options: List<String>, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.wrapContentSize()) {
        Button(onClick = { expanded = true }) { Text(text = selected) }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )

//                {
//                    Text(option)
//                }
            }
        }
    }
}
