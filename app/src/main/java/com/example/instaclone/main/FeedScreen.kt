package com.example.instaclone.main

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.instaclone.DestinationScreens
import com.example.instaclone.IgViewModel
import com.example.instaclone.R
import com.example.instaclone.data.NavParam
import com.example.instaclone.data.PostData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(navController: NavController, vm: IgViewModel){
    val activity = LocalContext.current as Activity
    BackHandler {
        activity.finish()
    }


    val userDataLoading = vm.inProgress.value
    val userData = vm.userData.value
    val postsFeedProgress = vm.postsFeedProgress.value
    val postsFeed = vm.postsFeed.value

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.LightGray)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White)
        ) {
            UserImageCard(userImage = userData?.imageUrl)
        }

        PostsList(
            posts = postsFeed,
            modifier = Modifier.weight(1f),
            loading = userDataLoading or postsFeedProgress,
            navController = navController,
            vm = vm,
            currentUserId = userData?.userId ?: ""
        )

        BottomNavigation(navController = navController, vm = vm)
    }

}

@Composable
fun PostsList(
    posts: List<PostData>,
    modifier: Modifier,
    loading: Boolean,
    navController: NavController,
    vm: IgViewModel,
    currentUserId: String
){
    Box(modifier = modifier) {
        LazyColumn{
            items(posts){
                Post(post = it,currentUserId = currentUserId,vm = vm,navController = navController){
                    navigateTo(navController,
                        DestinationScreens.SinglePost,
                        NavParam("post",it))
                }
            }
        }
        if (loading){
            CommonProgressSpinner()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun Post(post: PostData, currentUserId: String, vm: IgViewModel, navController: NavController, onPostClick:()->Unit){

    val likeAnimation = remember { mutableStateOf(false) }
    val unlikeAnimation = remember { mutableStateOf(false) }

    Card (
        shape = RoundedCornerShape(corner = CornerSize(4.dp)),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 4.dp, bottom = 4.dp)
    ){
        Column {
            Row(modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clickable { onPostClick.invoke() },
                verticalAlignment = Alignment.CenterVertically) {
                Card(shape = CircleShape, modifier = Modifier
                    .padding(8.dp)
                    .size(32.dp)) {
                    CommonImage(data = post.userImage, contentScale = ContentScale.FillWidth)
                }
                Text(text = post.username ?: "", modifier = Modifier.padding(4.dp))
            }
            Box(modifier = Modifier
                .fillMaxWidth(),
                contentAlignment = Alignment.Center) {
                val modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 150.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (post.likes?.contains(currentUserId) == true) {
                                    unlikeAnimation.value = true
                                } else {
                                    likeAnimation.value = true
                                }
                                vm.onLikePost(post)
                            },
                            onTap = {
                                onPostClick.invoke()
                            }
                        )
                    }
                CommonImage(
                    data = post.postImage,
                    modifier = modifier,
                    contentScale = ContentScale.FillWidth
                )
                if (likeAnimation.value){
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(1000L)
                        likeAnimation.value = false
                    }
                    LikeAnimation()
                }
                if (unlikeAnimation.value){
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(1000L)
                        unlikeAnimation.value = false
                    }
                    LikeAnimation(false)
                }
            }
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = if (post.likes?.contains(currentUserId) == true) R.drawable.ic_like else R.drawable.ic_unlike),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            if (post.likes?.contains(currentUserId) == true) {
                                unlikeAnimation.value = true
                            } else {
                                likeAnimation.value = true
                            }
                            vm.onLikePost(post)
                        },
                    colorFilter = ColorFilter.tint(if (post.likes?.contains(currentUserId) == true) Color.Red else Color.Black))
                Spacer(modifier = Modifier.width(8.dp))
                Image(painter = painterResource(id = R.drawable.ic_comments),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            post.postId?.let {
                                navController.navigate(DestinationScreens.Comment.createRoute(it))
                            }
                        })
            }
            Text(text = "${post.likes?.size ?: 0} likes", modifier = Modifier.padding(8.dp))

            Row(modifier = Modifier.padding(8.dp)) {
                Text(text = post.username ?: "", fontWeight = FontWeight.Bold)
                Text(text = post.postDescription ?: "", modifier = Modifier.padding(start=8.dp))
            }
        }
    }
}