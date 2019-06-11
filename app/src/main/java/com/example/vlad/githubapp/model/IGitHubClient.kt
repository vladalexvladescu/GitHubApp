package com.example.vlad.githubapp.model

import com.example.vlad.githubapp.model.gitHubModel.ObjectResponse
import com.google.gson.JsonArray
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor



interface IGitHubClient {

    @Headers("Accept: application/json")
    @POST("/login/oauth/access_token/")
    @FormUrlEncoded
    fun getAccessToken(
            @Field("client_id") clientId: String,
            @Field("client_secret") clientSecret: String,
            @Field("code") code: String): Observable<AccessToken>

    //https://github.com/search?q=ap
    //https://github.com/search?l=Java&q=agenda&type=Repositories
    @GET("/search/repositories")
    fun repoByName(@Header("Authorization") accessToken: String, @Query("language") languageOption: String,
                   @Query("q") name: String

    ): Observable<ObjectResponse>



    companion object {
        //factory pattern
        /*we use the default Adaptor factory that convert between Rx Observables to Call type of retrofit,
        and also the default GsonConverter object.
        */

        fun create(): IGitHubClient {
            val logging = HttpLoggingInterceptor()
          // set your desired log level
            logging.level = HttpLoggingInterceptor.Level.BODY

            val httpClient = OkHttpClient.Builder()
          // add your other interceptors …

          // add logging as last interceptor
            httpClient.addInterceptor(logging)  // <-- this is the important line!


            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(
                            RxJava2CallAdapterFactory.create())
                    .addConverterFactory(
                            GsonConverterFactory.create())
                    .baseUrl("https://api.github.com/")
                    .client(httpClient.build())
                    .build()

            return retrofit.create(IGitHubClient::class.java)
        }

        fun createForTokenOnly(): IGitHubClient {

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(
                            RxJava2CallAdapterFactory.create())
                    .addConverterFactory(
                            GsonConverterFactory.create())
                    .baseUrl("https://github.com/")
                    .build()

            return retrofit.create(IGitHubClient::class.java)
        }

    }
}