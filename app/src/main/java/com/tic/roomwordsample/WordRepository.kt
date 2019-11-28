package com.tic.roomwordsample

import androidx.lifecycle.LiveData

/**
 * Created by Ting on 2019-11-28.
 */
class WordRepository(private val wordDao: WordDao) {

    val allWords: LiveData<List<Word>> = wordDao.getAlphabetizedWords()

    suspend fun insert(word:Word){
        wordDao.insert(word)
    }
}