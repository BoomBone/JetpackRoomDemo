package com.tic.roomwordsample

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Created by Ting on 2019-11-28.
 */
@Dao
interface WordDao {

    //查询返回按升序排列的单词列表
    @Query("SELECT * from word_table ORDER BY word ASC")
    fun getAlphabetizedWords(): LiveData<List<Word>>

    // Declares a suspend function to insert one word.
    //如果冲突中选择的策略与列表中已有的单词完全相同，则会忽略该单词。要了解有关可用冲突策略的更多信息
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(word: Word)

    //没有用于删除多个实体的便捷注释，因此使用generic进行注释@Query
    @Query("DELETE FROM word_table")
    suspend fun deleteAll()
}