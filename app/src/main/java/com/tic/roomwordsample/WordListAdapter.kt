package com.tic.roomwordsample

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Ting on 2019-11-28.
 */
class WordListAdapter(val context: Context) :
    RecyclerView.Adapter<WordListAdapter.WordViewHolder>() {
    private var words = emptyList<Word>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.recyclerview_item, parent, false)
        return WordViewHolder(view)
    }

    override fun getItemCount(): Int = words.size

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.wordItemView.text = words[position].word
    }

    internal fun setWords(words:List<Word>){
        this.words =words
        notifyDataSetChanged()
    }


    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val wordItemView = itemView.findViewById<TextView>(R.id.textView)
    }
}