package com.example.mygoallyapp.Data

import androidx.room.Entity
import androidx.room.PrimaryKey
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