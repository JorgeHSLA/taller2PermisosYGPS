package com.example.taller2permisosygps.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SelectorScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Selecciona una opción" , fontSize = 26.sp)
        Spacer(modifier = Modifier.padding(15.dp))
        Button(onClick = { navController.navigate(Screen.ParteCam.route ) },
            colors = ButtonColors(containerColor = Color.Black,
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.Black)) {
                Text(text = "Camara")

        }
        Spacer(modifier = Modifier.padding(10.dp))
        Button(onClick = { navController.navigate(Screen.ParteMap.route) },
            colors = ButtonColors(containerColor = Color.Black,
            contentColor = Color.White,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.Black)) {
                Text(text = "Mapa")
        }

    }
}
