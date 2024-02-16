package com.example.instaclone.auth

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.instaclone.DestinationScreens
import com.example.instaclone.IgViewModel
import com.example.instaclone.R
import com.example.instaclone.main.CheckSignedIn
import com.example.instaclone.main.CommonProgressSpinner
import com.example.instaclone.main.navigateTo

@Composable
fun SignUpScreen(
    navController: NavController, vm: IgViewModel
){
    val activity = LocalContext.current as Activity
    BackHandler {
        activity.finish()
    }
    CheckSignedIn(navController = navController, vm = vm)

    val focus = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize()){
        Column(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .verticalScroll(
                rememberScrollState()
            ), horizontalAlignment = Alignment.CenterHorizontally) {

            val usernameState = vm.usernameState
            val emailState = vm.emailState
            val passwordState = vm.passwordState

            Image(painter = painterResource(id = R.drawable.ig_logo),
                contentDescription = null,
                modifier = Modifier
                    .width(250.dp)
                    .padding(top = 16.dp)
                    .padding(8.dp))
            Text(text = "Sign Up",
                modifier = Modifier.padding(8.dp),
                fontSize = 30.sp,
                fontFamily = FontFamily.SansSerif)
            OutlinedTextField(value = usernameState.value,
                onValueChange = {usernameState.value = it},
                modifier = Modifier.padding(8.dp),
                label = { Text(text = "Username")},maxLines = 1)
            OutlinedTextField(value = emailState.value,
                onValueChange = {emailState.value = it},
                modifier = Modifier.padding(8.dp),
                label = { Text(text = "Email")},maxLines = 1)
            OutlinedTextField(value = passwordState.value,
                onValueChange = {passwordState.value = it},
                modifier = Modifier.padding(8.dp),
                label = { Text(text = "Password")},maxLines = 1,
                visualTransformation = PasswordVisualTransformation())
            Button(onClick = {
                            focus.clearFocus(force = true)
                             vm.onSignUp(usernameState.value,
                                         emailState.value,
                                         passwordState.value)
                             },
                modifier = Modifier.padding(8.dp)) {
                Text(text = "Sign Up")
            }
            Text(text = "Already a user? Go to Login->",
                color = Color.Blue,
                modifier = Modifier.padding(8.dp)
                    .clickable { navigateTo(navController,DestinationScreens.Login) })
        }
        val isLoading = vm.inProgress.value
        if (isLoading){
            CommonProgressSpinner()
        }
    }
}