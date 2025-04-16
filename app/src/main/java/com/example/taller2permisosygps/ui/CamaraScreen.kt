package com.example.taller2permisosygps.ui

import androidx.camera.video.Recording
import androidx.camera.video.Recorder
import androidx.camera.video.VideoRecordEvent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.Locale
import java.util.concurrent.Executor
import android.Manifest
import android.content.Context
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.net.Uri
import android.util.Log
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.VideoCapture
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.accompanist.permissions.PermissionState
import java.io.File
import java.text.SimpleDateFormat

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CamaraScreen(navController: NavController) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
        audioPermissionState.launchPermissionRequest()
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Camara")

        Spacer(modifier = Modifier.padding(15.dp))
        var checked by remember { mutableStateOf(true) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "  video")

            Switch(
                checked = checked,
                onCheckedChange = {
                    checked = it
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                )
            )
            Text(text = "foto  ")
        }
        Spacer(modifier = Modifier.padding(15.dp))

        if (checked) {
            CameraFoto(cameraPermissionState )
        } else {
            CamaraVideo(cameraPermissionState, audioPermissionState)
        }
    }
}

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////

                                        //CAMARA

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraFoto(cameraPermissionState: PermissionState) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current




    if (!cameraPermissionState.status.isGranted) {
        Text("Se necesita permiso de la cámara para continuar.")
        return
    }


    // Estado para almacenar la imagen capturada
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { ContextCompat.getMainExecutor(context) }

    LaunchedEffect(previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraXScreen", "Error al inicializar CameraX", e)
            }
        }, cameraExecutor)
    }
    LaunchedEffect(capturedImage) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraXScreen", "Error al inicializar CameraX", e)
            }
        }, cameraExecutor)
    }
    fun takePhoto(executor: Executor) {
        val photoFile = File.createTempFile(
            "photo_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}",
            ".jpg",
            context.externalCacheDir
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    capturedImage = BitmapFactory.decodeFile(photoFile.absolutePath)
                    saveImageToGallery(context, capturedImage!!) //se usa !! porque capturedImage puede ser nulo, y al poner esto obligara a que haya algo
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraXScreen", "Error al capturar la foto", exc)
                }
            }
        )
    }


    Column(
        modifier = Modifier.size(width = 250.dp, height = 450.dp),
        verticalArrangement = Arrangement.SpaceBetween) { // Use a Column to manage vertical layout
        Spacer(modifier = Modifier.padding(15.dp))
        if(capturedImage != null){
            Image(
                bitmap = capturedImage!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            )
        }else{
            Box(
                modifier = Modifier
                    .weight(1f) //
                    .fillMaxSize()//
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.padding(35.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp), // Add some bottom padding to the Row
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            if (capturedImage == null) {
                Button(
                    onClick = { takePhoto(cameraExecutor) },
                ) {
                    Text(text = "Tomar Foto")
                }
            }else{

                Button(
                    onClick = { capturedImage = null },
                ) {
                    Text(text = "volver a tomar")
                }
            }

        }
    }
}

// Función para guardar la imagen en la galería
private fun saveImageToGallery(context: Context, bitmap: Bitmap) {
    val imageUri = MediaStore.Images.Media.insertImage(
        context.contentResolver,
        bitmap,
        "Photo_${System.currentTimeMillis()}",
        "Foto tomada con CameraX"
    )

    if (imageUri != null) {
        Log.d("CameraXScreen", "Imagen guardada en la galería: $imageUri")
    } else {
        Log.e("CameraXScreen", "Error al guardar la imagen en la galería")
    }
}

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////

                                //VIDEO

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CamaraVideo(cameraPermissionState: PermissionState, audioPermissionState: PermissionState) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    if (!cameraPermissionState.status.isGranted || !audioPermissionState.status.isGranted) {
        Text("Se necesitan permisos de cámara y audio para continuar.")
        return
    }

    // Creamos el PreviewView para mostrar el preview de la cámara.
    val previewView = remember { PreviewView(context) }
    var recordedVideoUri: Uri? by remember { mutableStateOf(null) }


    // Configuramos el use-case de VideoCapture.
    val recorder = Recorder.Builder()
        .setQualitySelector(QualitySelector.from(Quality.HD))
        .build()
    val videoCapture = VideoCapture.withOutput(recorder)

    val cameraExecutor = remember { ContextCompat.getMainExecutor(context) }
    // Inicializamos la cámara con preview y videoCapture.
    LaunchedEffect(previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture // Aquí añadimos el use-case para video.
                )
            } catch (e: Exception) {
                Log.e("CameraXVideoScreen", "Error al inicializar CameraX", e)
            }
        }, cameraExecutor)
    }

    // Variable para almacenar el recording actual.
    var recording: Recording? by remember { mutableStateOf(null) }
    //toma el video
    fun takeVideo(executor: Executor) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            .format(System.currentTimeMillis())
        // Creamos un archivo temporal para guardar el video con extensión .mp4.
        val videoFile = File.createTempFile(
            "video_$timeStamp",
            ".mp4",
            context.externalCacheDir
        )
        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        // Preparamos y comenzamos la grabación.
        recording = videoCapture.output
            .prepareRecording(context, outputOptions)
            .apply {
                // Habilitamos audio si se tiene permiso.
                if (audioPermissionState.status.isGranted) {
                    withAudioEnabled()
                }
            }
            .start(executor) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        Log.d("CameraXVideoScreen", "Inicio de la grabación")
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!event.hasError()) {
                            val videoUri = event.outputResults.outputUri
                            Log.d("CameraXVideoScreen", "Grabación finalizada: $videoUri")
                            // se guarda emn la galeria
                            // Guardar el video en la galería.
                            saveVideoToGallery(context, videoUri)

                            // Actualizar el estado para mostrar el video.
                            recordedVideoUri = videoUri

                        } else {
                            Log.e("CameraXVideoScreen", "Error en la grabación: ${event.error}")
                        }
                    }
                }
            }
    }
    // UI
    Column(
        modifier = Modifier.size(width = 250.dp, height = 450.dp),
        verticalArrangement = Arrangement.SpaceBetween) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            if (recording != null) {
                Button(onClick = {
                    // Al detener, se termina la grabación.
                    recording?.stop()
                    recording = null
                }) {
                    Text("Detener grabación")
                }
            } else {
                Button(onClick = {
                    takeVideo(cameraExecutor)
                }
                ) {
                    Text("Grabar Video")
                }
            }
        }
    }
}

private fun saveVideoToGallery(context: Context, videoUri: Uri) {
    val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "Video_${System.currentTimeMillis()}.mp4")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
        }

        val insertedUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
        insertedUri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                resolver.openInputStream(videoUri)?.copyTo(outputStream)
            }
            Log.d("CameraXVideoScreen", "Video saved to gallery: $it")
        } ?: run {
            Log.e("CameraXVideoScreen", "Failed to insert video into MediaStore")
        }
}