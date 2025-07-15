package com.example.anychat.ui.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.anychat.R
import com.example.anychat.data.RecentChat
import java.text.SimpleDateFormat
import java.util.*

class RecentChatsAdapter : ListAdapter<RecentChat, RecentChatsAdapter.RecentChatViewHolder>(RecentChatDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_chat, parent, false)
        return RecentChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RecentChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val otherUserText: TextView = itemView.findViewById(R.id.textOtherUser)
        private val lastMessageText: TextView = itemView.findViewById(R.id.textLastMessage)
        private val lastMessageTime: TextView = itemView.findViewById(R.id.textLastMessageTime)
        private val unreadBadge: TextView = itemView.findViewById(R.id.textUnreadBadge)

        fun bind(chat: RecentChat) {
            otherUserText.text = chat.otherUserId // You can fetch/display email if needed
            lastMessageText.text = chat.lastMessage
            lastMessageTime.text = if (chat.lastMessageTimestamp > 0)
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(chat.lastMessageTimestamp))
                else ""
            if (chat.unreadCount > 0) {
                unreadBadge.visibility = View.VISIBLE
                unreadBadge.text = chat.unreadCount.toString()
            } else {
                unreadBadge.visibility = View.GONE
            }
        }
    }

    class RecentChatDiffCallback : DiffUtil.ItemCallback<RecentChat>() {
        override fun areItemsTheSame(oldItem: RecentChat, newItem: RecentChat): Boolean = oldItem.chatId == newItem.chatId
        override fun areContentsTheSame(oldItem: RecentChat, newItem: RecentChat): Boolean = oldItem == newItem
    }
} 