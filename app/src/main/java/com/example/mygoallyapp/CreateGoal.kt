package com.example.mygoallyapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mygoallyapp.Data.GoalBase
import com.example.mygoallyapp.Data.GoalsDatabase
import com.example.mygoallyapp.Data.OfflineGoalsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CreateGoal : AppCompatActivity() {
    var ids = mutableListOf<Int>()
    var nameGoal = ""
    private var selectedDateTime: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_goal)

        // Настройка кнопки отмена
        val cancelButton = findViewById<Button>(R.id.CancelButton)
        cancelButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.greyLight)
        cancelButton.elevation = 0f
        cancelButton.setOnClickListener {
            finish()
        }

        // Настройка кнопки выбора дедлайна
        val deadlineButton = findViewById<Button>(R.id.deadlineButton)
        val typedValue = TypedValue()
        val theme = getTheme()
        theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true)
        deadlineButton.backgroundTintList = ColorStateList.valueOf(typedValue.data)
        theme.resolveAttribute(com.google.android.material.R.attr.colorOnSecondaryContainer, typedValue, true)
        deadlineButton.setTextColor(typedValue.data)
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
                            val selectedYear = selectedDateTime?.get(Calendar.YEAR) ?: 0
                            if (selectedYear != 1970) {
                                deadlineButton.text =
                                    SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                                        .format(selectedDateTime?.time)
                            } else { deadlineButton.text = "" }
                        },
                        currentDateTime.get(Calendar.HOUR_OF_DAY),
                        currentDateTime.get(Calendar.MINUTE),
                        true
                    )
                    timePickerDialog.show()
                },
                currentDateTime.get(Calendar.YEAR),
                currentDateTime.get(Calendar.MONTH),
                currentDateTime.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        val createTargetButton = findViewById<Button>(R.id.CreateTarget)
        val scrollView = findViewById<ScrollView>(R.id.scrollView2)
        val linearLayout = findViewById<LinearLayout>(R.id.scrollView)

        createTargetButton.setOnClickListener {
            // Получаем EditText
            val taskEditText = findViewById<EditText>(R.id.taskEditText)
            val taskText = taskEditText.text.toString()

            if (taskText.isNotEmpty()) {
                // Создаем новый TextView для отображения задачи
                val textView = TextView(this)
                textView.text = taskText
                textView.setBackgroundResource(R.drawable.goal_item_background)

                //Сохраняем айдишник TextView
                val idTextView = View.generateViewId()
                textView.id = idTextView
                ids.add(idTextView)

                // Устанавливаем шрифт, цвет и размер текста
                textView.setTypeface(Typeface.create("roboto_medium", Typeface.NORMAL))
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
                theme.resolveAttribute(com.google.android.material.R.attr.colorSecondaryContainer, typedValue, true)
                textView.setTextColor(typedValue.data)

                // Устанавливаем параметры размещения для нового TextView
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                // Добавляем отступы в параметры размещения
                layoutParams.topMargin = dpToPx(20)
                layoutParams.bottomMargin = dpToPx(20)
                layoutParams.leftMargin = dpToPx(20)
                layoutParams.rightMargin = dpToPx(20)

                // Добавляем новый TextView в начало списка задач
                linearLayout.addView(textView, linearLayout.childCount - 1, layoutParams)

                // Очищаем поле ввода задачи
                taskEditText.text.clear()

                // Прокручиваем ScrollView вверх, чтобы новая задача была видна
                scrollView.post {
                    scrollView.fullScroll(ScrollView.FOCUS_UP)
                }
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun GoToMain(view: View) {
        // Находим EditText с названием цели
        val NameGoal = findViewById<EditText>(R.id.NameGoal)
        nameGoal = NameGoal.text.toString()
        val DescriptionGoal = findViewById<EditText>(R.id.TextGoal)
        val descriptionGoal = DescriptionGoal.text.toString()

        val taskEditText = findViewById<EditText>(R.id.taskEditText)
        val taskText = taskEditText.text.toString()

        // Проверяем, что цель не пустая, если пустая - не сохраняем
        if (nameGoal.isEmpty()) {
            val borderDrawable = resources.getDrawable(R.drawable.rounded_square_bg) as GradientDrawable
            borderDrawable.setStroke(3, resources.getColor(android.R.color.holo_red_light))
            NameGoal.background = borderDrawable
            NameGoal.requestFocus()
            return
        } else {
            NameGoal.background = resources.getDrawable(R.drawable.rounded_square_bg)
        }

        // Проходимся по ранее сохраненным id TextView'ов и заполняем лист задач
        val tasksList = mutableListOf<String>()
        if (ids.isNotEmpty()){
            ids.forEach{id ->
                val textView = findViewById<TextView>(id)
                tasksList.add(textView.text.toString())
            }
            if (taskText.isNotEmpty()) {
                tasksList.add(taskText)
            }
        } else {
            if (taskText.isNotEmpty()) {
                tasksList.add(taskText)
            } else {
                tasksList.add(nameGoal)
            }
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
                description = descriptionGoal,
                unfulfilledTasks = tasksList,
                fulfilledTasks = mutableListOf(),
                allTask = tasksList.count(),
                deadline = selectedDateTime?.timeInMillis ?: 0L
            )
            goalsRepository.insertGoal(goalBase, context)
        }
    }
}

