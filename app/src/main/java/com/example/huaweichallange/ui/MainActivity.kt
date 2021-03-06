package com.example.huaweichallange.ui


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.huaweichallange.R
import com.example.huaweichallange.model.UserInfoModel
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import java.io.Serializable


class MainActivity : AppCompatActivity() {
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var gso: GoogleSignInOptions
    lateinit var signOut: Button
    val RC_SIGN_IN: Int = 1
    var firebaseAuth: FirebaseAuth? = null
    var callbackManager: CallbackManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val signIn = findViewById<View>(R.id.signInButton) as SignInButton
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)



        signIn.setOnClickListener { v: View? ->
            signInGoogle()
        }

        firebaseAuth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        login_button.setPermissions("email")
        login_button.setOnClickListener {
            fbSignIn()
        }


    }

    private fun fbSignIn() {
        login_button.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                handleFacebookAccessToken(result!!.accessToken)


            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException?) {

            }

        })
    }

    private fun handleFacebookAccessToken(accessToken: AccessToken?) {
        val credential = FacebookAuthProvider.getCredential(accessToken!!.token)
        firebaseAuth!!.signInWithCredential(credential)
            .addOnFailureListener { e ->
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }.addOnSuccessListener { result ->
                val userObject = UserInfoModel()
                userObject.personName = result.user?.displayName
                userObject.personPhoto = result.user?.photoUrl.toString()
                if (credential != null){
                    val intent = Intent(this, TabbedActivity::class.java)
                    intent.putExtra("extra", userObject as Serializable)
                    startActivity(intent)
                }else{
                    Toast.makeText(this,"Something went wrong with logging in!", Toast.LENGTH_LONG).show()
                }

            }
    }

    private fun signInGoogle() {
        val signIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signIntent, RC_SIGN_IN)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)

            handleResult(task)
        }

        val userObject = UserInfoModel()
        val acct = GoogleSignIn.getLastSignedInAccount(this)
        if (acct != null) {
            userObject.personName = acct.displayName
            userObject.personPhoto = acct.photoUrl.toString()
        }
        callbackManager!!.onActivityResult(requestCode, resultCode, data)

        val intent = Intent(this, TabbedActivity::class.java)
        intent.putExtra("extra", userObject as Serializable)
        startActivity(intent)




    }



    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                //updateUI(account)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {

        signOut.visibility = View.VISIBLE
        signOut.setOnClickListener { view: View? ->
            mGoogleSignInClient.signOut().addOnCompleteListener { task: Task<Void> ->

                signOut.visibility = View.INVISIBLE
            }
        }
    }


}
