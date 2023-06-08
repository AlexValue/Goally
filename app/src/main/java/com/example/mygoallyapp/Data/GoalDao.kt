package com.example.mygoallyapp.Data


import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM Goals")
    fun getAllGoals(): Flow<List<GoalBase>>

    @Query("SELECT * FROM Goals WHERE id = :goalId")
    fun getGoalById(goalId: Int): Flow<GoalBase?>

    @Insert
    fun insert(goal: GoalBase)

    @Update
    fun update(goal: GoalBase)

    @Delete
    fun delete(goal: GoalBase)

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoalById(goalId: Int)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM Users")
    fun getAll(): List<User>

    @Insert
    fun insert(user: User)

    @Update
    fun update(user: User)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM Tasks")
    fun getAll(): List<Task>

    @Query("SELECT * FROM Tasks WHERE user_id = :userId")
    fun findByUser(userId: Int): List<Task>

    @Query("UPDATE Tasks SET is_completed = 0, progress = 0, rewardIssued = 0 WHERE is_Daily_Task = 1")
    suspend fun resetDailyTasks()

    @Insert
    fun insert(task: Task)

    @Update
    fun update(task: Task)
}
