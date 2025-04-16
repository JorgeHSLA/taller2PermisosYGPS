package com.example.taller2permisosygps.ui


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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.taller2permisosygps.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun CamaraScreen(navController: NavController) {
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
            CameraXScreen()
            //CamaraFoto()
        } else {
            //UiDeVideo()
        }
    }
}
// CameraXScreen.kt

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraXScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
    }

    if (!cameraPermissionState.status.isGranted) {
        Text("Se necesita permiso de la cámara para continuar.")
        return
    }

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
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    saveImageToGallery(context, bitmap)
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraXScreen", "Error al capturar la foto", exc)
                }
            }
        )
    }

    Column(modifier = Modifier.size(width = 250.dp, height = 450.dp),
        verticalArrangement = Arrangement.SpaceEvenly) { // Use a Column to manage vertical layout
        Spacer(modifier = Modifier.padding(15.dp))
        Box(
            modifier = Modifier
                .weight(1f) //
                .fillMaxWidth() //
                .paddingFromBaseline(30.dp)
        ) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.padding(15.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp), // Add some bottom padding to the Row
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Button(
                onClick = { takePhoto(cameraExecutor) },
            ) {
                Text(text = "Tomar Foto")
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
