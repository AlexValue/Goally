package com.example.mygoallyapp

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout

class CreateGoal : AppCompatActivity() {
    var ids = mutableListOf<Int>()
    var name = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_goal)

        val NameGoal = findViewById<EditText>(R.id.NameGoal)

        val createTargetButton = findViewById<Button>(R.id.CreateTarget)
        createTargetButton.setOnClickListener {
            name = NameGoal.text.toString()
            // Создаем новый EditText
            val editText = EditText(this)
            // Устанавливаем его идентификатор и текст по умолчанию
            val idEditText = View.generateViewId()
            editText.id = idEditText
            ids.add(idEditText)
            editText.setHint("Введите задачу")
            // Устанавливаем параметры размещения для нового EditText
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.topMargin = resources.getDimension(R.dimen.margin_100dp).toInt()
            // Добавляем новый EditText на место кнопки CreateTarget
            val linearLayout = createTargetButton.parent as LinearLayout
            val index = linearLayout.indexOfChild(createTargetButton)
            linearLayout.addView(editText, index, layoutParams)
            // Сдвигаем кнопку CreateTarget вниз на 50dp
            val createTargetButtonLayoutParams = createTargetButton.layoutParams as LinearLayout.LayoutParams
            createTargetButtonLayoutParams.topMargin += resources.getDimension(R.dimen.margin_50dp).toInt()
            createTargetButton.layoutParams = createTargetButtonLayoutParams
        }
    }


    fun GoToMain(view: View) {
        var Goal = Goal()
        Goal.SetName(name)
        if (ids.count() > 0){
            ids.forEach{id ->
                Goal.AddTask(id.toString())
            }
        }
        else{
            Goal.AddTask("")
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("goal", Goal)
        startActivity(intent)
    }


}

