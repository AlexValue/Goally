package com.example.mygoallyapp.Data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Goals")
data class GoalBase(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val tasks: MutableList<String>
)