package com.example.mygoallyapp.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Date


@Entity(tableName = "Goals")
data class GoalBase(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val unfulfilledTasks: MutableList<String>,
    val fulfilledTasks: MutableList<String>,
    val allTask: Int,
    val deadline: Long
) {
    fun getDeadlineAsDate(): Date {
        return Date(deadline)
    }
}

@Entity(tableName = "Users")
data class User(
    @PrimaryKey val id: Int,
    var lastLogin: Long,
    var experience: Int,
    var countUseChatGPT: Int = 0
) {
    fun isLastLoginOlderThanOneDay(inputDate: Long): Boolean {
        val calendar1 = Calendar.getInstance().apply {
            timeInMillis = lastLogin
            // Обнуляем время, оставляем только дату
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val calendar2 = Calendar.getInstance().apply {
            timeInMillis = inputDate
            // Обнуляем время, оставляем только дату
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Сравниваем даты
        return calendar1.before(calendar2)
    }
}

@Entity(tableName = "Tasks", foreignKeys = arrayOf(ForeignKey(entity = User::class, parentColumns = arrayOf("id"), childColumns = arrayOf("user_id"), onDelete = ForeignKey.CASCADE)))
data class Task(
    @PrimaryKey val id: Int,
    val description: String,
    var reward: Int,
    @ColumnInfo(name = "is_active") var isActive: Boolean,
    @ColumnInfo(name = "is_completed") var isCompleted: Boolean,
    @ColumnInfo(name = "is_Daily_Task") val isDailyTask: Boolean,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "progress") var progress: Int = 0,
    var rewardIssued: Boolean = false
)
