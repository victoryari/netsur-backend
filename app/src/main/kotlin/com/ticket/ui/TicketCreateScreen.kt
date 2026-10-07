package com.ticket.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ticket.common.Ticket
import com.ticket.common.User
import com.ticket.common.UserRole
import com.ticket.common.database.TiDBService
import com.ticket.common.ui.components.*
import com.ticket.common.ui.theme.TicketShapes
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketCreateScreen(navController: NavController, user: User?) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIA") }
    var category by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var attachmentUri by remember { mutableStateOf<Uri?>(null) }
    var attachmentName by remember { mutableStateOf("") }
    var attachmentMime by remember { mutableStateOf("") }
    var attachmentBytes by remember { mutableStateOf<ByteArray?>(null) }

    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var cameraPhotoFile by remember { mutableStateOf<File?>(null) }

    var technicians by remember { mutableStateOf<List<User>>(emptyList()) }
    var selectedTech by remember { mutableStateOf<User?>(null) }
    var techDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        if (user?.role == UserRole.ADMIN) {
            technicians = TiDBService.getTechnicians()
        }
    }

    // Launcher para la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && cameraPhotoUri != null && cameraPhotoFile != null) {
            attachmentUri = cameraPhotoUri
            attachmentName = cameraPhotoFile!!.name
            attachmentMime = "image/jpeg"
            attachmentBytes = cameraPhotoFile!!.readBytes()
        }
    }

    fun executeLaunchCamera() {
        try {
            val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val photoFile = File.createTempFile(
                "ticket_foto_${System.currentTimeMillis()}",
                ".jpg",
                picturesDir ?: context.cacheDir
            )
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraPhotoFile = photoFile
            cameraPhotoUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            errorMessage = "Error al abrir la cámara: ${e.localizedMessage}"
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            executeLaunchCamera()
        } else {
            errorMessage = "Se requiere permiso de cámara para capturar fotografías"
        }
    }

    fun launchCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            executeLaunchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Launcher para galería de imágenes
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            attachmentUri = uri
            attachmentName = uri.lastPathSegment ?: "foto.jpg"
            attachmentMime = context.contentResolver.getType(uri) ?: "image/jpeg"
            attachmentBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        }
    }

    // Launcher para archivos generales
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            attachmentUri = uri
            attachmentName = uri.lastPathSegment ?: "archivo"
            attachmentMime = context.contentResolver.getType(uri) ?: "application/octet-stream"
            attachmentBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Ticket") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TicketTextField(
                value = title,
                onValueChange = { title = it },
                label = "Título de la incidencia",
                placeholder = "Ej. Falla en impresora de recepción",
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            TicketTextField(
                value = description,
                onValueChange = { description = it },
                label = "Descripción detallada",
                placeholder = "Explica lo que sucede con el mayor detalle posible...",
                singleLine = false,
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Prioridad", style = MaterialTheme.typography.labelLarge)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("BAJA", "MEDIA", "ALTA", "URGENTE").forEach { p ->
                    FilterChip(
                        selected = priority == p,
                        onClick = { priority = p },
                        label = { Text(p, style = MaterialTheme.typography.labelMedium) },
                        shape = TicketShapes.full
                    )
                }
            }

            TicketTextField(
                value = category,
                onValueChange = { category = it },
                label = "Categoría (opcional)",
                placeholder = "Ej. Hardware, Redes, Software",
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Adjunto (opcional)", style = MaterialTheme.typography.labelLarge)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TicketButton(
                    text = "Cámara",
                    onClick = { launchCamera() },
                    type = ButtonType.Outlined,
                    size = ButtonSize.Small,
                    icon = Icons.Filled.PhotoCamera,
                    modifier = Modifier.weight(1f)
                )

                TicketButton(
                    text = "Galería",
                    onClick = { photoPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    type = ButtonType.Outlined,
                    size = ButtonSize.Small,
                    icon = Icons.Filled.Image,
                    modifier = Modifier.weight(1f)
                )

                TicketButton(
                    text = "Archivo",
                    onClick = { filePicker.launch("*/*") },
                    type = ButtonType.Outlined,
                    size = ButtonSize.Small,
                    icon = Icons.Filled.AttachFile,
                    modifier = Modifier.weight(1f)
                )
            }

            if (attachmentUri != null) {
                TicketCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (attachmentMime.startsWith("image/")) {
                            AsyncImage(
                                model = attachmentUri,
                                contentDescription = attachmentName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(TicketShapes.md),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AttachFile, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(attachmentName, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TicketButton(
                                text = "Quitar adjunto",
                                onClick = {
                                    attachmentUri = null
                                    attachmentName = ""
                                    attachmentMime = ""
                                    attachmentBytes = null
                                },
                                type = ButtonType.Text,
                                size = ButtonSize.Small
                            )
                        }
                    }
                }
            }

            if (user?.role == UserRole.ADMIN) {
                Text("Asignar a técnico (opcional)", style = MaterialTheme.typography.labelLarge)

                Box(modifier = Modifier.fillMaxWidth()) {
                    TicketTextField(
                        value = selectedTech?.let { "${it.displayName ?: it.email} (${it.email})" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = "Técnico asignado",
                        trailingIcon = {
                            Icon(
                                if (techDropdownExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { techDropdownExpanded = true }
                    )

                    DropdownMenu(
                        expanded = techDropdownExpanded,
                        onDismissRequest = { techDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin asignar") },
                            onClick = {
                                selectedTech = null
                                techDropdownExpanded = false
                            }
                        )
                        technicians.forEach { tech ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(tech.displayName ?: tech.email, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            tech.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedTech = tech
                                    techDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            errorMessage?.let {
                Surface(
                    shape = TicketShapes.sm,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TicketButton(
                text = "Crear Ticket",
                onClick = {
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        val dbPriority = when (priority.uppercase()) {
                            "BAJA", "LOW" -> "low"
                            "MEDIA", "MEDIUM" -> "medium"
                            "ALTA", "HIGH" -> "high"
                            "URGENTE", "URGENT" -> "urgent"
                            else -> "medium"
                        }

                        val ticket = Ticket(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            description = description.ifBlank { null },
                            status = "open",
                            priority = dbPriority,
                            category = category.ifBlank { null },
                            createdBy = user?.id ?: "",
                            createdAt = System.currentTimeMillis() / 1000,
                            updatedAt = System.currentTimeMillis() / 1000,
                            assignedTo = selectedTech?.id
                        )
                        val success = TiDBService.createTicket(ticket)

                        if (success) {
                            if (attachmentBytes != null) {
                                val uploadSuccess = TiDBService.uploadAttachment(
                                    ticketId = ticket.id,
                                    fileName = attachmentName,
                                    mimeType = attachmentMime,
                                    bytes = attachmentBytes!!
                                )
                                if (!uploadSuccess) {
                                    errorMessage = "Ticket creado, pero falló la subida del adjunto: ${TiDBService.lastError}"
                                }
                            }
                            isLoading = false
                            if (errorMessage == null) {
                                navController.popBackStack()
                            }
                        } else {
                            isLoading = false
                            errorMessage = TiDBService.lastError ?: "Error del servidor al crear el ticket"
                        }
                    }
                },
                loading = isLoading,
                enabled = title.isNotBlank() && user != null,
                type = ButtonType.Primary,
                size = ButtonSize.Large,
                icon = Icons.Filled.Add,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
