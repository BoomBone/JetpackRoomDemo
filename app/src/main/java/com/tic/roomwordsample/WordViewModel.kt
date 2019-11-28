package com.tic.roomwordsample

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * Created by Ting on 2019-11-28.
 * If you need the application context
 * (which has a lifecycle that lives as long as the application does),
 * use AndroidViewModel, as shown in this codelab.
 */
class WordViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WordRepository
    val allWords: LiveData<List<Word>>

    init {

        val wordDao = WordRoomDatabase.getDatabase(application, viewModelScope).wordDao()
        repository = WordRepository(wordDao)
        allWords = repository.allWords

    }

    fun insert(word: Word) = viewModelScope.launch {
        repository.insert(word)
    }


}