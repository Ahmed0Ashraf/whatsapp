package com.innovators.whatsapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        regsterbtn.setOnClickListener {
           performRegister()
        }
        selectUserPhotoBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)

        }
        alreadyhaveAccTextView.setOnClickListener{
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }



    }

    var selectedUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data!= null){

            selectedUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedUri)
            val bitmapDrawable = BitmapDrawable(bitmap)
            selectUserPhotoBtn.setBackgroundDrawable(bitmapDrawable)
        }
    }
    fun uploadImageToFirebase(){
        if (selectedUri == null){
            saveUserToFireDatabase("")
        }else{

            val fileName = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$fileName")
            ref.putFile(selectedUri!!).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    saveUserToFireDatabase(it.toString())
                }
            }
        }


    }
    fun saveUserToFireDatabase(imageUrl:String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid,usernameeditText.text.toString(),imageUrl)
        ref.setValue(user)
            .addOnSuccessListener {

                val intent = Intent(this,LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }


    }
    fun performRegister(){
        val email = emaileditText.text.toString()
        val password = passwordeditText.text.toString()
        // Initialize Firebase Auth
        //var auth = FirebaseAuth.getInstance()
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if (!it.isSuccessful)
                    return@addOnCompleteListener
                else{
                    uploadImageToFirebase()
                    println(it.result!!.user!!.uid)

                }
            }
    }
}
