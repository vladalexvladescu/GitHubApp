package com.example.vlad.githubapp

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.example.vlad.githubapp.model.IGitHubClient
import com.example.vlad.githubapp.model.gitHubModel.GitHubRepoAllData
import com.example.vlad.githubapp.model.gitHubModel.GitHubRepoAllDataModel
import com.example.vlad.githubapp.model.gitHubModel.RepoOwnerData
import com.google.gson.GsonBuilder
import es.dmoral.toasty.Toasty
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.ArrayAdapter
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    //defined this object lazily so is created when the first time it is used,
    // reason- no need to write a function
    val iGitHubClient by lazy {
        IGitHubClient.create()
    }
    val iGitHubClientForTokenOnly by lazy {
        IGitHubClient.createForTokenOnly()
    }

    //in case feching activity is destroyed dispose this object- prevents
    // crashes if the activity crashed before we have a result
    var disposable: Disposable? = null

    val clientID = "0dfe0aeaa27bbe3b1bb2"
    val clientSecret = "96397cd849ac2816fe424f6aa3772810d70bf021"
    val redirectURL = "vladgithubapp://callback"
    var accessTokenGlobal = "none"
    var spinnerOption = "All"
    var loggedOrNot = "none"
    var qeryFromSearch = "a"

    var repos: ArrayList<GitHubRepoAllDataModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getSupportActionBar()?.setTitle(getResources().getString(R.string.search_hint))

        progress_bar.setVisibility(View.INVISIBLE)
        val adapter = ArrayAdapter.createFromResource(this,
                R.array.languages_list, android.R.layout.simple_spinner_item)
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        drop_down_spinner.adapter = adapter

        drop_down_spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {


            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                spinnerOption = parent?.getItemAtPosition(position).toString()
                search_title.text="Search by '"+qeryFromSearch+"' "+"with filter "+spinnerOption
                searchByRepoName(loggedOrNot,qeryFromSearch,spinnerOption)

            }

        }

        loggedOrNot = getValue("CLIENTLOGGED")
        if (loggedOrNot.equals("defaultStringIfNothingFound")) {
            progress_bar.setVisibility(View.INVISIBLE)
            btn_login.setText("Login to GitHub")
        } else {
            btn_login.setVisibility(View.GONE)
        }


        if (Intent.ACTION_SEARCH == intent.action) {
            if (loggedOrNot.equals("defaultStringIfNothingFound")) {
                Toasty.error(this, "Login to GitHub to perform this action!", Toast.LENGTH_SHORT).show()
            } else {
                qeryFromSearch = intent.getStringExtra(SearchManager.QUERY)
                search_title.text="Search by '"+qeryFromSearch+"'"
                searchByRepoName(loggedOrNot,qeryFromSearch,spinnerOption)
            }


        }

        btn_login.setOnClickListener {
            if (loggedOrNot.equals("defaultStringIfNothingFound")) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/login/oauth/authorize" + "?client_id=" + clientID + "&scope=repo&redirect_uri=" + redirectURL))
                startActivity(intent)


            }

        }

        rv_repo_list.layoutManager = LinearLayoutManager(this) as RecyclerView.LayoutManager?
        rv_repo_list.layoutManager = GridLayoutManager(this, 1)
        rv_repo_list.adapter = GitHubRepoAdapter(repos, this)

    }

    //makes a token request after user login
    private fun getAccessTokenClient(code: String) {
        /*iGitHubClient s the singleton service, where hitCountCheck will return an Observable

      the Observable, since it’s like a endpoint fetcher result generator
      , we tell it to fetch the data on background by subscribeOn(Schedulers.io())

      we like the fetched data to be displayed on the MainTread (UI)
      so we set observeOn(AndroidSchedulers.mainThread())

      In term of what we do with the result, we use subscribe to define our action on the result
      where by the result is the fetched data result, where we could access the totalhits data accordingly.
       In the event of error occurs, it will return error instead to be handled.
       */
        progress_bar.setVisibility(View.VISIBLE)
        disposable = iGitHubClientForTokenOnly.getAccessToken(clientID, clientSecret, code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            btn_login.setVisibility(View.GONE)
                            accessTokenGlobal = result?.accessToken.toString()
                            storeValue("CLIENTLOGGED",accessTokenGlobal)
                            Toast.makeText(this,"Succes retriving token!", Toast.LENGTH_SHORT).show()


                        },
                        { error ->
                            TODO()
                        }
                )
    }
    private fun searchByRepoName(accessToken: String, name: String,languageOption:String) {
        progress_bar.setVisibility(View.VISIBLE)
        disposable = iGitHubClient.repoByName(accessToken,languageOption,name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { result ->
                            val gson = GsonBuilder().create()
                            val objectRespponse = gson.fromJson(result.items, Array<GitHubRepoAllData>::class.java).toList()
                            addNewItemsAndRedrawView(objectRespponse)

                        },
                        { error ->
                            Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                            TODO()
                        }
                )
    }


    override fun onResume() {
        super.onResume()
        var uri = intent.data
        if (uri != null && uri.toString().startsWith(redirectURL)) {
            progress_bar.setVisibility(View.VISIBLE)
            val code = uri.getQueryParameter("code")
            getAccessTokenClient(code)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_activity_main, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.mi_search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }

        return true
    }
  //sorts items by starcount in teh list the redraw view
   private fun addNewItemsAndRedrawView(list: List<GitHubRepoAllData>) {
        //sort by starcount
        var sortedList = ArrayList(list).sortedWith(compareBy({ it.starCount.toInt() }))
        sortedList = sortedList.asReversed()
        repos.clear()
        for (i in sortedList) {
            val gson = GsonBuilder().create()
            var repoOwner = gson.fromJson(i.owner, RepoOwnerData::class.java)
            val gitHubRepoAllDataModel = GitHubRepoAllDataModel(i, repoOwner)
            repos.add(gitHubRepoAllDataModel)
        }
        redrawRecycleView()

    }
//redraws  redrawRecycleView when necessary
  private  fun redrawRecycleView() {
        rv_repo_list.setAdapter(null);
        rv_repo_list.setLayoutManager(null);
        rv_repo_list.layoutManager = LinearLayoutManager(this) as RecyclerView.LayoutManager?
        rv_repo_list.layoutManager = GridLayoutManager(this, 1)
        rv_repo_list.setAdapter(GitHubRepoAdapter(repos, this))
        rv_repo_list.adapter.notifyDataSetChanged()
        progress_bar.setVisibility(View.INVISIBLE)
    }
    //stores in shared pref
   private fun storeValue(key:String,value:String){
       PreferenceManager.getDefaultSharedPreferences(applicationContext).edit().putString(key, value).apply();
   }
    //gets a value from shared pref
    private fun getValue(key:String):String{

        return  PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getString(key, "defaultStringIfNothingFound")
    }
}

