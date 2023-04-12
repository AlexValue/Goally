package com.example.mygoallyapp.Data

import androidx.room.TypeConverter

class MutableListTypeConverter {

    @TypeConverter
    fun fromString(value: String): MutableList<String> {
        return value.split(",").toMutableList()
    }

    @TypeConverter
    fun toString(list: MutableList<String>): String {
        return list.joinToString(separator = ",")
    }
}
