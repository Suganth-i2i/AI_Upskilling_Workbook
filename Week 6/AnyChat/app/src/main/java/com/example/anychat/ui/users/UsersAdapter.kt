package com.example.anychat.ui.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.anychat.R
import com.example.anychat.data.User
import java.text.SimpleDateFormat
import java.util.*

class UsersAdapter(private val onUserClick: (User) -> Unit) : ListAdapter<User, UsersAdapter.UserViewHolder>(UserDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view, onUserClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(itemView: View, private val onUserClick: (User) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val emailText: TextView = itemView.findViewById(R.id.textEmail)
        private val statusDot: View = itemView.findViewById(R.id.statusDot)
        private val avatar: ImageView = itemView.findViewById(R.id.imageAvatar)
        private val lastMessageText: TextView? = itemView.findViewById(R.id.textLastMessage)
        private val lastMessageTime: TextView? = itemView.findViewById(R.id.textLastMessageTime)
        private val unreadBadge: TextView? = itemView.findViewById(R.id.textUnreadBadge)
        private var currentUser: User? = null

        init {
            itemView.setOnClickListener {
                currentUser?.let { onUserClick(it) }
            }
        }

        fun bind(user: User) {
            currentUser = user
            emailText.text = user.email
            statusDot.setBackgroundResource(
                if (user.online) R.drawable.status_dot_online else R.drawable.status_dot_offline
            )
            lastMessageText?.text = user.lastMessage
            lastMessageTime?.text = if (user.lastMessageTimestamp > 0)
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(user.lastMessageTimestamp))
                else ""
            if (user.unreadCount > 0) {
                unreadBadge?.visibility = View.VISIBLE
                unreadBadge?.text = user.unreadCount.toString()
            } else {
                unreadBadge?.visibility = View.GONE
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean = oldItem.uid == newItem.uid
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
    }
} 