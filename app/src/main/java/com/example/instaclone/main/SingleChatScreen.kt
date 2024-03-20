package com.example.instaclone.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instaclone.IgViewModel
import com.example.instaclone.data.Message

@Composable
fun SingleChatScreen(navController: NavController, vm: IgViewModel, chatId: String) {
    LaunchedEffect(key1 = Unit) {
        vm.populateChat(chatId)
    }
    BackHandler {
        vm.depopulateChat()
        navController.popBackStack()
    }

    var reply by rememberSaveable { mutableStateOf("") }
    val currentChat = vm.chats.value.first { it.chatId == chatId }
    val myId = vm.userData.value
    val chatUser = if (myId?.username == currentChat.userOne.username) currentChat.userTwo
    else currentChat.userOne
    val onSendReply = {
        vm.onSendReply(chatId, reply)
        reply = ""
    }
    val chatMessages = vm.chatMessages

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat header
        ChatHeader(name = chatUser.name ?: "", imageUrl = chatUser.imageUrl ?: "") {
            navController.popBackStack()
            vm.depopulateChat()
        }

        // Messages
        Messages(
            modifier = Modifier.weight(1f),
            chatMessages = chatMessages.value,
            currentUser = myId?.username ?: ""
        )

        // Reply box
        ReplyBox(reply = reply, onReplyChange = { reply = it }, onSendReply = onSendReply)
    }
}

@Composable
fun ChatHeader(name: String, imageUrl: String, onBackClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.ArrowBack,
            contentDescription = null,
            modifier = Modifier
                .clickable { onBackClicked.invoke() }
                .padding(8.dp)
        )
        CommonImage(
            data = imageUrl,
            modifier = Modifier
                .padding(8.dp)
                .size(50.dp)
                .clip(CircleShape)
        )
        Text(
            text = name,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
    Divider(color = Color.LightGray,
        thickness = 1.dp,
        modifier = Modifier
            .alpha(0.3f))
}

@Composable
fun Messages(modifier: Modifier, chatMessages: List<Message>, currentUser: String) {
    LazyColumn(modifier = modifier, reverseLayout = true) {
        items(chatMessages) { msg ->
            msg.message?.let {
                val alignment = if (msg.sentBy == currentUser) Alignment.End
                else Alignment.Start
                val color = if (msg.sentBy == currentUser) Color(0xFF68C400)
                else Color(0xFFC0C0C0)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = alignment
                ) {
                    Text(
                        text = msg.message,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(color)
                            .padding(12.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplyBox(reply: String, onReplyChange: (String) -> Unit, onSendReply: () -> Unit) {
    Column{
        Divider(color = Color.LightGray,
            thickness = 1.dp,
            modifier = Modifier
                .alpha(0.3f))
        Row(modifier = Modifier
            .fillMaxWidth()) {
            TextField(value = reply,
                onValueChange = onReplyChange,
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
                        if (reply.isNotEmpty()){
                            onSendReply()
                        }
                    }
                ),
                maxLines = 3,
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        if (reply.isNotEmpty()){
                            onSendReply()
                        }
                    }) {
                        Icon(imageVector = Icons.Filled.Send, contentDescription = null)
                    }
                }
            )
        }
    }
}