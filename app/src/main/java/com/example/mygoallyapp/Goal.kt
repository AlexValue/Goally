package com.example.mygoallyapp

import android.os.Parcel
import android.os.Parcelable

//Использовался в первой версии, после появления базы данных не актуален
class Goal : Parcelable {
    var NameGoal : String = ""
    var Tasks : MutableList<String> = mutableListOf()

    fun SetName(name : String){
        NameGoal = name
    }

    fun AddTask(task: String){
        Tasks.add(task)
    }

    //Написать метод выдачи Goal

    // Реализация метода writeToParcel для класса Goal
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(NameGoal)
        parcel.writeStringList(Tasks)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Goal> {
        override fun createFromParcel(parcel: Parcel): Goal {
            val goal = Goal()
            goal.NameGoal = parcel.readString() ?: ""
            parcel.readStringList(goal.Tasks)
            return goal
        }

        override fun newArray(size: Int): Array<Goal?> {
            return arrayOfNulls(size)
        }
    }
}
