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
    fun getGoalStream(id: Int, context: Context): GoalBase?


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

    /**
     * Retrieve all the users from the the given data source.
     */
    fun getAllUsers(context: Context): List<User>

    /**
     * Insert user in the data source
     */
    suspend fun insertUser(user: User, context: Context)

    /**
     * Retrieve all the tasks from the given data source.
     */
    fun getAllTasks(context: Context): List<Task>

    /**
     * Retrieve all tasks for a specific user from the given data source.
     */
    fun getTasksByUser(context: Context, userId: Int): List<Task>

    /**
     * Reset all daily tasks in the data source.
     */
    suspend fun resetDailyTasks(context: Context)

    /**
     * Insert task in the data source
     */
    suspend fun insertTask(task: Task, context: Context)

    /**
     * Update task in the data source
     */
    suspend fun updateTask(task: Task, context: Context)
}