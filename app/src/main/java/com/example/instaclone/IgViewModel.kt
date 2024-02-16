package com.example.instaclone

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instaclone.data.ChatData
import com.example.instaclone.data.ChatUser
import com.example.instaclone.data.CommentData
import com.example.instaclone.data.Event
import com.example.instaclone.data.Message
import com.example.instaclone.data.NotificationBody
import com.example.instaclone.data.PostData
import com.example.instaclone.data.SendMessageDto
import com.example.instaclone.data.Status
import com.example.instaclone.data.UserData
import com.example.instaclone.util.BASE_URL
import com.example.instaclone.util.CHATS
import com.example.instaclone.util.COMMENTS
import com.example.instaclone.util.MESSAGES
import com.example.instaclone.util.POSTS
import com.example.instaclone.util.STATUS
import com.example.instaclone.util.USERS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import java.io.IOException
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class IgViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage,
) : ViewModel() {

    val isSignIn = mutableStateOf(false)
    val inProgress = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)
    val popUpNotification = mutableStateOf<Event<String>?>(null)

    val chats = mutableStateOf<List<ChatData>>(listOf())
    val inProgressChats = mutableStateOf(false)

    val chatMessages = mutableStateOf(listOf<Message>())
    val inProgressChatMessages = mutableStateOf(false)
    var currentChatMessagesListener: ListenerRegistration? = null

    val status = mutableStateOf<List<Status>>(listOf())
    val inProgressStatus = mutableStateOf(false)

    val usernameState = mutableStateOf("")
    val emailState = mutableStateOf("")
    val passwordState = mutableStateOf("")

    val refreshPostsProgress = mutableStateOf(false)
    val posts = mutableStateOf<List<PostData>>(listOf())

    val currentScreen =  mutableStateOf<DestinationScreens>(DestinationScreens.Feed)

    var searchedPosts = mutableStateOf<List<PostData>>(listOf())
    val searchedPostsProgress = mutableStateOf(false)
    val searchTerm = mutableStateOf("")

    val postsFeed = mutableStateOf<List<PostData>>(listOf())
    val postsFeedProgress = mutableStateOf(false)

    val comments = mutableStateOf<List<CommentData>>(listOf())
    val commentsProgress = mutableStateOf(false)

    val followers = mutableStateOf(0)

    init {
        //auth.signOut()
        val currentUser = auth.currentUser
        isSignIn.value = currentUser != null
        currentUser?.uid?.let {uid->
            getUserData(uid)
        }
    }

    fun onSignUp(username: String, email: String, password: String){
        if (username.isEmpty() or email.isEmpty() or password.isEmpty()){
            handleException(customMessage = "Please fill all fields")
            return
        }
        inProgress.value = true

        db.collection(USERS).whereEqualTo("username",username).get()
            .addOnSuccessListener {documents->
                if (documents.size()>0){
                    handleException(customMessage = "Username already exists! Please use a different Username")
                    inProgress.value = false
                } else {
                    auth.createUserWithEmailAndPassword(email,password)
                        .addOnCompleteListener {task->
                            if (task.isSuccessful){
                                isSignIn.value = true
                                createOrUpdateProfile(username = username)
                            } else {
                                handleException(task.exception,"SignUp Failed!!")
                            }
                            inProgress.value = false
                        }
                }

            }
            .addOnFailureListener {
                handleException(exception = it)
                inProgress.value = false
            }
    }

    fun onLogin(email: String, password: String){
        if (email.isEmpty() or password.isEmpty()){
            handleException(customMessage = "Please fill all fields")
            return
        }
        inProgress.value = true

        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener {task->
                if (task.isSuccessful){
                    isSignIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let {
                        getUserData(it)
                    }
                } else {
                    handleException(task.exception,"Login Failed!")
                    inProgress.value = false
                }
            }
            .addOnFailureListener {
                handleException(it,"Login Failed!")
                inProgress.value = false
            }
    }

    private fun createOrUpdateProfile(
        name: String? = null,
        username: String? = null,
        bio: String? = null,
        imageUrl: String? = null,
        fcmToken: String? = null,
    ){
        val uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name= name ?: userData.value?.name,
            username= username ?: userData.value?.username,
            imageUrl= imageUrl ?: userData.value?.imageUrl,
            bio= bio ?: userData.value?.bio,
            following = userData.value?.following,
            fcmToken = fcmToken ?: userData.value?.fcmToken
        )

        uid?.let {uid->
            inProgress.value = true
            db.collection(USERS).document(uid).get().addOnSuccessListener {
                if (it.exists()){
                    it.reference.update(userData.toMap())
                        .addOnSuccessListener {
                            this.userData.value = userData
                            inProgress.value = false
                            handleException(customMessage = "Profile Updated Successfully")
                        }
                        .addOnFailureListener {
                            handleException(it,"Cannot update user")
                            inProgress.value = false
                        }
                } else {
                    db.collection(USERS).document(uid).set(userData)
                    getUserData(uid)
                    inProgress.value = false
                }
            }.addOnFailureListener {
                handleException(it,"Cannot create user")
                inProgress.value = false
            }
        }
    }

    private fun getUserData(uid: String){
        inProgress.value = true
        db.collection(USERS).document(uid).get()
            .addOnSuccessListener {
                val user = it.toObject<UserData>()
                userData.value = user
                inProgress.value = false
                refreshPosts()
                getPersonalizedFeed()
                getFollowers(user?.userId)
                populateChats()
                populateStatuses()
                setFCMToken()
            }
            .addOnFailureListener {
                handleException(it,"Cannot retrieve user data!")
                inProgress.value = false
            }
    }

    fun handleException(exception: Exception? = null, customMessage: String = ""){
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isEmpty()) errorMsg else "$customMessage $errorMsg"
        popUpNotification.value = Event(message)
    }

    fun updateProfileData(name: String, username: String, bio: String){
        createOrUpdateProfile(name, username, bio)
    }

    private fun uploadImage(uri: Uri, onSuccess:(Uri)->Unit){
        inProgress.value = true

        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)

        uploadTask.addOnSuccessListener {
            val result = it.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener(onSuccess)
            inProgress.value = false
        }
            .addOnFailureListener {
                handleException(exception = it)
                inProgress.value = false
            }
    }

    fun uploadProfileImage(uri: Uri){
        uploadImage(uri){
            createOrUpdateProfile(imageUrl = it.toString())
            updatePostUserImageData(imageUri = it.toString())
        }
    }

    private fun updatePostUserImageData(imageUri: String){
        val currentUid = auth.currentUser?.uid
        db.collection(POSTS).whereEqualTo("userId",currentUid).get()
            .addOnSuccessListener {
                val posts = mutableStateOf<List<PostData>>(arrayListOf())
                convertPosts(it,posts)
                val refs = arrayListOf<DocumentReference>()
                for (post in posts.value){
                    post.postId?.let {id->
                        refs.add(db.collection(POSTS).document(id))
                    }
                }
                if (refs.isNotEmpty()){
                    db.runBatch{batch->
                        for (ref in refs){
                            batch.update(ref,"userImage",imageUri)
                        }
                    }.addOnSuccessListener {
                        refreshPosts()
                    }
                }
            }
    }

    fun onLogout(){
        auth.signOut()
        isSignIn.value=false
        userData.value=null
        popUpNotification.value = Event("Logged Out")
        currentScreen.value = DestinationScreens.Login
        searchedPosts.value = listOf()
        postsFeed.value = listOf()
    }

    fun onNewPost(uri: Uri, description: String, onPostSuccess:()->Unit){
        uploadImage(uri){
            onCreatePost(it,description,onPostSuccess)
        }
    }

    private fun onCreatePost(imageUri: Uri, description: String, onPostSuccess:()->Unit){
        inProgress.value = true
        val currentUid = auth.currentUser?.uid
        val currentUsername = userData.value?.username
        val currentUserImage = userData.value?.imageUrl

        if (currentUid != null){
            val postUuid = UUID.randomUUID().toString()
            val fillerWords = listOf("the","be","to","is","of","and","or","a","in","it")
            val searchTerms = description.plus(" "+currentUsername)
                .split(" ",".",",","?","!","#","@","$","&","*","(",")",";","/",":")
                .map { it.lowercase() }
                .filter { it.isNotEmpty() and !fillerWords.contains(it) }
            val post = PostData(
                postId = postUuid,
                userId = currentUid,
                username = currentUsername,
                userImage = currentUserImage,
                postImage = imageUri.toString(),
                postDescription = description,
                time = System.currentTimeMillis(),
                likes = listOf<String>(),
                searchTerms = searchTerms
            )
            db.collection(POSTS).document(postUuid).set(post)
                .addOnSuccessListener {
                    popUpNotification.value = Event("Post created successfully")
                    inProgress.value = false
                    refreshPosts()
                    onPostSuccess.invoke()
                }
                .addOnFailureListener {
                    handleException(it,customMessage = "Unable to create Post")
                    inProgress.value = false
                }
        } else {
            handleException(customMessage = "Error: username unavailable. Unable to create post")
            onLogout()
            inProgress.value = false
        }
    }

    private fun refreshPosts(){
        val currentUid = auth.currentUser?.uid
        if (currentUid != null){
            refreshPostsProgress.value = true
            db.collection(POSTS).whereEqualTo("userId",currentUid).get()
                .addOnSuccessListener {documents->
                    convertPosts(documents,posts)
                    refreshPostsProgress.value = false
                }
                .addOnFailureListener {
                    handleException(it,customMessage = "Cannot refresh posts")
                    refreshPostsProgress.value = false
                }
        } else {
            handleException(customMessage = "Error: username unavailable. Unable to refresh posts")
            onLogout()
        }
    }

    private fun convertPosts(documents: QuerySnapshot, outState: MutableState<List<PostData>>){
        val newPosts = mutableListOf<PostData>()
        documents.forEach{doc->
            val post = doc.toObject<PostData>()
            newPosts.add(post)
        }
        val sortedPosts = newPosts.sortedByDescending { it.time }
        outState.value = sortedPosts
    }

    fun searchPosts(searchTerm: String){
        if (searchTerm.isNotEmpty()){
            searchedPostsProgress.value = true
            db.collection(POSTS).whereArrayContains("searchTerms",searchTerm.trim().lowercase() )
                .get()
                .addOnSuccessListener {
                    convertPosts(it,searchedPosts)
                    searchedPostsProgress.value = false
                }
                .addOnFailureListener {
                    handleException(it,"Cannot Search Posts!")
                    searchedPostsProgress.value = false
                }
        } else {
            searchedPosts.value = listOf()
        }
    }

    fun onFollowClick(userId: String){
        auth.currentUser?.uid?.let {currentUser->
            val following = arrayListOf<String>()
            userData.value?.following?.let {
                following.addAll(it)
            }
            if (following.contains(userId)){
                following.remove(userId)
            } else {
                following.add(userId)
            }
            db.collection(USERS).document(currentUser).update("following",following)
                .addOnSuccessListener {
                    getUserData(currentUser)
                }
        }
    }

    private fun getPersonalizedFeed(){
        val following = userData.value?.following
        if (!following.isNullOrEmpty()){
            postsFeedProgress.value = true
            db.collection(POSTS).whereIn("userId",following).get()
                .addOnSuccessListener {
                    convertPosts(it,postsFeed)
                    if(postsFeed.value.isEmpty()){
                        getGeneralFeed()
                    } else {
                        postsFeedProgress.value = false
                    }
                }
                .addOnFailureListener {
                    handleException(it,"Cannot get personalized feed")
                    postsFeedProgress.value = false
                }
        } else {
            getGeneralFeed()
        }
    }

    private fun getGeneralFeed(){
        postsFeedProgress.value = true
        val currentTime = System.currentTimeMillis()
        val difference = 24 * 60 * 60 * 1000 //1 day in millis
        db.collection(POSTS).whereGreaterThan("time",currentTime - difference).get()
            .addOnSuccessListener {
                convertPosts(it,postsFeed)
                postsFeedProgress.value = false
            }
            .addOnFailureListener {
                handleException(it,"Cannot get feed")
                postsFeedProgress.value = false
            }
    }

    fun onLikePost(postData: PostData){
        auth.currentUser?.uid?.let {userId->
            postData.likes?.let {likes->
                val newLikes = arrayListOf<String>()
                if (likes.contains(userId)){
                    newLikes.addAll(likes.filter { userId != it })
                } else {
                    newLikes.addAll(likes)
                    newLikes.add(userId)
                }
                postData.postId?.let {postId->
                    db.collection(POSTS).document(postId).update("likes",newLikes)
                        .addOnSuccessListener {
                            postData.likes = newLikes
                        }
                        .addOnFailureListener {
                            handleException(it,"Unable to like post")
                        }
                }
            }
        }
    }

    fun createComment(postId: String, text: String){
        userData.value?.username?.let {username->
            val commentId = UUID.randomUUID().toString()
            val comment = CommentData(
                commentId = commentId,
                postId = postId,
                username = username,
                text = text,
                timeStamp = System.currentTimeMillis()
            )
            db.collection(COMMENTS).document(commentId).set(comment)
                .addOnSuccessListener {
                    getComment(postId)
                }
                .addOnFailureListener {
                    handleException(it,"Cannot create comment")
                }
        }
    }

    fun getComment(postId: String?){
        commentsProgress.value = true
        db.collection(COMMENTS).whereEqualTo("postId",postId).get()
            .addOnSuccessListener {documents->
                val newComments = mutableListOf<CommentData>()
                documents.forEach {doc->
                    val comment = doc.toObject<CommentData>()
                    newComments.add(comment)
                }
                val sortedComments = newComments.sortedByDescending { it.timeStamp }
                comments.value = sortedComments
                commentsProgress.value = false
            }
            .addOnFailureListener {
                handleException(it,"Cannot fetch comments")
                commentsProgress.value = false
            }
    }

    private fun getFollowers(uid: String?){
        db.collection(USERS).whereArrayContains("following",uid?:"").get()
            .addOnSuccessListener {
                followers.value = it.size()
            }
            .addOnFailureListener {
                handleException(it)
            }
    }

    fun onAddChat(username: String){
        if (username.isEmpty()){
            handleException(customMessage = "Please Enter User Id")
        } else {
            db.collection(CHATS)
                .where(
                    Filter.or(
                        Filter.and(
                            Filter.equalTo("userOne.username", username),
                            Filter.equalTo("userTwo.username",userData.value?.username)
                        ),
                        Filter.and(
                            Filter.equalTo("userOne.username",userData.value?.username),
                            Filter.equalTo("userTwo.username", username)
                        )
                    )
                )
                .get()
                .addOnSuccessListener {
                    if (it.isEmpty){
                        db.collection(USERS).whereEqualTo("username",username)
                            .get()
                            .addOnSuccessListener {
                                if (it.isEmpty){
                                    handleException(customMessage = "Cannot Retrieve user")
                                } else {
                                    val chatPartner = it.toObjects<UserData>()[0]
                                    val id = db.collection(CHATS).document().id
                                    val chat = ChatData(
                                        id,
                                        ChatUser(
                                            userData.value?.userId,
                                            userData.value?.username,
                                            userData.value?.name,
                                            userData.value?.imageUrl
                                        ),
                                        ChatUser(
                                            chatPartner.userId,
                                            chatPartner.username,
                                            chatPartner.name,
                                            chatPartner.imageUrl
                                        )
                                    )
                                    db.collection(CHATS).document(id).set(chat)
                                }
                            }
                            .addOnFailureListener {
                                handleException(it)
                            }
                    } else {
                        handleException(customMessage = "Chat Already Exists!")
                    }
                }
        }
    }

    private fun populateChats(){
        inProgressChats.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("userOne.username",userData.value?.username),
                Filter.equalTo("userTwo.username",userData.value?.username)
            )
        )
            .addSnapshotListener{ value, error ->
                if (error!=null){
                    handleException(error)
                }
                if (value!=null){
                    chats.value = value.documents.mapNotNull { it.toObject<ChatData>() }
                    inProgressChats.value = false
                }
            }
    }

    fun onSendReply(chatId: String, message: String){
        val time = Calendar.getInstance().time.toString()
        val msg = Message(userData.value?.username,message,time)
        db.collection(CHATS)
            .document(chatId)
            .collection(MESSAGES)
            .document()
            .set(msg)
        sendMessage(chatId,message)
    }

    fun populateChat(chatId: String){
        inProgressChatMessages.value = true
        currentChatMessagesListener = db.collection(CHATS)
            .document(chatId)
            .collection(MESSAGES)
            .addSnapshotListener{value, error->
                if (error!=null){
                    handleException(error)
                }
                if(value!=null){
                    chatMessages.value =
                        value.documents
                            .mapNotNull { it.toObject<Message>() }
                            .sortedBy { it.timestamp }
                    inProgressChatMessages.value = false
                }
            }
    }

    fun depopulateChat(){
        chatMessages.value = listOf()
        currentChatMessagesListener = null
    }

    private fun createStatus(imageUrl: String){
        val newStatus = Status(
            ChatUser(
                userData.value?.userId,
                userData.value?.username,
                userData.value?.name,
                userData.value?.imageUrl
            ),
            imageUrl,
            System.currentTimeMillis()
        )
        db.collection(STATUS).document().set(newStatus)
    }

    fun uploadStatus(imageUri: Uri){
        uploadImage(imageUri){
            createStatus(it.toString())
        }
    }

    private fun populateStatuses(){
        inProgressStatus.value = true
        val millisTimeDelta = 24L * 60 * 60 * 1000
        val cutoff = System.currentTimeMillis() - millisTimeDelta

        val following = userData.value?.following?.plus(userData.value?.userId)
        if (!following.isNullOrEmpty()) {
            db.collection(STATUS)
                .whereGreaterThan("timeStamp",cutoff)
                .whereIn("user.userId", following)
                .addSnapshotListener{ value, error ->
                    if (error!=null){
                        handleException(error)
                    }
                    if (value!=null){
                        status.value = value.toObjects()
                        inProgressStatus.value = false
                    }
                }
        }
    }

    private fun setFCMToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task->
            if (task.isSuccessful){
                val token = task.result
                Log.d("token", token)
                createOrUpdateProfile(fcmToken = token)
            }
        }
    }



    fun sendMessage(chatId: String, message: String) {

        val api: FCMApi = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()

        var token : String? = null

        val chat = db.collection(CHATS).document(chatId).get()

        chat.addOnSuccessListener {
            val chatData = it.toObject<ChatData>()
            var userTwoUserId = chatData?.userTwo?.userId
            if (userData.value?.userId == chatData?.userTwo?.userId){
                userTwoUserId = chatData?.userOne?.userId
            }
            if (userTwoUserId != null) {
                db.collection(USERS).document(userTwoUserId).get()
                    .addOnSuccessListener {
                        val user = it.toObject<UserData>()
                        token = user?.fcmToken
                        viewModelScope.launch {
                            val messageDto = SendMessageDto(
                                to = token,
                                notification = NotificationBody(
                                    title = userData.value?.username ?: "New Message!",
                                    body = message
                                )
                            )
                            try {
                                api.sendMessage(messageDto)
                            } catch(e: HttpException) {
                                e.printStackTrace()
                            } catch(e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
            }
        }
    }

}

