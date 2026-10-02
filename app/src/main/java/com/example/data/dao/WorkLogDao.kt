package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WorkLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkLogDao {
    @Query("SELECT * FROM work_logs ORDER BY date DESC, id DESC")
    fun getAllLogs(): Flow<List<WorkLog>>

    @Query("SELECT * FROM work_logs WHERE date LIKE :monthPrefix || '%' ORDER BY date ASC, id ASC")
    fun getLogsByMonth(monthPrefix: String): Flow<List<WorkLog>>

    @Query("SELECT * FROM work_logs WHERE date = :date LIMIT 1")
    fun getLogByDate(date: String): Flow<WorkLog?>

    @Query("SELECT * FROM work_logs WHERE date = :date LIMIT 1")
    suspend fun getLogByDateDirect(date: String): WorkLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(workLog: WorkLog): Long

    @Update
    suspend fun updateLog(workLog: WorkLog)

    @Delete
    suspend fun deleteLog(workLog: WorkLog)

    @Query("DELETE FROM work_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM work_logs")
    suspend fun clearAll()
}
