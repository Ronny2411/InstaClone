package com.example.instaclone.main

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instaclone.IgViewModel
import com.example.instaclone.data.CommentData
import kotlin.Float.Companion.MAX_VALUE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentScreen(navController: NavController, vm: IgViewModel, postId: String){
    var commentText by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(key1 = true){
        vm.getComment(postId)
    }
    val commentProgress = vm.commentsProgress.value
    val comments = vm.comments.value
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)
        .verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Back", fontWeight = FontWeight.Bold, color = Color.Blue,
                modifier = Modifier.clickable { navController.popBackStack() })
            Text(text = "Comments", fontWeight = FontWeight.Bold)
            Text(text = "       ")
        }
        Divider(color = Color.LightGray,
            thickness = 1.dp,
            modifier = Modifier
                .alpha(0.3f)
                .padding(top = 8.dp))
        if (commentProgress){
            Column(modifier = Modifier
                .fillMaxSize()
                .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                CommonProgressSpinner()
            }
        } else if (comments.isEmpty()){
            Column(modifier = Modifier
                .fillMaxSize()
                .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "No comments available")
            }
        } else {
            LazyColumn(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .nestedScroll(
                    rememberNestedScrollInteropConnection()
                )){
                items(items = comments){comment->
                    Row(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)) {
                        Text(text = comment.username ?: "", fontWeight = FontWeight.Bold)
                        Text(text = comment.text ?: "", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
        Column{
            Divider(color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier
                    .alpha(0.3f))
            Row(modifier = Modifier
                .fillMaxWidth()) {
                TextField(value = commentText,
                    onValueChange = {commentText = it},
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray, CircleShape),
                    shape = CircleShape,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions (
                        onSend = {
                            if (commentText.isNotEmpty()){
                                vm.createComment(postId,commentText)
                                commentText = ""
                            }
                            focusManager.clearFocus()
                        }
                    ),
                    maxLines = 1,
                    singleLine = true,
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (commentText.isNotEmpty()){
                                vm.createComment(postId,commentText)
                                commentText = ""
                            }
                            focusManager.clearFocus()
                        }) {
                            Icon(imageVector = Icons.Filled.Send, contentDescription = null)
                        }
                    }
                )
            }
        }
    }
}