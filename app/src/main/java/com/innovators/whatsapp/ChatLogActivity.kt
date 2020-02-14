package com.innovators.whatsapp

import android.content.ClipData
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from.view.*
import kotlinx.android.synthetic.main.chat_to.view.*
import kotlinx.android.synthetic.main.user_layout_message.view.*

class ChatLogActivity : AppCompatActivity() {
    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        val user = intent.getParcelableExtra<User>("User")
        supportActionBar?.title = user.uid

        chatRecyclerView.adapter = adapter
        listenToMessgaes()

        sendbtn.setOnClickListener {
            performSendMessages()
        }



    }
    fun listenToMessgaes(){
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>("User")
        val toId = user.uid
        lateinit var ref : DatabaseReference
        if (fromId!! > toId){
            ref = FirebaseDatabase.getInstance().getReference("/user_messages/$fromId/$toId")
        }
        else{
            ref = FirebaseDatabase.getInstance().getReference("/user_messages/$toId/$fromId")
        }

        ref.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if(chatMessage!=null){
                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        adapter.add(ChatToItem(chatMessage.text,LatestMessagesActivity.currentUser!!))
                    }else{
                        val user = intent.getParcelableExtra<User>("User")

                        adapter.add(ChatFromItem(chatMessage.text,user))

                    }
                }


            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })
    }
fun performSendMessages(){
    val text = messagePlainText.text.toString()
    val fromId = FirebaseAuth.getInstance().uid
    val user = intent.getParcelableExtra<User>("User")

    val toId = user.uid
   // val ref = FirebaseDatabase.getInstance().getReference("/messages").push()
    if (fromId!! > toId){
        val ref = FirebaseDatabase.getInstance().getReference("/user_messages/$fromId/$toId").push()
        val chatMessage = ChatMessage(ref.key!!,text,fromId!!,toId,System.currentTimeMillis()/1000)
        ref.setValue(chatMessage).addOnSuccessListener {
        }
    }else{
        val ref = FirebaseDatabase.getInstance().getReference("/user_messages/$toId/$fromId").push()
        val chatMessage = ChatMessage(ref.key!!,text,fromId!!,toId,System.currentTimeMillis()/1000)
        ref.setValue(chatMessage).addOnSuccessListener {
        }
    }

    val latestRef = FirebaseDatabase.getInstance().getReference("/latest_messages")
    latestRef.addChildEventListener(object: ChildEventListener{
        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
        }

        override fun onChildAdded(p0: DataSnapshot, p1: String?) {
            val chatMessage = p0.getValue(ChatMessage::class.java)

            if(chatMessage!=null){
                if((chatMessage.fromId == FirebaseAuth.getInstance().uid && chatMessage.toId == toId)||(chatMessage.fromId == toId && chatMessage.toId == FirebaseAuth.getInstance().uid)){

                    latestRef.child(chatMessage.id).removeValue()
                    return@onChildAdded
                }
            }


        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }

    })
   val ref = FirebaseDatabase.getInstance().getReference("/latest_messages").push()
    val latestChatMessage = ChatMessage(ref.key!!,text,fromId!!,toId,System.currentTimeMillis()/1000)
    ref.setValue(latestChatMessage).addOnSuccessListener {
    }


}

}
class ChatToItem(val text : String,val user:User): com.xwray.groupie.kotlinandroidextensions.Item(){

    override fun getLayout() = R.layout.chat_to

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.chatToTextview.text = text

        val uri = user.imageUrl
        val targetImageView = viewHolder.itemView.chatToimageView
        Picasso.get().load(uri).into(targetImageView)
    }

}
class ChatFromItem(val text : String,val user:User): com.xwray.groupie.kotlinandroidextensions.Item(){

    override fun getLayout() = R.layout.chat_from

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.chatFromTextview.text = text
        val uri = user.imageUrl
        val targetImageView = viewHolder.itemView.chatFromimageView
        Picasso.get().load(uri).into(targetImageView)
    }

}