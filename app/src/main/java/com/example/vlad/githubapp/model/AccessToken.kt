package com.example.vlad.githubapp.model

import com.google.gson.annotations.SerializedName

class AccessToken (@SerializedName("access_token")val accessToken :String,
                   @SerializedName("token_type")val tokenType :String )