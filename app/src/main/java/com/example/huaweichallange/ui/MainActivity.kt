package com.example.huaweichallange.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
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
        signOut = findViewById<View>(R.id.signOutButton) as Button
        signOut.visibility = View.INVISIBLE

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
                val email = result.user?.email
                Toast.makeText(this, "You Logged with:$email", Toast.LENGTH_LONG).show()
                val intent = Intent(this, TabbedActivity::class.java)
                startActivity(intent)

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

        val Object = UserInfoModel()
        val acct = GoogleSignIn.getLastSignedInAccount(this)
        if (acct != null) {
            Object.personName = acct.displayName
            Object.personGivenName = acct.givenName
            Object.familyName = acct.familyName
            Object.personEmail = acct.email
            Object.personId = acct.id
            Object.personPhoto = acct.photoUrl.toString()
        }
        callbackManager!!.onActivityResult(requestCode, resultCode, data)

        val intent = Intent(this, TabbedActivity::class.java)
        intent.putExtra("extra", Object as Serializable)
        startActivity(intent)


        //getUserInfo()

    }

    /*private fun getUserInfo() {
        val acct = GoogleSignIn.getLastSignedInAccount(getActivity())
        if (acct != null) {
            val personName = acct.displayName
            val personGivenName = acct.givenName
            val personFamilyName = acct.familyName
            val personEmail = acct.email
            val personId = acct.id
            val personPhoto: Uri? = acct.photoUrl
        }
    }*/

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                updateUI(account)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val dispTxt = findViewById<View>(R.id.statusTextView) as TextView
        dispTxt.text = account.displayName
        signOut.visibility = View.VISIBLE
        signOut.setOnClickListener { view: View? ->
            mGoogleSignInClient.signOut().addOnCompleteListener { task: Task<Void> ->
                dispTxt.text = ""
                signOut.visibility = View.INVISIBLE
            }
        }
    }


}
