package com.vanjor.cevrideshare

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.vanjor.cevrideshare.Utility.isValidEmail
import com.vanjor.cevrideshare.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private val TAG = "LoginActivity"
    private lateinit var mBinding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        val view = mBinding.root
        setContentView(view)
        setErrorListeners()
    }

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }

    fun setErrorListeners() {
        mBinding.emailEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    mBinding.emailTextField.error = "Email is required."
                } else if (!s.isValidEmail()) {
                    mBinding.emailTextField.error = "Please enter a valid email address"
                } else {
                    mBinding.emailTextField.error = null;
                }

            }
        })

        mBinding.passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    mBinding.passwordTextField.error = "Password is required."
                } else {
                    mBinding.passwordTextField.error = null;
                }
            }
        })
    }


    fun requestLogin(v: View) {
        // validate text fields
        if (mBinding.emailTextField.error != null || mBinding.passwordTextField.error != null) {
            return
        }

        if (mBinding.emailEditText.text == null || mBinding.emailEditText.text!!.isEmpty()) {
            mBinding.emailTextField.error = "Email is required"
            return
        }

        if (mBinding.passwordEditText.text == null || mBinding.passwordEditText.text!!.isEmpty()) {
            mBinding.passwordTextField.error = "Password is required"
            return
        }

        // send login request
        val queue = Volley.newRequestQueue(this)

        val vehicleStatusReqUrl = "http://192.168.0.13:25680/userprofile/login/" + mBinding.emailEditText.text + "/" + mBinding.passwordEditText.text
        val vehicleStatusRequest = JsonObjectRequest(
            Request.Method.GET, vehicleStatusReqUrl, null,
            { response ->
                Log.d(TAG, response.toString())
                Utility.currentUserId = response.getLong("userId")

                val sharedPref = getPreferences(Context.MODE_PRIVATE);
                if (sharedPref != null) {
                    with(sharedPref.edit()) {
                        putLong(getString(R.string.saved_user_id), Utility.currentUserId!!)
                        apply()
                    }
                }

                val intent = Intent()
                intent.setClass(this, MapsActivity::class.java)
                startActivity(intent)

                finish()
            },
            { error ->
                Snackbar.make(mBinding.root, "Incorrect login credentials", Snackbar.LENGTH_SHORT)
                    .setAnchorView(mBinding.backButton)
                    .show()
            }
        )
        queue.add(vehicleStatusRequest)

    }

    fun quit(v: View) {
     finish()
    }

    fun launchSignUp(v: View) {
        val intent = Intent()
        intent.setClass(this, SignUpActivity::class.java)
        startActivity(intent)

        finish()
    }

}