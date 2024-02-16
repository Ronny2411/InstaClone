package com.example.instaclone.main

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.instaclone.DestinationScreens
import com.example.instaclone.IgViewModel
import com.example.instaclone.R
import com.example.instaclone.data.PostData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SinglePostScreen(navController: NavController, vm: IgViewModel, post: PostData){
    LaunchedEffect(key1 = Unit){
        vm.getComment(post.postId)
    }
    val comments = vm.comments.value
    post.userId?.let {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Back", modifier = Modifier.clickable {
                    navController.popBackStack()
                }, color = Color.Blue, fontWeight = FontWeight.Bold)
            }

            Divider(color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier
                    .alpha(0.3f))

            SinglePostDisplay(navController = navController, vm = vm, post = post, nComments = comments.size)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun SinglePostDisplay(navController: NavController, vm: IgViewModel, post: PostData, nComments: Int){
    val userData = vm.userData.value
    val modalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Card(shape = CircleShape, modifier = Modifier
                    .padding(8.dp)
                    .size(32.dp)) {
                    Image(painter = rememberAsyncImagePainter(model = post.userImage), contentDescription = null)
                }
                Text(text = post.username ?: "")
            }
            if (userData?.userId == post.userId){
                //Current user's post. Show nothing
            } else if (userData?.following?.contains(post.userId) == true){
                Text(text = "Following", color = Color.Gray, modifier = Modifier
                    .clickable {
                        vm.onFollowClick(post.userId!!)
                    }
                    .padding(8.dp), fontWeight = FontWeight.Bold)
            } else {
                Text(text = "Follow", color = Color.Blue, modifier = Modifier
                    .clickable {
                        vm.onFollowClick(post.userId!!)
                    }
                    .padding(8.dp), fontWeight = FontWeight.Bold)
            }
        }
    }

    val likeAnimation = remember { mutableStateOf(false) }
    val unlikeAnimation = remember { mutableStateOf(false) }

    Box (modifier = Modifier
        .fillMaxWidth(),
        contentAlignment = Alignment.Center){
        val modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 150.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (post.likes?.contains(userData?.userId) == true) {
                            unlikeAnimation.value = true
                        } else {
                            likeAnimation.value = true
                        }
                        vm.onLikePost(post)
                    }
                )
            }
        CommonImage(data = post.postImage,
                    modifier = modifier,
                    contentScale = ContentScale.FillWidth)
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
        Image(painter = painterResource(id = if (post.likes?.contains(userData?.userId) == true) R.drawable.ic_like else R.drawable.ic_unlike),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .clickable {
                    if (post.likes?.contains(userData?.userId) == true) {
                        unlikeAnimation.value = true
                    } else {
                        likeAnimation.value = true
                    }
                    vm.onLikePost(post)
                },
            colorFilter = ColorFilter.tint(if (post.likes?.contains(userData?.userId) == true) Color.Red else Color.Black))
        Spacer(modifier = Modifier.width(8.dp))
        Image(painter = painterResource(id = R.drawable.ic_comments),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .clickable { post.postId?.let {
                    navController.navigate(DestinationScreens.Comment.createRoute(it))
                } })
    }
    Text(text = "${post.likes?.size ?: 0} likes", modifier = Modifier.padding(8.dp))

    Row(modifier = Modifier.padding(8.dp)) {
        Text(text = post.username ?: "", fontWeight = FontWeight.Bold)
        Text(text = post.postDescription ?: "", modifier = Modifier.padding(start=8.dp))
    }
    Row(modifier = Modifier.padding(8.dp)) {
        Text(text = "$nComments comments", modifier = Modifier
            .padding(start = 8.dp)
            .clickable { post.postId?.let {
                navController.navigate(DestinationScreens.Comment.createRoute(it))
            } }, color = Color.Gray)
    }
}