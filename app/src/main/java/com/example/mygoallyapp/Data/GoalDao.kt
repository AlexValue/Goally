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
}