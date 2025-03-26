package com.hasnan0062.hasnanalif_607062300062_assesment1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter

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
    val notes = remember { mutableStateListOf<Triple<String, Uri?, Boolean>>() }

    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(navController, notes) }
        composable("note") { NoteScreen(navController, notes) }
    }
}

@Composable
fun HomeScreen(navController: NavController, notes: List<Triple<String, Uri?, Boolean>>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Simple Note App", fontSize = 24.sp, color = Color.Black)
        Button(onClick = { navController.navigate("note") }) {
            Text("Create Note")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Urutkan catatan, yang "penting" tampil di atas
        val sortedNotes = notes.sortedByDescending { it.third }

        sortedNotes.forEach { (text, imageUri, isImportant) ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                if (isImportant) {
                    Text("📌 Pinned Note", fontSize = 14.sp, color = Color.Red)
                }

                imageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Note Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
                Text(text = text, fontSize = 16.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun NoteScreen(navController: NavController, notes: MutableList<Triple<String, Uri?, Boolean>>) {
    var noteText by remember { mutableStateOf(TextFieldValue()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isImportant by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Create Note", fontSize = 20.sp, color = Color.Black)

        Button(onClick = { imagePickerLauncher.launch("image/*") }) {
            Text("Select Image")
        }

        selectedImageUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        BasicTextField(
            value = noteText,
            onValueChange = { noteText = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isImportant,
                onCheckedChange = { isImportant = it }
            )
            Text("Mark as Important")
        }

        Button(onClick = {
            notes.add(Triple(noteText.text, selectedImageUri, isImportant))
            navController.navigate("home")
        }) {
            Text("Save Note")
        }
    }
}
