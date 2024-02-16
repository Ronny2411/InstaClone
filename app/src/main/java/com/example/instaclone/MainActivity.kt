package com.example.instaclone

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.instaclone.auth.LoginScreen
import com.example.instaclone.auth.SignUpScreen
import com.example.instaclone.data.PostData
import com.example.instaclone.main.CommentScreen
import com.example.instaclone.main.FeedScreen
import com.example.instaclone.main.MyPostScreen
import com.example.instaclone.main.NewPostScreen
import com.example.instaclone.main.NotificationMessage
import com.example.instaclone.main.ProfileScreen
import com.example.instaclone.main.SearchScreen
import com.example.instaclone.main.SinglePostScreen
import com.example.instaclone.ui.theme.InstaCloneTheme
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
    }
}

@Composable
@Preview
fun InstaCloneAppPreview(){
    InstaCloneApp()
}
