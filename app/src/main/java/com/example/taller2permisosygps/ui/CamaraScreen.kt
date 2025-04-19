
package com.example.taller2permisosygps.ui

import androidx.camera.core.Preview
import androidx.media3.common.MediaItem
import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.error
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor


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
fun CamaraVideo(
    cameraPermissionState: PermissionState,
    audioPermissionState: PermissionState
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    if (!cameraPermissionState.status.isGranted || !audioPermissionState.status.isGranted) {
        Text("Se necesitan permisos de cámara y audio para continuar.")
        return
    }

    val previewView = remember { PreviewView(context) }
    val cameraExecutor = remember { ContextCompat.getMainExecutor(context) }
    val recorder = Recorder.Builder()
        .setQualitySelector(QualitySelector.from(Quality.HD))
        .build()
    val videoCapture = VideoCapture.withOutput(recorder)

    // Estado del provider
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    // 1) vincula solo el preview
    fun bindPreview() {
        cameraProvider?.unbindAll()
        val preview = Preview.Builder()
            .build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }
        cameraProvider?.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview
        )
    }

    // 2) vincula Preview + Video
    fun bindPreviewAndVideo() {
        cameraProvider?.unbindAll()
        val preview = Preview.Builder()
            .build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }
        cameraProvider?.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            videoCapture
        )
    }

    LaunchedEffect(Unit) {
        ProcessCameraProvider.getInstance(context).also { future ->
            future.addListener({
                cameraProvider = future.get()
                bindPreview()
            }, cameraExecutor)
        }
    }

    var isRecording by remember { mutableStateOf(false) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var recordedVideoUri by remember { mutableStateOf<Uri?>(null) }

    Column(
        modifier = Modifier.size(width = 250.dp, height = 450.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (recordedVideoUri != null && !isRecording) {  //video ya grabado
            VideoPlayer(uri = recordedVideoUri!!)
        } else {
            AndroidView(                                 //preview de la camara para grabar
                factory = { previewView },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                if (isRecording) {
                    activeRecording?.stop()
                } else {
                    recordedVideoUri = null
                    bindPreviewAndVideo()
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                        .format(System.currentTimeMillis())
                    val videoFile = File.createTempFile(
                        "video_$timestamp",
                        ".mp4",
                        context.externalCacheDir
                    )
                    val options = FileOutputOptions.Builder(videoFile).build()
                    val recording = videoCapture.output
                        .prepareRecording(context, options)
                        .apply { if (audioPermissionState.status.isGranted) withAudioEnabled() }
                        .start(cameraExecutor) { event ->
                            when (event) {
                                is VideoRecordEvent.Start -> isRecording = true
                                is VideoRecordEvent.Finalize -> {
                                    if (!event.hasError()) {
                                        recordedVideoUri = event.outputResults.outputUri
                                        saveVideoToGallery(context, recordedVideoUri!!)
                                    }
                                    isRecording = false
                                    activeRecording = null
                                    bindPreview()          // se termina la grabacion e inicia solo preview
                                }
                            }
                        }
                    activeRecording = recording
                }
            }) {
                Text(if (isRecording) "Detener grabación" else "Grabar Video")
            }
        }
    }
}

@Composable
fun VideoPlayer(uri: Uri) {
    val context = LocalContext.current
    val exoPlayer = remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
        }
    }
    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }
    AndroidView(
        factory = { ctx -> PlayerView(ctx).apply { player = exoPlayer; useController = true } },
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    )
}

private fun saveVideoToGallery(context: Context, videoUri: Uri) {
    val resolver = context.contentResolver
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "Video_${System.currentTimeMillis()}.mp4")
        put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
    }
    resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
        resolver.openOutputStream(uri)?.use { output ->
            resolver.openInputStream(videoUri)?.copyTo(output)
        }
        Log.d("CameraXVideo", "Saved: $uri")
    } ?: Log.e("CameraXVideo", "Failed to save video")
}
