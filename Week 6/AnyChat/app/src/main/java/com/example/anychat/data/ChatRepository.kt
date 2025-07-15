package com.example.anychat.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getChatId(user1: String, user2: String): String =
        if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"

    suspend fun sendMessageAndUpdateRecentChats(
        chatId: String,
        message: Message,
        senderId: String,
        receiverId: String
    ) {
        val db = firestore
        val messageRef = db.collection("chats").document(chatId).collection("messages").document()
        val batch = db.batch()
        batch.set(messageRef, message)

        // Update sender's recentChats
        val senderRecentRef = db.collection("users").document(senderId)
            .collection("recentChats").document(chatId)
        batch.set(senderRecentRef, mapOf(
            "chatId" to chatId,
            "otherUserId" to receiverId,
            "lastMessage" to message.text,
            "lastMessageTimestamp" to message.timestamp,
            "unreadCount" to 0
        ), SetOptions.merge())

        // Update receiver's recentChats (increment unreadCount)
        val receiverRecentRef = db.collection("users").document(receiverId)
            .collection("recentChats").document(chatId)
        batch.set(receiverRecentRef, mapOf(
            "chatId" to chatId,
            "otherUserId" to senderId,
            "lastMessage" to message.text,
            "lastMessageTimestamp" to message.timestamp,
            "unreadCount" to FieldValue.increment(1)
        ), SetOptions.merge())

        batch.commit().await()
    }

    fun getMessagesQuery(chatId: String): Query =
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)

    fun listenForRecentChats(userId: String, onUpdate: (List<RecentChat>) -> Unit) =
        firestore.collection("users").document(userId)
            .collection("recentChats")
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val chats = snapshot.documents.mapNotNull { it.toObject(RecentChat::class.java) }
                onUpdate(chats)
            }

    fun resetUnreadCount(userId: String, chatId: String) {
        firestore.collection("users").document(userId)
            .collection("recentChats").document(chatId)
            .update("unreadCount", 0)
    }

    // New: Merge all users with recent chats for the user list
    fun listenForMergedUserChats(currentUid: String, onUpdate: (List<MergedUserChat>) -> Unit) {
        val usersRef = firestore.collection("users")
        val recentChatsRef = firestore.collection("users").document(currentUid).collection("recentChats")
        usersRef.addSnapshotListener { usersSnapshot, _ ->
            if (usersSnapshot == null) {
                onUpdate(emptyList())
                return@addSnapshotListener
            }
            val allUsers = usersSnapshot.documents.mapNotNull { it.toObject(User::class.java) }
                .filter { it.uid != currentUid }
            recentChatsRef.addSnapshotListener { chatsSnapshot, _ ->
                val recentChats = chatsSnapshot?.documents?.mapNotNull { it.toObject(RecentChat::class.java) } ?: emptyList()
                val merged = allUsers.map { user ->
                    val chatId = getChatId(currentUid, user.uid)
                    val chat = recentChats.find { it.chatId == chatId }
                    MergedUserChat(
                        uid = user.uid,
                        email = user.email,
                        online = user.online,
                        lastMessage = chat?.lastMessage ?: "",
                        lastMessageTimestamp = chat?.lastMessageTimestamp ?: 0L,
                        unreadCount = chat?.unreadCount ?: 0
                    )
                }.sortedWith(compareByDescending<MergedUserChat> { it.lastMessageTimestamp > 0L }.thenByDescending { it.lastMessageTimestamp })
                onUpdate(merged)
            }
        }
    }
} 