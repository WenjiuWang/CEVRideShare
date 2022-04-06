package com.vanjor.cevrideshare.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.vanjor.cevrideshare.SignUpActivity
import com.vanjor.cevrideshare.databinding.FragmentUserProfileFormBinding
import com.vanjor.cevrideshare.viewmodel.SignUpViewModel

class UserProfileFormFragment : Fragment() {

    private lateinit var mBinding: FragmentUserProfileFormBinding
    private val mSignUpViewModel: SignUpViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentUserProfileFormBinding.inflate(inflater, container, false)
        mBinding.apply { viewmodel = mSignUpViewModel }
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.continueButton.setOnClickListener { validateProfileForm() }

        mBinding.firstNameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    mBinding.firstNameTextField.error = "First name is required"
                } else {
                    mBinding.firstNameTextField.error = null;
                }
            }
        })

        mBinding.lastNameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    mBinding.lastNameTextField.error = "Last name is required"
                } else {
                    mBinding.lastNameTextField.error = null;
                }
            }
        })

        mBinding.phoneEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    mBinding.phoneTextField.error = "Phone number is required"
                } else {
                    mBinding.phoneTextField.error = null;
                }
            }
        })
    }

    fun validateProfileForm() {
        // validate text fields
        if (mBinding.firstNameTextField.error != null || mBinding.lastNameTextField.error != null || mBinding.phoneTextField.error != null) {
            return
        }

        if (mBinding.firstNameEditText.text == null || mBinding.firstNameEditText.text!!.isEmpty()) {
            mBinding.firstNameTextField.error = "First name is required"
            return
        }

        if (mBinding.lastNameEditText.text == null || mBinding.lastNameEditText.text!!.isEmpty()) {
            mBinding.lastNameTextField.error = "Last name is required"
            return
        }

        if (mBinding.phoneEditText.text == null || mBinding.phoneEditText.text!!.isEmpty()) {
            mBinding.phoneTextField.error = "Phone is required"
            return
        }

        (activity as SignUpActivity).nextPager()
    }
}