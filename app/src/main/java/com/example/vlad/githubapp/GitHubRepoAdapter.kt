package com.example.vlad.githubapp

import android.content.Context
import android.media.MediaPlayer
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.vlad.githubapp.model.gitHubModel.GitHubRepoAllDataModel
import kotlinx.android.synthetic.main.repo_item.view.*
import java.util.ArrayList
import android.content.Intent
import android.net.Uri


class GitHubRepoAdapter(val items: ArrayList<GitHubRepoAllDataModel>, val context: Context) : RecyclerView.Adapter<GitHubRepoAdapter.ViewHolder>() {

    // Gets the number of songs in the list
    override fun getItemCount(): Int {

        return items.size
    }

    override
    fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    var itemViewList = ArrayList<View>()
    var nameOfRepoAux = ""
    var positionSaver: Int = 0
    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {

        val itemView = LayoutInflater.from(context).inflate(R.layout.repo_item, parent, false);
        val myViewHolder = ViewHolder(itemView)
        itemViewList.add(itemView); //to add all the 'list row item' views
        return myViewHolder
    }

    // Binds each song in the ArrayList to a view
    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.starImage?.setImageDrawable(context.getResources().getDrawable(R.drawable.star))
        holder?.starNumber?.setText(items.get(position).gitHubRepo.starCount)

        holder?.starNumber?.setTextColor(context.getResources().getColor(R.color.text_gray_color))
        positionSaver = position
        nameOfRepoAux = items.get(position).gitHubRepo.name
        holder?.name?.text = nameOfRepoAux
        holder?.owner?.text = items.get(position).repoOwner.name
        holder?.name?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                val openURL = Intent(android.content.Intent.ACTION_VIEW)
                openURL.data = Uri.parse(items.get(position).gitHubRepo.htmlUrl)
                context.startActivity(openURL)

            }
        })

    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val owner = view.owner
        val name = view.name
        val starNumber = view.star_number
        val starImage = view.star_image
    }

}