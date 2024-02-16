package com.example.instaclone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.instaclone.auth.LoginScreen
import com.example.instaclone.auth.SignUpScreen
import com.example.instaclone.data.PostData
import com.example.instaclone.main.ChatListScreen
import com.example.instaclone.main.CommentScreen
import com.example.instaclone.main.FeedScreen
import com.example.instaclone.main.MyPostScreen
import com.example.instaclone.main.NewPostScreen
import com.example.instaclone.main.NotificationMessage
import com.example.instaclone.main.ProfileScreen
import com.example.instaclone.main.SearchScreen
import com.example.instaclone.main.SingleChatScreen
import com.example.instaclone.main.SinglePostScreen
import com.example.instaclone.main.StatusScreen
import com.example.instaclone.ui.theme.InstaCloneTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Route

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InstaCloneTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    InstaCloneApp()
                }
            }
        }
    }
}

sealed class DestinationScreens(val route: String){
    object SignUp: DestinationScreens("signup")
    object Login: DestinationScreens("login")
    object Feed: DestinationScreens("feed")
    object Search: DestinationScreens("search")
    object MyPost: DestinationScreens("mypost")
    object Profile: DestinationScreens("profile")
    object NewPost: DestinationScreens("newpost/{imageUri}") {
        fun createRoute(uri: String) = "newpost/$uri"
    }
    object SinglePost: DestinationScreens("singlepost")
    object Comment: DestinationScreens("comment/{postId}"){
        fun createRoute(postId: String) = "comment/$postId"
    }
    object ChatList: DestinationScreens("chatlist")
    object SingleChat: DestinationScreens("singlechat/{chatId}"){
        fun createRoute(chatId: String) = "singlechat/$chatId"
    }
    object Status: DestinationScreens("status/{userId}"){
        fun createRoute(userId: String?) = "status/$userId"
    }
}

inline fun <reified T> Bundle.getParcelableExtraCompat(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T?
}

@Composable
fun InstaCloneApp(){
    val vm = hiltViewModel<IgViewModel>()
    val navController = rememberNavController()
    
    NotificationMessage(vm = vm)
    
    NavHost(navController = navController, startDestination = DestinationScreens.SignUp.route){
        composable(DestinationScreens.SignUp.route){
            SignUpScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreens.Login.route){
            LoginScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreens.Feed.route){
            FeedScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreens.Search.route){
            SearchScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreens.MyPost.route){
            MyPostScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreens.Profile.route){
            ProfileScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreens.NewPost.route){navBackStackEntry ->
            val imageUri = navBackStackEntry.arguments?.getString("imageUri")
            imageUri?.let {
                NewPostScreen(navController = navController, vm = vm, encodedUri = it)
            }
        }
        composable(DestinationScreens.SinglePost.route){
            val postData = navController.previousBackStackEntry?.savedStateHandle?.get<PostData>("post")
            postData?.let {
                SinglePostScreen(navController = navController, vm = vm, post = postData)
            }
        }
        composable(DestinationScreens.Comment.route,
            enterTransition = {
                slideInVertically(initialOffsetY = {it}, animationSpec = tween(300))
            },
            exitTransition = {
                slideOutVertically(targetOffsetY = {it}, animationSpec = tween(300))
            }){navBackStackEntry ->
            val postId = navBackStackEntry.arguments?.getString("postId")
            postId?.let {
                CommentScreen(navController = navController, vm = vm, postId = it)
            }
        }
        composable(DestinationScreens.ChatList.route,
            enterTransition = {
                slideInHorizontally(initialOffsetX = {it}, animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = {it}, animationSpec = tween(300))
            }){
                ChatListScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreens.SingleChat.route,
            enterTransition = {
                slideInVertically(initialOffsetY = {it}, animationSpec = tween(300))
            },
            exitTransition = {
                slideOutVertically(targetOffsetY = {it}, animationSpec = tween(300))
            }){navBackStackEntry ->
            val chatId = navBackStackEntry.arguments?.getString("chatId")
            chatId?.let {
                SingleChatScreen(navController = navController, vm = vm, chatId = it)
            }
        }
        composable(DestinationScreens.Status.route,
            enterTransition = {
                slideInVertically(initialOffsetY = {it}, animationSpec = tween(300))
            },
            exitTransition = {
                slideOutVertically(targetOffsetY = {it}, animationSpec = tween(300))
            }){navBackStackEntry ->
            val userId = navBackStackEntry.arguments?.getString("userId")
            userId?.let {
                StatusScreen(navController = navController, vm = vm, userId = it)
            }
        }
    }
}

@Composable
@Preview
fun InstaCloneAppPreview(){
    InstaCloneApp()
}
