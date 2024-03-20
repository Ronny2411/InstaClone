package com.example.instaclone.main

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instaclone.DestinationScreens
import com.example.instaclone.IgViewModel

@Composable
fun ChatListScreen(navController: NavController, vm: IgViewModel) {
    val inProgress = vm.inProgressChats.value
    if (inProgress)
        CommonProgressSpinner()
    else {
        val chats = vm.chats.value
        val userData = vm.userData.value

        val showDialog = remember { mutableStateOf(false) }
        val onFabClick: () -> Unit = { showDialog.value = true }
        val onDismiss: () -> Unit = { showDialog.value = false }
        val onAddChat: (String) -> Unit = {
            vm.onAddChat(it)
            showDialog.value = false
        }

        Scaffold(
            floatingActionButton = { FAB(showDialog.value, onFabClick, onDismiss, onAddChat) },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
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
                                .clickable { navController.popBackStack() }
                                .padding(8.dp)
                        )
                        TitleText(txt = "Chats")
                    }
                    Divider(color = Color.LightGray,
                        thickness = 1.dp,
                        modifier = Modifier
                            .alpha(0.3f))
                    if (chats.isEmpty())
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "No chats available")
                        }
                    else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(chats) { chat ->
                                val chatUser = if (chat.userOne.username == userData?.username) chat.userTwo
                                else chat.userOne
                                CommonRow(
                                    imageUrl = chatUser.imageUrl ?: "",
                                    name = chatUser.name ?: "---"
                                ) {
                                    chat.chatId?.let {id ->
                                        navController.navigate(
                                            DestinationScreens.SingleChat.createRoute(id)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun FAB(
    showDialog: Boolean,
    onFabClick: () -> Unit,
    onDismiss: () -> Unit,
    onAddChat: (String) -> Unit
) {

    val addChatNumber = remember { mutableStateOf("") }

    if (showDialog)
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(onClick = { onAddChat(addChatNumber.value) }) {
                    Text(text = "Add chat")
                }
            },
            title = { Text(text = "Add chat") },
            text = {
                OutlinedTextField(
                    value = addChatNumber.value,
                    onValueChange = { addChatNumber.value = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            }
        )

    FloatingActionButton(
        onClick = onFabClick,
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = Modifier.padding(bottom = 40.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Add chat",
            tint = Color.White,
        )
    }
}