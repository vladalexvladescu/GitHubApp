package com.example.vlad.githubapp.model.gitHubModel

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName


data class GitHubRepoAllData(@SerializedName("name")val name :String,
                      @SerializedName("id")val id :Int,
                      @SerializedName("owner")val owner :JsonObject,
                      @SerializedName("stargazers_count")var starCount :String,
                      @SerializedName("html_url")val htmlUrl :String)

