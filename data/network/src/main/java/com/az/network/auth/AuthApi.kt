package com.olaf.network.auth

import com.olaf.model.auth.request.SignUpRequestData
import com.olaf.model.auth.response.SignInResponseData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface AuthApi {

    @POST("auth/sign-up")
    fun signUp(
        @Body
        signUpRequestData: SignUpRequestData
    ): Call<SignInResponseData>
}