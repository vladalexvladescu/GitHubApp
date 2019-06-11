package com.example.vlad.githubapp.model.gitHubModel

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

class ObjectResponse (@SerializedName("items")val items : JsonArray)