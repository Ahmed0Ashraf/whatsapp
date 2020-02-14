package com.innovators.whatsapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder

import kotlinx.android.synthetic.main.activity_new_message.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.user_layout_message.view.*


class NewMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar?.title = "Select User"

fetchUsers()



    }
    fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()

                p0.children.forEach{
                    val user = it.getValue(User::class.java)
                    adapter.add( UserItem(user))

                    adapter.setOnItemClickListener{item, view ->
                        val userItem = item as UserItem
                        val intent = Intent(applicationContext,ChatLogActivity::class.java)
                        intent.putExtra("User",userItem.user)
                        startActivity(intent)
                        finish()
                    }
                }
                val llm = LinearLayoutManager(applicationContext)
                messagesRecyclerView.layoutManager = llm

                messagesRecyclerView.adapter = adapter
            }

        })
    }

}


class UserItem(val user:User?): Item(){

    override fun getLayout() = R.layout.user_layout_message

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.itemView.username_newmessage.text = user?.username
        if (user?.imageUrl != ""){
            Picasso.get().load(user?.imageUrl).into(viewHolder.itemView.userImage)
        }
    }

}
