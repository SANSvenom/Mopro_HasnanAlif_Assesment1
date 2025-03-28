package com.hasnan0062.hasnanalif_607062300062_assesment1

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import android.content.Intent
import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) // Ikuti sistem

        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            ) {
                NoteApp()
            }
        }
    }
}

data class Note(
    val id: String = UUID.randomUUID().toString(),
    var text: String,
    var imageUri: Uri?,
    var isImportant: Boolean
)

//  Tambahkan ViewModel agar data tetap ada saat berpindah mode
class NoteViewModel : ViewModel() {
    var notes = mutableStateListOf<Note>()
}

@Composable
fun NoteApp() {
    val navController = rememberNavController()
    val viewModel: NoteViewModel = viewModel() // Gunakan ViewModel

    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(navController, viewModel) }
        composable("note/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            NoteScreen(navController, viewModel, noteId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: NoteViewModel) {
    val context = LocalContext.current  // Konteks aplikasi untuk Intent
    val notes = viewModel.notes
    val isDarkTheme = isSystemInDarkTheme() // Cek apakah dark mode aktif

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_note),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(40.dp)
                                .padding(end = 8.dp)
                        )
                        Text("Catatan", color = if (isDarkTheme) Color.White else Color.Black)
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Button(onClick = { navController.navigate("note/new") }) {
                Text("Buat Catatan")
            }
            Spacer(modifier = Modifier.height(16.dp))

            val sortedNotes = notes.sortedByDescending { it.isImportant }
            sortedNotes.forEach { note ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .border(
                            width = 2.dp,
                            color = if (isDarkTheme) Color.White else Color.Black,
                            shape = MaterialTheme.shapes.medium
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally // Biar rapi di tengah
                    ) {
                        // Menampilkan gambar jika ada
                        note.imageUri?.let { uri ->
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Note Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp) // Ukuran gambar lebih jelas
                                    .padding(bottom = 8.dp)
                            )
                        }

                        // Tampilkan teks di bawah gambar
                        Text(
                            text = note.text,
                            fontSize = 16.sp,
                            color = if (isDarkTheme) Color.White else Color.Black,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Row untuk tombol edit, delete, dan share
                        Row {
                            IconButton(onClick = { navController.navigate("note/${note.id}") }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray)
                            }
                            IconButton(onClick = { notes.remove(note) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                            IconButton(onClick = { shareNote(context, note.text) }) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.Gray)
                            }
                        }
                    }
                }
            }

        }
    }
}


// Fungsi untuk berbagi catatan menggunakan Intent
fun shareNote(context: Context, noteText: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, noteText)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, context.getString(R.string.share_via)) //  FIXED
    context.startActivity(shareIntent)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(navController: NavController, viewModel: NoteViewModel, noteId: String?) {
    var noteText by remember { mutableStateOf(TextFieldValue()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isImportant by remember { mutableStateOf(false) }

    val isEditing = noteId != "new"
    val noteToEdit = viewModel.notes.find { it.id == noteId }

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
                title = { Text(stringResource(id = if (isEditing) R.string.edit_note else R.string.create_note)) },
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
        },
        bottomBar = {
            Button(
                onClick = {
                    if (isEditing && noteToEdit != null) {
                        noteToEdit.text = noteText.text
                        noteToEdit.imageUri = selectedImageUri
                        noteToEdit.isImportant = isImportant
                    } else {
                        viewModel.notes.add(Note(text = noteText.text, imageUri = selectedImageUri, isImportant = isImportant))
                    }
                    navController.navigate("home")
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(stringResource(id = R.string.save_note))
            }
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
                Text(stringResource(id = R.string.select_image))
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
            TextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant, //  Background sesuai tema
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Gray
                ),
                textStyle = LocalTextStyle.current.copy(
                    color = MaterialTheme.colorScheme.onSurface //  Warna teks mengikuti tema
                )
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isImportant, onCheckedChange = { isImportant = it })
                Text(stringResource(id = R.string.mark_as_important))
            }
        }
    }
}
