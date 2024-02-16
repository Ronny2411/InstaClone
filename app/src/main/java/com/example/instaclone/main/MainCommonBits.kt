package com.example.instaclone.main

import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.instaclone.DestinationScreens
import com.example.instaclone.IgViewModel
import com.example.instaclone.R
import com.example.instaclone.data.NavParam
import com.example.instaclone.data.PostData

@Composable
fun NotificationMessage(vm : IgViewModel){
    val notifState = vm.popUpNotification.value
    val notifMessage = notifState?.getContentOrNull()
    if (notifMessage != null){
        Toast.makeText(LocalContext.current,notifMessage,Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun CommonProgressSpinner(){
    Row(
        modifier = Modifier
            .alpha(0.5f)
            .background(Color.LightGray)
            .clickable(enabled = false) { }
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

fun navigateTo(navController: NavController, dest: DestinationScreens, vararg params: NavParam){
    for (param in params){
        navController.currentBackStackEntry?.savedStateHandle?.set(key = param.name,value = param.value)
    }
    navController.navigate(dest.route){
        popUpTo(dest.route)
        launchSingleTop = true
    }
}

@Composable
fun CheckSignedIn(navController: NavController,vm: IgViewModel){
    val alreadySignedIn = remember { mutableStateOf(false) }
    val isSignIn = vm.isSignIn.value
    if (isSignIn && !alreadySignedIn.value){
        alreadySignedIn.value = true
        navController.navigate(DestinationScreens.Feed.route){
            popUpTo(0){}
            launchSingleTop = true
        }
    }
}

@Composable
fun BottomNavigation(navController: NavController,vm : IgViewModel){
        BottomAppBar(
            Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            val post = PostData()
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { navigateTo(navController,DestinationScreens.Feed,NavParam("post",post))
                    vm.currentScreen.value = DestinationScreens.Feed}) {
                    if (vm.currentScreen.value == DestinationScreens.Feed) {
                        Icon(imageVector = Icons.Filled.Home, contentDescription = null)
                    } else {
                        Icon(imageVector = Icons.Outlined.Home, contentDescription = null)
                    }
                }
                IconButton(onClick = { navigateTo(navController,DestinationScreens.Search,NavParam("post",post))
                    vm.currentScreen.value = DestinationScreens.Search}) {
                    if (vm.currentScreen.value == DestinationScreens.Search) {
                        Icon(painter = painterResource(id = R.drawable.icons8_search__1_), contentDescription = null)
                    } else {
                        Icon(painter = painterResource(id = R.drawable.icons8_search), contentDescription = null)
                    }
                }
                IconButton(onClick = { navigateTo(navController,DestinationScreens.MyPost,NavParam("post",post))
                    vm.currentScreen.value = DestinationScreens.MyPost}) {
                    if (vm.currentScreen.value == DestinationScreens.MyPost) {
                        Icon(imageVector = Icons.Filled.AccountCircle, contentDescription = null)
                    } else {
                        Icon(imageVector = Icons.Outlined.AccountCircle, contentDescription = null)
                    }
                }
            }
        }
}

@Composable
fun CommonImage(
    data : String?,
    modifier: Modifier = Modifier.wrapContentSize(),
    contentScale: ContentScale = ContentScale.Crop
){
    val painter = rememberAsyncImagePainter(model = data)
    val state = painter.state
    Image(painter =painter,
        contentDescription =null,
        modifier = modifier,
        contentScale = contentScale
        )
    if (state is AsyncImagePainter.State.Loading){
        CommonProgressSpinner()
    }
}

@Composable
fun UserImageCard(
    userImage: String?,
    modifier: Modifier = Modifier
        .padding(8.dp)
        .size(64.dp)
){
    Card(shape = CircleShape, modifier = modifier) {
        if (userImage.isNullOrEmpty()){
            Image(painter = painterResource(id = R.drawable.default_profile_pic),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.Gray))
        } else {
            CommonImage(data = userImage)
        }
    }
}

private enum class LikeIconSize{
    SMALL,
    LARGE
}

@Composable
fun LikeAnimation(like: Boolean = true){
    var sizeState by remember { mutableStateOf(LikeIconSize.SMALL) }
    val transition = updateTransition(targetState = sizeState,label = "")
    val size by transition.animateDp(
        label = "",
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        }
    ) {state ->  
        when(state){
            LikeIconSize.SMALL -> 0.dp
            LikeIconSize.LARGE -> 150.dp
        }
    }
    
    Image(painter = painterResource(id = if (like) R.drawable.ic_like else R.drawable.ic_unlike),
        contentDescription = null,
        modifier = Modifier.size(size),
        colorFilter = ColorFilter.tint(if (like) Color.Red else Color.Gray))

    sizeState = LikeIconSize.LARGE
}

@Composable
fun CommonRow(imageUrl: String?, name: String?, onItemClick: ()-> Unit){
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(75.dp)
        .clickable { onItemClick.invoke() },
        verticalAlignment = Alignment.CenterVertically) {
        
        CommonImage(data = imageUrl,
            modifier = Modifier
                .padding(8.dp)
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.Red)
        )
        Text(text = name ?: "---",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp))
    }
}

@Composable
fun TitleText(txt: String){
    Text(text = txt,
        fontWeight = FontWeight.Bold,
        fontSize = 35.sp,
        modifier = Modifier.padding(8.dp))
}
