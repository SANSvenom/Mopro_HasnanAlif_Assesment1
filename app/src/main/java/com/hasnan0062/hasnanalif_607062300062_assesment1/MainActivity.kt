package com.hasnan0062.hasnanalif_607062300062_assesment1

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteApp()
        }
    }
}

// Menggunakan data class agar lebih mudah dikelola
data class Note(
    val id: String = UUID.randomUUID().toString(),
    var text: String,
    var imageUri: Uri?,
    var isImportant: Boolean
)

@Composable
fun NoteApp() {
    val navController = rememberNavController()
    val notes = remember { mutableStateListOf<Note>() }

    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(navController, notes) }
        composable("note/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            NoteScreen(navController, notes, noteId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, notes: MutableList<Note>) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simple Note App") },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    )
    { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Button(onClick = { navController.navigate("note/new") }) {
                Text("Create Note")
            }

            Spacer(modifier = Modifier.height(16.dp))

            val sortedNotes = notes.sortedByDescending { it.isImportant }
            sortedNotes.forEach { note ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    if (note.isImportant) {
                        Text("📌 Pinned Note", fontSize = 14.sp, color = Color.Red)
                    }

                    note.imageUri?.let {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Note Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                    Text(text = note.text, fontSize = 16.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        IconButton(onClick = { navController.navigate("note/${note.id}") }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { notes.remove(note) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(navController: NavController, notes: MutableList<Note>, noteId: String?) {
    var noteText by remember { mutableStateOf(TextFieldValue()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isImportant by remember { mutableStateOf(false) }

    val isEditing = noteId != "new" // Cek apakah sedang edit atau membuat note baru
    val noteToEdit = notes.find { it.id == noteId }

    LaunchedEffect(noteToEdit) {
        noteToEdit?.let {
            noteText = TextFieldValue(it.text)
            selectedImageUri = it.imageUri
            isImportant = it.isImportant
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Note" else "Create Note") }, // Menyesuaikan judul
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home") }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                if (isEditing) {
                    // Edit note lama
                    noteToEdit?.apply {
                        text = noteText.text
                        imageUri = selectedImageUri
                        isImportant = isImportant
                    }
                } else {
                    // Tambahkan note baru
                    notes.add(Note(text = noteText.text, imageUri = selectedImageUri, isImportant = isImportant))
                }
                navController.navigate("home")
            }) {
                Text("Save Note")
            }
        }
    }
}

