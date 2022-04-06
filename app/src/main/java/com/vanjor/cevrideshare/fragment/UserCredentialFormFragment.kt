package com.vanjor.cevrideshare.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.vanjor.cevrideshare.R
import com.vanjor.cevrideshare.SignUpActivity
import com.vanjor.cevrideshare.Utility
import com.vanjor.cevrideshare.Utility.isValidEmail
import com.vanjor.cevrideshare.databinding.FragmentUserCredentialFormBinding
import com.vanjor.cevrideshare.viewmodel.SignUpViewModel
import org.json.JSONObject

class UserCredentialFormFragment : Fragment() {
    private val TAG = "SignUpActivity"
    private lateinit var mBinding: FragmentUserCredentialFormBinding
    private val mSignUpViewModel: SignUpViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentUserCredentialFormBinding.inflate(inflater, container, false)
        mBinding.apply { viewmodel = mSignUpViewModel }
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.signUpButton.setOnClickListener { validateCredentialAndSubmit() }

        mBinding.emailEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    mBinding.emailTextField.error = "Email is required"
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
                    mBinding.passwordTextField.error = "Password is required"
                } else {
                    mBinding.passwordTextField.error = null;
                    if (mBinding.confirmpasswordEditText.text != null &&
                                !s.toString().equals(mBinding.confirmpasswordEditText.text.toString())) {
                        mBinding.confirmPasswordTextField.error = "Passwords do not match"
                    } else {
                        mBinding.confirmPasswordTextField.error = null
                    }
                }
            }
        })

        mBinding.confirmpasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.d("TAG", s.toString() + ", " + mBinding.passwordEditText.text)
                if (s.isEmpty() || (mBinding.passwordEditText.text != null &&
                            !s.toString().equals(mBinding.passwordEditText.text.toString()))) {
                    mBinding.confirmPasswordTextField.error = "Passwords do not match"
                } else {
                    mBinding.confirmPasswordTextField.error = null;
                }
            }
        })
    }


    fun validateCredentialAndSubmit() {
        if (mBinding.emailTextField.error != null || mBinding.passwordTextField.error != null || mBinding.confirmPasswordTextField.error != null) {
            return
        }

        if (mBinding.emailEditText.text == null || mBinding.emailEditText.text!!.isEmpty()) {
            mBinding.emailTextField.error = "Email is required"
            return
        }

        if (mBinding.passwordEditText.text == null || mBinding.passwordEditText.text!!.isEmpty()) {
            mBinding.passwordTextField.error = "Email is required"
            return
        }

        if (mBinding.confirmpasswordEditText.text == null || mBinding.confirmpasswordEditText.text!!.isEmpty()) {
            mBinding.confirmPasswordTextField.error = "Email is required"
            return
        }

        // send register request
        val queue = Volley.newRequestQueue(activity)

        val registerReqUrl = "http://192.168.0.13:25680/userprofile/register/"

        val registerObj = JSONObject()
        registerObj.put("firstName", mSignUpViewModel.firstName)
        registerObj.put("lastName", mSignUpViewModel.firstName)
        registerObj.put("userRating", 5)
        registerObj.put("email", mSignUpViewModel.email)
        registerObj.put("phone", mSignUpViewModel.phone)
        registerObj.put("password", mSignUpViewModel.password)

        val vehicleStatusRequest = JsonObjectRequest(
            Request.Method.POST, registerReqUrl, registerObj,
            { response ->
                Log.d(TAG, response.toString())

                Utility.currentUserId = response.getLong("userId")
                Utility.setAppType(if (mSignUpViewModel.needVehicle) Utility.TYPE_DRIVER else Utility.TYPE_PASSENGER)

                createAevInfo()
            },
            { error ->
                Snackbar.make(mBinding.root, "Account already exists", Snackbar.LENGTH_SHORT)
                    .setAnchorView(activity?.findViewById(R.id.back_button))
                    .show()
            }
        )
        queue.add(vehicleStatusRequest)
    }

    fun createAevInfo() {
        val queue = Volley.newRequestQueue(activity)

        val aevInfoReqUrl = "http://192.168.0.13:25680/aevinfo/create"

        val registerObj = JSONObject()
        registerObj.put("id", -1)
        registerObj.put("vehicleVin", mSignUpViewModel.vin)
        registerObj.put("plateNumber", mSignUpViewModel.licensePlate)
        registerObj.put("make", mSignUpViewModel.make)
        registerObj.put("model", mSignUpViewModel.model)
        registerObj.put("year", mSignUpViewModel.year)
        registerObj.put("vehicleType", "aev")
        registerObj.put("numSeats", mSignUpViewModel.seats)
        registerObj.put("vehicleColor", mSignUpViewModel.color)
        registerObj.put("ownerId", Utility.currentUserId)
        registerObj.put("vehicleRating", 5)

        val vehicleStatusRequest = JsonObjectRequest(
            Request.Method.POST, aevInfoReqUrl, registerObj,
            { response ->
                Log.d(TAG, response.toString())

                val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE);
                if (sharedPref != null) {
                    with(sharedPref.edit()) {
                        putLong(getString(R.string.saved_user_id), Utility.currentUserId!!)
                        apply()
                    }
                }

                (activity as SignUpActivity).launchMapsActivity()
            },
            { error ->
                Snackbar.make(mBinding.root, "Failed to upload vehicle info", Snackbar.LENGTH_SHORT)
                    .setAnchorView(activity?.findViewById(R.id.back_button))
                    .show()
            }
        )
        queue.add(vehicleStatusRequest)
    }
}