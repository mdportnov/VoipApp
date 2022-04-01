package ru.mephi.voip.ui.catalog

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ru.mephi.voip.R

@Composable
fun CatalogScreen(navController: NavController) {
    val context = LocalContext.current

    BackHandler {
        navController.popBackStack()
    }

    Column {
        TopAppBar(
            backgroundColor = Color.White,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_mephi),
                        contentDescription = "лого",
                    )
                }

                Text(
                    text = "Каталог", style = TextStyle(color = Color.Black, fontSize = 20.sp),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}