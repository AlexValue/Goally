package com.example.mygoallyapp.Data

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.Executors


@Database(entities = [GoalBase::class, User::class, Task::class], version = 2)
@TypeConverters(MutableListTypeConverter::class)
abstract class GoalsDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: GoalsDatabase? = null

        fun getDatabase(context: Context): GoalsDatabase {
            val rdc: RoomDatabase.Callback = object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    Executors.newSingleThreadExecutor().execute {
                        val instance = Room.databaseBuilder(
                            context.applicationContext,
                            GoalsDatabase::class.java,
                            "goals_database"
                        ).build()
                        INSTANCE = instance
                        val taskDao = instance.taskDao()
                        if (taskDao.getAll().isEmpty()) {
                            initDatabase(context, instance.userDao(), taskDao)
                        }
                    }
                }
            }

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GoalsDatabase::class.java,
                    "goals_database"
                ).addCallback(rdc).build()
                INSTANCE = instance
                instance
            }
        }


        private fun initDatabase(context: Context, userDao: UserDao, taskDao: TaskDao) {
            val user = userDao.getAll().firstOrNull()
            if (user == null) {
                val newUser = User(id = 1, lastLogin = 0, experience = 100)
                userDao.insert(newUser)
            }

            val userId = 1
            val isCompleted = false
            val isActive = listOf(true, true, true, true, true, false, false, false, false, false,
                false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false)

            val descriptions = listOf("Выполнить 3 задачи",
                "Зайти в приложение",
                "Завершить 3 цели",
                "Завершить 10 целей",
                "Завершить 50 целей",
                "Завершить задачу, которую вы откладывали длительное время",
                "Завершить 1 задачу, из категории \"Здоровье\"",
                "Завершить 1 задачу, из категории \"Учёба\"",
                "Завершить 1 задачу, из категории \"Дом\"",
                "Завершить 1 задачу, из категории \"Работа\"",
                "Завершить 1 цель",
                "Завершить 3 цели",
                "Завершить 10 цель",
                "Завершить 10 целей",
                "Завершить 50 целей",
                "Выполнять задачи 7 дней подряд",
                "Выполнять задачи 30 дней подряд",
                "Выполнять задачи 365 дней подряд",
                "Выполнить 5 задач в срок",
                "Выполнить 15 задач в срок",
                "Выполнить 45 задач в срок",
                "Достичь 1 уровня",
                "Достичь 3 уровня",
                "Достичь 5 уровня",
                "Достичь 10 уровня",
                "Достичь 20 уровня",
                "Достичь 30 уровня",
                "Достичь 50 уровня",
                "Достичь 100 уровня")
            val rewards = listOf(20, 20, 60, 60, 60, 20, 20, 20, 20, 20,
                60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60)
            val isDailyTask = listOf(true, true, false, false, false, true, true, true, true, true,
                false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false)

            assert(descriptions.size == rewards.size && rewards.size == isDailyTask.size)

            for (i in descriptions.indices) {
                val task = Task(id = i + 1, description = descriptions[i], reward = rewards[i], isCompleted = isCompleted, isActive = isActive[i], isDailyTask = isDailyTask[i], userId = userId)
                taskDao.insert(task)
            }
        }
    }
}





