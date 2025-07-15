package com.example.anychat.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.anychat.R
import com.example.anychat.data.Message
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val currentUserId: String) : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {
    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (holder is SentMessageViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(message)
        }
    }

    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textMessage: TextView = itemView.findViewById(R.id.textMessage)
        private val textTime: TextView = itemView.findViewById(R.id.textTime)
        fun bind(message: Message) {
            textMessage.text = message.text
            textTime.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
        }
    }

    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textMessage: TextView = itemView.findViewById(R.id.textMessage)
        private val textTime: TextView = itemView.findViewById(R.id.textTime)
        fun bind(message: Message) {
            textMessage.text = message.text
            textTime.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean = oldItem.id == newItem.id && oldItem.timestamp == newItem.timestamp
        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean = oldItem == newItem
    }
} 