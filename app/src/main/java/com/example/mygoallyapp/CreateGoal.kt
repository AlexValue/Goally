package com.example.mygoallyapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import com.example.mygoallyapp.Data.GoalBase
import com.example.mygoallyapp.Data.GoalsDatabase
import com.example.mygoallyapp.Data.OfflineGoalsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateGoal : AppCompatActivity() {
    var ids = mutableListOf<Int>()
    var nameGoal = ""
    private var selectedDateTime: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_goal)

        // Настройка кнопки выбора дедлайна
        val deadlineButton = findViewById<Button>(R.id.deadlineButton)
        deadlineButton.setOnClickListener {
            val currentDateTime = Calendar.getInstance()
            // Открытие DatePickerDialog
            val datePickerDialog = DatePickerDialog(this,
                { _, year, monthOfYear, dayOfMonth ->
                    // Создаем календарь с выбранной датой
                    val date = Calendar.getInstance().apply {
                        set(year, monthOfYear, dayOfMonth)
                    }
                    // Открытие TimePickerDialog после выбора даты
                    val timePickerDialog = TimePickerDialog(this,
                        { _, hourOfDay, minute ->
                            // Создаем новый календарь, объединяя выбранную дату и время
                            selectedDateTime = Calendar.getInstance().apply {
                                time = date.time // Устанавливаем выбранную дату
                                set(Calendar.HOUR_OF_DAY, hourOfDay) // Устанавливаем выбранное время
                                set(Calendar.MINUTE, minute)
                            }
                            // Обновление текста кнопки после выбора времени
                            deadlineButton.text = SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault())
                                .format(selectedDateTime?.time)
                        }, 12, 0, true)
                    timePickerDialog.show()
                }, currentDateTime.get(Calendar.YEAR), currentDateTime.get(Calendar.MONTH), currentDateTime.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
        }

        val createTargetButton = findViewById<Button>(R.id.CreateTarget)
        createTargetButton.setOnClickListener {
            // Создаем новый EditText
            val editText = EditText(this)
            // Устанавливаем его идентификатор и текст по умолчанию
            val idEditText = View.generateViewId()
            editText.id = idEditText
            ids.add(idEditText)
            editText.setHint("Введите задачу")
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            // Устанавливаем параметры размещения для нового EditText
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.topMargin = resources.getDimension(R.dimen.margin_30dp).toInt()
            // Добавляем новый EditText на место кнопки CreateTarget
            val linearLayout = createTargetButton.parent as LinearLayout
            val index = linearLayout.indexOfChild(createTargetButton)
            linearLayout.addView(editText, index, layoutParams)
            // Сдвигаем кнопку CreateTarget вниз на 30dp
            val createTargetButtonLayoutParams = createTargetButton.layoutParams as LinearLayout.LayoutParams
            createTargetButtonLayoutParams.topMargin += resources.getDimension(R.dimen.margin_30dp).toInt()
            createTargetButton.layoutParams = createTargetButtonLayoutParams
        }
    }


    fun GoToMain(view: View) {
        // Находим EditText с названием цели
        val NameGoal = findViewById<EditText>(R.id.NameGoal)
        nameGoal = NameGoal.text.toString()

        // Проходимся по ранее сохраненным id EditText'ов и заполняем лист задач
        val tasksList = mutableListOf<String>()
        if (ids.isNotEmpty()){
            ids.forEach{id ->
                val editText = findViewById<EditText>(id)
                tasksList.add(editText.text.toString())
            }
        } else {
            tasksList.add(nameGoal)
        }

        val intent = Intent(this, MainActivity::class.java)
        val database = GoalsDatabase.getDatabase(application)
        val goalDao = database.goalDao()
        val userDao = database.userDao()
        val taskDao = database.taskDao()
        val goalsRepository = OfflineGoalsRepository(goalDao, userDao, taskDao)

        startActivity(intent)

        //Для занесения цели в базу создаём отдельный процесс через корутины
        val context: Context = this
        lifecycleScope.launch {
            // асинхронные операции здесь
            val goalBase = GoalBase(
                name = nameGoal,
                unfulfilledTasks = tasksList,
                fulfilledTasks = mutableListOf(),
                allTask = tasksList.count(),
                deadline = selectedDateTime?.timeInMillis ?: 0
            )
            goalsRepository.insertGoal(goalBase, context)
        }
    }

}

