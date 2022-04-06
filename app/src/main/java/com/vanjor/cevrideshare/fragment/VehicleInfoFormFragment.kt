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
import com.vanjor.cevrideshare.databinding.FragmentVehicleInfoFormBinding
import com.vanjor.cevrideshare.viewmodel.SignUpViewModel

class VehicleInfoFormFragment : Fragment() {

    private lateinit var mBinding: FragmentVehicleInfoFormBinding
    private val mSignUpViewModel: SignUpViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentVehicleInfoFormBinding.inflate(inflater, container, false)
        mBinding.apply { viewmodel = mSignUpViewModel }
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.continueButton.setOnClickListener { validateVehicleInfoForm() }

        mBinding.vinText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    mBinding.vinTextField.error = "VIN is required"
                } else {
                    mBinding.vinTextField.error = null;
                }
            }
        })

        mBinding.makeText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    mBinding.makeTextField.error = "required"
                } else {
                    mBinding.makeTextField.error = null;
                }
            }
        })

        mBinding.modelText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    mBinding.modelTextField.error = "required"
                } else {
                    mBinding.modelTextField.error = null;
                }
            }
        })

        mBinding.yearText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    mBinding.yearTextField.error = "required"
                } else {
                    mBinding.yearTextField.error = null;
                }
            }
        })

        mBinding.plateText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    mBinding.plateTextField.error = "License is required"
                } else {
                    mBinding.plateTextField.error = null;
                }
            }
        })

        mBinding.colorText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    mBinding.colorTextField.error = "required"
                } else {
                    mBinding.colorTextField.error = null;
                }
            }
        })

        mBinding.seatText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    mBinding.seatTextField.error = "required"
                } else {
                    mBinding.seatTextField.error = null;
                }
            }
        })
    }

    fun validateVehicleInfoForm() {
        // validate text fields
        if (mBinding.vinTextField.error != null || mBinding.makeTextField.error != null || mBinding.modelTextField.error != null
            || mBinding.yearTextField.error != null || mBinding.plateTextField.error != null || mBinding.colorTextField.error != null || mBinding.seatTextField.error != null) {
            return
        }

        if (mBinding.vinText.text == null || mBinding.vinText.text!!.isEmpty()) {
            mBinding.vinTextField.error = "VIN is required"
            return
        }

        if (mBinding.makeText.text == null || mBinding.makeText.text!!.isEmpty()) {
            mBinding.makeTextField.error = "required"
            return
        }

        if (mBinding.modelText.text == null || mBinding.modelText.text!!.isEmpty()) {
            mBinding.modelTextField.error = "required"
            return
        }

        if (mBinding.yearText.text == null || mBinding.yearText.text!!.isEmpty()) {
            mBinding.yearTextField.error = "required"
            return
        }

        if (mBinding.plateText.text == null || mBinding.plateText.text!!.isEmpty()) {
            mBinding.plateTextField.error = "License is required"
            return
        }

        if (mBinding.colorText.text == null || mBinding.colorText.text!!.isEmpty()) {
            mBinding.colorTextField.error = "required"
            return
        }

        if (mBinding.seatText.text == null || mBinding.seatText.text!!.isEmpty()) {
            mBinding.seatTextField.error = "required"
            return
        }

        (activity as SignUpActivity).nextPager()
    }
}