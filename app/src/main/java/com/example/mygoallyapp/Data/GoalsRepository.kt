package com.example.mygoallyapp.Data

import android.content.Context
import kotlinx.coroutines.flow.Flow

interface GoalsRepository {
    /**
     * Retrieve all the items from the the given data source.
     */
    fun getAllGoalsStream(context: Context): Flow<List<GoalBase>>

    /**
     * Retrieve an item from the given data source that matches with the [id].
     */
    fun getGoalStream(id: Int, context: Context): Flow<GoalBase?>


    /**
     * Insert item in the data source
     */
    suspend fun insertGoal(goalBase: GoalBase, context: Context)

    /**
     * Delete item from the data source
     */
    suspend fun deleteGoal(goalBase: GoalBase, context: Context)

    /**
     * Update item in the data source
     */
    suspend fun updateGoal(goalBase: GoalBase, context: Context)
}