package com.diffusethinking.cognitrack.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TestRecordDao {
    @Query("SELECT * FROM test_records ORDER BY startDate DESC")
    fun getAllRecordsSorted(): Flow<List<TestRecord>>

    @Query("SELECT * FROM test_records ORDER BY startDate ASC")
    fun getAllRecordsAscending(): Flow<List<TestRecord>>

    @Query("SELECT * FROM test_records WHERE isBaseline = 1 ORDER BY startDate DESC LIMIT 1")
    suspend fun getLatestBaseline(): TestRecord?

    @Query("SELECT * FROM test_records")
    suspend fun getAllRecordsList(): List<TestRecord>

    @Insert
    suspend fun insert(record: TestRecord)

    @Update
    suspend fun update(record: TestRecord)

    @Delete
    suspend fun delete(record: TestRecord)

    @Query("DELETE FROM test_records")
    suspend fun deleteAll()
}
