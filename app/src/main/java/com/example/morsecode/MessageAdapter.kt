package com.example.morsecode

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.morsecode.databinding.TemplateMessageBinding

class MessageAdapter: RecyclerView.Adapter<MessageAdapter.MessageHolder>() {
    private val messageList = ArrayList<Message>()
    class MessageHolder(view: View): RecyclerView.ViewHolder(view) {
        private val binding = TemplateMessageBinding.bind(view)
        fun bind(message: Message){
            val params = binding.messageText.layoutParams as ConstraintLayout.LayoutParams
            binding.messageText.text = message.text

            if (message.type == "send"){
                params.endToEnd = binding.messageConstraint.id
                params.startToStart = ConstraintLayout.LayoutParams.UNSET
                binding.messageText.setBackgroundResource(R.drawable.main_act_shape_send_message)
            } else if (message.type == "receive"){
                params.startToStart = binding.messageConstraint.id
                params.endToEnd = ConstraintLayout.LayoutParams.UNSET
                binding.messageText.setBackgroundResource(R.drawable.main_act_shape_rec_message)
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.template_message, parent,
            false)
        return MessageHolder(view)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {

        val message = messageList[position]
        holder.bind(message)
    }

    fun addNewMessage(message: Message){
        messageList.add(message)
        notifyDataSetChanged()
    }

}