package com.example.anychat.ui.chat

import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.anychat.data.ChatRepository
import com.example.anychat.data.Message
import com.example.anychat.databinding.ActivityChatBinding
import com.example.anychat.viewmodel.ChatViewModel
import com.example.anychat.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ChatViewModel(ChatRepository()) as T
            }
        }
    }
    private lateinit var adapter: ChatAdapter
    private lateinit var chatId: String
    private lateinit var receiverId: String
    private lateinit var currentUserId: String
    private val chatRepository = ChatRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Session check: if not logged in, go to LoginActivity
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        receiverId = intent.getStringExtra("receiverId") ?: ""
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        chatId = chatRepository.getChatId(currentUserId, receiverId)

        adapter = ChatAdapter(currentUserId)
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewChat.adapter = adapter

        // Listen for messages in real-time
        viewModel.getMessagesQuery(chatId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Toast.makeText(this, "Error loading messages", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            val messages = snapshot?.toObjects<Message>() ?: emptyList()
            adapter.submitList(messages)
            binding.recyclerViewChat.scrollToPosition(messages.size - 1)
        }

        binding.buttonSend.setOnClickListener {
            val text = binding.editTextMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                val message = Message(
                    senderId = currentUserId,
                    receiverId = receiverId,
                    text = text,
                    timestamp = System.currentTimeMillis(),
                    read = false
                )
                Log.d("SendDebug", "Sending message: senderId=$currentUserId, receiverId=$receiverId, text=$text, read=false")
                CoroutineScope(Dispatchers.IO).launch {
                    chatRepository.sendMessageAndUpdateRecentChats(
                        chatId = chatId,
                        message = message,
                        senderId = currentUserId,
                        receiverId = receiverId
                    )
                }
                binding.editTextMessage.setText("")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reset unread count for this chat
        chatRepository.resetUnreadCount(currentUserId, chatId)
    }
} 