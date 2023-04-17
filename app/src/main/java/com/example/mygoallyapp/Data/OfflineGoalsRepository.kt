package com.example.mygoallyapp.Data

import android.content.Context
import com.example.mygoallyapp.Data.GoalsDatabase.Companion.getDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class OfflineGoalsRepository(private val goalDao: GoalDao) : GoalsRepository {
    override fun getAllGoalsStream(context: Context): Flow<List<GoalBase>> {
        return getDatabase(context.applicationContext).goalDao().getAllGoals()
            .map { it } // преобразование Flow<*> в Flow<List<GoalBase>>
            .flowOn(Dispatchers.IO)
    }

    override fun getGoalStream(id: Int, context: Context): Flow<GoalBase?> {
        return getDatabase(context.applicationContext).goalDao().getGoalById(id)
//            .map { it } // преобразование Flow<*> в Flow<GoalBase>
//            .flowOn(Dispatchers.IO)
    }

    override suspend fun insertGoal(goalBase: GoalBase, context: Context) {
        withContext(Dispatchers.IO) {
            getDatabase(context.applicationContext).goalDao().insert(goalBase)
        }
    }

    override suspend fun deleteGoal(goalBase: GoalBase, context: Context) {
        withContext(Dispatchers.IO) {
            getDatabase(context.applicationContext).goalDao().delete(goalBase)
        }
    }

    override suspend fun updateGoal(goalBase: GoalBase, context: Context) {
        withContext(Dispatchers.IO) {
            getDatabase(context.applicationContext).goalDao().update(goalBase)
        }
    }


}