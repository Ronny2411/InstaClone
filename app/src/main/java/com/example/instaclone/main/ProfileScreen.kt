package com.example.instaclone.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instaclone.DestinationScreens
import com.example.instaclone.IgViewModel

@Composable
fun ProfileScreen(navController: NavController, vm: IgViewModel){
    val isLoading = vm.inProgress.value
    if (isLoading){
        CommonProgressSpinner()
    } else {
        val userData = vm.userData.value
        val name = remember {
            mutableStateOf(userData?.name ?: "")
        }
        val username = remember {
            mutableStateOf(userData?.username ?: "")
        }
        val bio = remember {
            mutableStateOf(userData?.bio ?: "")
        }
        ProfileContent(
            vm = vm,
            name = name.value,
            username = username.value,
            bio = bio.value,
            onNameChanged = { name.value = it },
            onUsernameChanged = { username.value = it },
            onBioChanged = { bio.value = it },
            onSave = { vm.updateProfileData(name.value,username.value,bio.value)},
            onBack = { navigateTo(navController,DestinationScreens.MyPost) },
            onLogout = { vm.onLogout()
                        navigateTo(navController,DestinationScreens.Login)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    vm: IgViewModel,
    username: String,
    name: String,
    bio: String,
    onNameChanged:(String)->Unit,
    onUsernameChanged:(String)->Unit,
    onBioChanged:(String)->Unit,
    onSave:()->Unit,
    onBack:()->Unit,
    onLogout:()->Unit
){
    Column(modifier = Modifier
        .verticalScroll(rememberScrollState())) {

        Row(modifier = Modifier.fillMaxWidth().height(40.dp).padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Cancel", modifier = Modifier.clickable {
                onBack.invoke()
            }, color = Color.Blue, fontWeight = FontWeight.Bold)
            Text(text = "Save", modifier = Modifier.clickable {
                onSave.invoke()
            }, color = Color.Blue, fontWeight = FontWeight.Bold)
        }

        Divider(color = Color.LightGray,
            thickness = 1.dp,
            modifier = Modifier
                .alpha(0.3f))

        //User Image
        Column(modifier = Modifier
            .height(200.dp)
            .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            if (vm.inProgress.value){
                CommonProgressSpinner()
            }
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()){uri: Uri? ->
                uri?.let {
                    vm.uploadProfileImage(uri)
                }
            }
                    UserImageCard(userImage = vm.userData.value?.imageUrl, modifier = Modifier
                        .padding(8.dp)
                        .size(150.dp)
                        .clickable { launcher.launch("image/*") })
                    Text(text = "Edit Picture",
                        modifier = Modifier.clickable { launcher.launch("image/*") },
                        color = Color.Blue)
        }

        Divider(color = Color.LightGray,
            thickness = 1.dp,
            modifier = Modifier
                .alpha(0.3f))

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Name: ", modifier = Modifier.width(100.dp))
            TextField(value = name, onValueChange = {onNameChanged(it)},singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent
                ))
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Username: ", modifier = Modifier.width(100.dp))
            TextField(value = username, onValueChange = {onUsernameChanged(it)},singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent
                ))
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.Top) {
            Text(text = "Bio: ", modifier = Modifier.width(100.dp))
            TextField(value = bio, onValueChange = {onBioChanged(it)},singleLine = false,
                modifier = Modifier.height(150.dp),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent
                ))
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.Center) {
            Text(text = "Logout", color = Color.Blue, modifier = Modifier.clickable { onLogout.invoke() })
        }
    }
}
