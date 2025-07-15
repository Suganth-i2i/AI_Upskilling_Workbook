package com.example.anychat.ui.users

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.anychat.R
import com.example.anychat.data.ChatRepository
import com.example.anychat.data.MergedUserChat
import com.example.anychat.databinding.ActivityUsersListBinding
import com.example.anychat.viewmodel.MergedUserChatsViewModel
import com.example.anychat.ui.auth.LoginActivity
import com.example.anychat.ui.chat.ChatActivity
import com.google.firebase.auth.FirebaseAuth

class UsersListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUsersListBinding
    private val viewModel: MergedUserChatsViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return MergedUserChatsViewModel(ChatRepository()) as T
            }
        }
    }
    private lateinit var adapter: MergedUserChatsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Check user session
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        binding = ActivityUsersListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up app bar
        val toolbar = Toolbar(this)
        toolbar.title = "AnyChat"
        setSupportActionBar(toolbar)
        binding.root.addView(toolbar, 0)

        adapter = MergedUserChatsAdapter { userChat ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("receiverId", userChat.uid)
            startActivity(intent)
        }
        binding.recyclerViewUsers.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewUsers.adapter = adapter

        viewModel.mergedChats.observe(this) { chats ->
            adapter.submitList(chats)
            binding.progressBar.visibility = View.GONE
            if (chats.isEmpty()) {
                Toast.makeText(this, "No users found", Toast.LENGTH_SHORT).show()
            }
        }

        binding.progressBar.visibility = View.VISIBLE
        viewModel.listenForMergedUserChats(currentUser.uid)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_users_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 