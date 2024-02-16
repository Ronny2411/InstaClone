package com.example.instaclone.main

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.instaclone.IgViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPostScreen(navController: NavController, vm: IgViewModel, encodedUri: String){

    val imageUri by remember{ mutableStateOf(encodedUri) }
    var description by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier
        .verticalScroll(rememberScrollState())
        .fillMaxWidth()) {

        Row(modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Cancel", modifier = Modifier.clickable {
                navController.popBackStack()
            }, color = Color.Blue, fontWeight = FontWeight.Bold)
            Text(text = "Post", modifier = Modifier.clickable {
                focusManager.clearFocus()
                vm.onNewPost(Uri.parse(imageUri),description){ navController.popBackStack() }
            }, color = Color.Blue, fontWeight = FontWeight.Bold)
        }

        Divider(color = Color.LightGray,
            thickness = 1.dp,
            modifier = Modifier
                .alpha(0.3f))
        
        Image(painter = rememberAsyncImagePainter(model = imageUri),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 150.dp),
            contentScale = ContentScale.FillWidth)

        Row (modifier = Modifier.padding(16.dp)){
            OutlinedTextField(value = description,
                onValueChange = {description = it},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                label = { Text(text = "Description")},
                singleLine = false,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent
                ))
        }
    }
    if (vm.inProgress.value){
        CommonProgressSpinner()
    }
}

