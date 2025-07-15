package com.example.anychat.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.*

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val realtimeDb: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    private val usersCollection = firestore.collection("users")
    private val presenceRef = realtimeDb.getReference("presence")

    // Listen for users and their unread counts in real time
    fun listenForUsersWithLastMessage(currentUid: String, onUpdate: (List<User>) -> Unit): ListenerRegistration {
        Log.d("UnreadDebug", "listenForUsersWithLastMessage: currentUid=$currentUid")
        return usersCollection.addSnapshotListener { usersSnapshot, _ ->
            if (usersSnapshot == null) {
                onUpdate(emptyList())
                return@addSnapshotListener
            }
            val users = usersSnapshot.documents.mapNotNull { it.toObject(User::class.java) }
                .filter { it.uid != currentUid }

            if (users.isEmpty()) {
                onUpdate(emptyList())
                return@addSnapshotListener
            }

            val db = FirebaseFirestore.getInstance()
            val resultList = mutableListOf<User>()
            var processed = 0

            for (user in users) {
                val chatId = getChatId(currentUid, user.uid)
                Log.d("UnreadDebug", "Checking chatId=$chatId for user=${user.email}")
                db.collection("chats").document(chatId).collection("messages")
                    .addSnapshotListener { messagesSnapshot, _ ->
                        if (messagesSnapshot == null) {
                            processed++
                            if (processed == users.size) {
                                onUpdate(resultList.sortedByDescending { it.lastMessageTimestamp })
                            }
                            return@addSnapshotListener
                        }
                        val lastMessage = messagesSnapshot.documents
                            .maxByOrNull { it.getLong("timestamp") ?: 0L }
                            ?.toObject(Message::class.java)
                        val lastMessageText = lastMessage?.text ?: ""
                        val lastMessageTimestamp = lastMessage?.timestamp ?: 0L
                        var debugCount = 0
                        val unreadCount = messagesSnapshot.documents.count {
                            val read = it.getBoolean("read")
                            val receiver = it.getString("receiverId")
                            val logMsg = "Msg: id=${it.id}, receiverId=$receiver, read=$read, currentUid=$currentUid, chatId=$chatId"
                            Log.d("UnreadDebug", logMsg)
                            val isUnread = (read == null || read == false) && receiver == currentUid
                            if (isUnread) debugCount++
                            isUnread
                        }
                        Log.d("UnreadDebug", "User: ${user.email}, chatId: $chatId, unreadCount: $unreadCount, debugCount=$debugCount")
                        // Remove any previous entry for this user
                        resultList.removeAll { it.uid == user.uid }
                        resultList.add(
                            user.copy(
                                lastMessage = lastMessageText,
                                lastMessageTimestamp = lastMessageTimestamp,
                                unreadCount = unreadCount
                            )
                        )
                        processed++
                        if (processed == users.size) {
                            onUpdate(resultList.sortedByDescending { it.lastMessageTimestamp })
                        }
                    }
            }
        }
    }

    fun setUserOnline(uid: String) {
        presenceRef.child(uid).setValue(true)
    }

    fun setUserOffline(uid: String) {
        presenceRef.child(uid).setValue(false)
    }

    fun getPresenceRef(uid: String) = presenceRef.child(uid)

    private fun getChatId(user1: String, user2: String): String =
        if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
} 