package com.movil.mucamas.ui.viewmodels

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movil.mucamas.domain.usecase.StartOtpFlowResult
import com.movil.mucamas.domain.usecase.StartOtpFlowUseCase
import com.movil.mucamas.ui.screens.login.OtpLoginState
import com.movil.mucamas.ui.utils.OtpManager
import com.movil.mucamas.ui.utils.OtpVerificationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// State for the OTP verification process
sealed class OtpVerificationState {
    object Loading : OtpVerificationState()
    object Success : OtpVerificationState()
    data class Error(val message: String) : OtpVerificationState()
}

class LoginViewModel : ViewModel() {

    // --- Start OTP Flow Logic ---
    private val startOtpFlowUseCase = StartOtpFlowUseCase()
    private val _otpLoginState = MutableStateFlow<OtpLoginState?>(null)
    val otpLoginState = _otpLoginState.asStateFlow()


    fun startOtpLoginFlow(context: Context, userId: String) {
        viewModelScope.launch {
            _otpLoginState.value = OtpLoginState.Loading
            when (val result = startOtpFlowUseCase(context,userId)) {
                is StartOtpFlowResult.Success -> _otpLoginState.value = OtpLoginState.OtpSent
                is StartOtpFlowResult.Error.UserNotFound -> _otpLoginState.value = OtpLoginState.InvalidId
                is StartOtpFlowResult.Error.EmailSendError -> _otpLoginState.value = OtpLoginState.EmailSendError
                is StartOtpFlowResult.Error -> _otpLoginState.value = OtpLoginState.GenericError(result.message)
            }
        }
    }

    fun resetStartState() {
        _otpLoginState.value = null
    }

    // --- Verify OTP Logic ---
    private val _otpVerifyState = MutableStateFlow<OtpVerificationState?>(null)
    val otpVerifyState = _otpVerifyState.asStateFlow()

    fun verifyOtp(userId: String, enteredOtp: String) {
        viewModelScope.launch {
            _otpVerifyState.value = OtpVerificationState.Loading

            when (val result = OtpManager.verifyOtp(userId, enteredOtp)) {
                is OtpVerificationResult.Success -> {
                    // After successful verification, delete the OTP
                    OtpManager.deleteOtp(userId)
                    _otpVerifyState.value = OtpVerificationState.Success
                }
                is OtpVerificationResult.Error -> {
                    _otpVerifyState.value = OtpVerificationState.Error(result.message)
                }
            }
        }
    }

    fun resetVerifyState() {
        _otpVerifyState.value = null
    }
}
