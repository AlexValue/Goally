package com.example.mygoallyapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.example.mygoallyapp.Data.GoalBase
import com.example.mygoallyapp.Data.GoalsDatabase
import com.example.mygoallyapp.Data.OfflineGoalsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CreateGoal : AppCompatActivity() {
    var ids = mutableListOf<Int>()
    var nameGoal = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_goal)


        val createTargetButton = findViewById<Button>(R.id.CreateTarget)
        createTargetButton.setOnClickListener {
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
        // Находим EditText с названием цели
        val NameGoal = findViewById<EditText>(R.id.NameGoal)
        nameGoal = NameGoal.text.toString()
        // Проходимся по ранее сохраненным id EditText'ов и заполняем лист задач
        var tasksList = mutableListOf<String>()
        if (ids.count() > 0){
            ids.forEach{id ->
                val editText = findViewById<EditText>(id)
                tasksList.add(editText.text.toString())
            }
        }

//
        val intent = Intent(this, MainActivity::class.java)
        val database = GoalsDatabase.getDatabase(application)
        val goalDao = database.goalDao()
        val goalsRepository = OfflineGoalsRepository(goalDao)

        startActivity(intent)

        //Для занесения цели в базу создаём отдельный процесс через корутины
        val context: Context = this
        GlobalScope.launch(Dispatchers.Main) {
            // асинхронные операции здесь
            var goalBase = GoalBase(
                name = nameGoal,
                tasks = tasksList
            )
            goalsRepository.insertGoal(goalBase, context)
        }
    }

}

