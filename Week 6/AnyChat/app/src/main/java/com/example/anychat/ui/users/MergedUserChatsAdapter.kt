package com.example.anychat.ui.users

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.anychat.R
import com.example.anychat.data.MergedUserChat
import java.text.SimpleDateFormat
import java.util.*

class MergedUserChatsAdapter(
    private val onUserClick: (MergedUserChat) -> Unit
) : ListAdapter<MergedUserChat, MergedUserChatsAdapter.MergedUserChatViewHolder>(MergedUserChatDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MergedUserChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_chat, parent, false)
        return MergedUserChatViewHolder(view, onUserClick)
    }

    override fun onBindViewHolder(holder: MergedUserChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MergedUserChatViewHolder(itemView: View, private val onUserClick: (MergedUserChat) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val otherUserText: TextView = itemView.findViewById(R.id.textOtherUser)
        private val lastMessageText: TextView = itemView.findViewById(R.id.textLastMessage)
        private val lastMessageTime: TextView = itemView.findViewById(R.id.textLastMessageTime)
        private val unreadBadge: TextView = itemView.findViewById(R.id.textUnreadBadge)
        private val statusDot: View = itemView.findViewById(R.id.statusDot)
        private val avatar: ImageView = itemView.findViewById(R.id.imageAvatar)
        private var currentUser: MergedUserChat? = null

        init {
            itemView.setOnClickListener {
                currentUser?.let { onUserClick(it) }
            }
        }

        fun bind(chat: MergedUserChat) {
            currentUser = chat
            otherUserText.text = chat.email
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
            // Show online dot
            statusDot.visibility = if (chat.online) View.VISIBLE else View.GONE
            // Set avatar as first letter of email
            val initial = chat.email.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            avatar.setImageDrawable(textAsCircleDrawable(initial))
        }

        private fun textAsCircleDrawable(text: String): BitmapDrawable {
            val size = 48
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint()
            paint.isAntiAlias = true
            paint.color = Color.parseColor("#1976D2")
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
            paint.color = Color.WHITE
            paint.textSize = 28f
            paint.textAlign = Paint.Align.CENTER
            val y = size / 2f - (paint.descent() + paint.ascent()) / 2
            canvas.drawText(text, size / 2f, y, paint)
            return BitmapDrawable(itemView.resources, bitmap)
        }
    }

    class MergedUserChatDiffCallback : DiffUtil.ItemCallback<MergedUserChat>() {
        override fun areItemsTheSame(oldItem: MergedUserChat, newItem: MergedUserChat): Boolean = oldItem.uid == newItem.uid
        override fun areContentsTheSame(oldItem: MergedUserChat, newItem: MergedUserChat): Boolean = oldItem == newItem
    }
} 