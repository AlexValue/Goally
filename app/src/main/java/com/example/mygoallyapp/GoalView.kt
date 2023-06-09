package com.example.mygoallyapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mygoallyapp.Data.GoalBase
import com.example.mygoallyapp.Data.GoalsDatabase
import com.example.mygoallyapp.Data.OfflineGoalsRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class GoalView : AppCompatActivity() {
    var idGoal = -1
    var ids = mutableListOf<Int>()
    var lastId = -1
    var countUseAddTask = 0
    private var selectedDateTime: Calendar? = null
    var useDeadLineButton: Boolean = false
    var goalBaseStart = GoalBase( name = "", description = "", unfulfilledTasks = mutableListOf(), fulfilledTasks = mutableListOf(), allTask = 0, deadline = 0)

    private lateinit var linearLayout: LinearLayout
    private lateinit var layoutParams: LinearLayout.LayoutParams
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_view)

        val intent = getIntent()
        val goalId = intent.getIntExtra("goal_id", -1)
        idGoal = goalId

        showGoalInfo(goalId, this) { goalInfoView ->
            // Получаем корневой макет активности
            val mainLayout = findViewById<LinearLayout>(R.id.main_layout)

            // Удаляем все предыдущие дочерние элементы, если они есть
            mainLayout.removeAllViews()

            // Добавляем goalInfoView в корневой макет активности
            mainLayout.addView(goalInfoView)
        }

        val database = GoalsDatabase.getDatabase(this)
        lifecycleScope.launch (Dispatchers.IO) {
            val goalDao = database.goalDao()
            val goal = goalDao.getGoal(idGoal)
            withContext(Dispatchers.Main) {
                if (goal != null) {
                    val NameGoal = findViewById<EditText>(R.id.NameGoal)
                    val DescriptionGoal = findViewById<EditText>(R.id.DescriptionGoal)
                    val DeadLineButton = findViewById<Button>(R.id.deadlineButton)
                    NameGoal.setText(goal.name)
                    DescriptionGoal.setText(goal.description)
                    val typedValue = TypedValue()
                    val theme = getTheme()
                    theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true)
                    DeadLineButton.backgroundTintList = ColorStateList.valueOf(typedValue.data)
                    theme.resolveAttribute(com.google.android.material.R.attr.colorSecondaryContainer, typedValue, true)
                    DeadLineButton.setTextColor(typedValue.data)
                    val deadlineDate = Date(goal.deadline)
                    val format = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                    val deadlineString = format.format(deadlineDate)
                    if (goal.deadline != 0L) {
                        DeadLineButton.text = deadlineString
                    }
                    DeadLineButton.setOnClickListener {
                        useDeadLineButton = true
                        val currentDateTime = Calendar.getInstance()
                        // Открытие DatePickerDialog
                        val datePickerDialog = DatePickerDialog(
                            this@GoalView,
                            { _, year, monthOfYear, dayOfMonth ->
                                // Создаем календарь с выбранной датой
                                val date = Calendar.getInstance().apply {
                                    set(year, monthOfYear, dayOfMonth)
                                }
                                // Открытие TimePickerDialog после выбора даты
                                val timePickerDialog = TimePickerDialog(
                                    this@GoalView,
                                    { _, hourOfDay, minute ->
                                        // Создаем новый календарь, объединяя выбранную дату и время
                                        selectedDateTime = Calendar.getInstance().apply {
                                            time = date.time // Устанавливаем выбранную дату
                                            set(
                                                Calendar.HOUR_OF_DAY,
                                                hourOfDay
                                            ) // Устанавливаем выбранное время
                                            set(Calendar.MINUTE, minute)
                                        }
                                        // Обновление текста кнопки после выбора времени
                                        val selectedYear = selectedDateTime?.get(Calendar.YEAR) ?: 0
                                        if (selectedYear != 1970) {
                                            DeadLineButton.text =
                                                SimpleDateFormat(
                                                    "dd MMM yyyy HH:mm",
                                                    Locale.getDefault()
                                                )
                                                    .format(selectedDateTime?.time)
                                        } else {
                                            DeadLineButton.text = ""
                                        }
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
                }
            }
        }

        // кнопка удаления цели
        val deleteButton = findViewById<Button>(R.id.deleteButton)
        deleteButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.delete)
        deleteButton.elevation = 0f
        deleteButton.setOnClickListener{
            val builder = MaterialAlertDialogBuilder(this@GoalView, R.style.AlertDialogTheme)
           // androidx.appcompat.app.AlertDialog.Builder(this@GoalView, R.style.AlertDialogTheme)
            builder.setTitle("Удаление")
                .setMessage("Вы уверены, что хотите удалить цель?")
                .setPositiveButton("Удалить") { dialog, _ ->
                    deleteGoal(idGoal, this)
                    dialog.dismiss()
                }
                .setNegativeButton("Отменить") {dialog, _ ->
                    dialog.dismiss()
                }

            builder.create().show()
        }
    }

    private fun deleteGoal(goalId: Int, context: Context) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val database = GoalsDatabase.getDatabase(context)
                val goalDao = database.goalDao()
                goalDao.deleteGoalById(goalId)
                finish()
            }
        }
    }

    fun showGoalInfo(id: Int, context: Context, onGoalInfoReady: (View) -> Unit) {
        val database = GoalsDatabase.getDatabase(context)
        val goalDao = database.goalDao()
        val userDao = database.userDao()
        val taskDao = database.taskDao()
        val goalsRepository = OfflineGoalsRepository(goalDao, userDao, taskDao)

        lifecycleScope.launch {
            val goal = goalsRepository.getGoalStream(id, context).firstOrNull()
            if (goal != null) {
                goalBaseStart = goal

                val rootLayout = LinearLayout(context)
                rootLayout.orientation = LinearLayout.VERTICAL
                rootLayout.setBackgroundResource(R.drawable.goal_item_background)
                val rootLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
//                rootLayoutParams.setMargins(
//                    dpToPx(8f, context),
//                    dpToPx(8f, context),
//                    dpToPx(8f, context),
//                    dpToPx(8f, context)
//                )
                rootLayout.layoutParams = rootLayoutParams

                val tasksLayout = LinearLayout(context)
                tasksLayout.orientation = LinearLayout.VERTICAL
                rootLayout.addView(tasksLayout)

                goal.unfulfilledTasks.forEach { task ->
                    if (task.isNotBlank()) { // Add this line to check if the task is not empty
                        createTaskLayout(context, task, goal, goalsRepository, tasksLayout)
                    }
                }

                val completedTasksLayout = LinearLayout(context)
                completedTasksLayout.orientation = LinearLayout.VERTICAL
                completedTasksLayout.setBackgroundResource(R.drawable.box_task_completed)

                displayFulfilledTasks(goal, completedTasksLayout, context)
                if (goal.fulfilledTasks.isNotEmpty()) {
                    tasksLayout.addView(completedTasksLayout)
                }

                onGoalInfoReady(rootLayout)
            }
        }
    }

    fun showGoalInfo1(id: Int, context: Context, onGoalInfoReady: (View) -> Unit) {
        val database = GoalsDatabase.getDatabase(context)
        val goalDao = database.goalDao()
        val userDao = database.userDao()
        val taskDao = database.taskDao()
        val goalsRepository = OfflineGoalsRepository(goalDao, userDao, taskDao)

        lifecycleScope.launch {
            val goal = goalsRepository.getGoalStream(id, context).firstOrNull()
            if (goal != null) {
                goalBaseStart = goal
                val rootLayout = LinearLayout(context)
                rootLayout.orientation = LinearLayout.VERTICAL
                rootLayout.setBackgroundResource(R.drawable.goal_item_background)
                val rootLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                rootLayoutParams.setMargins(
                    dpToPx(8f, context),
                    dpToPx(8f, context),
                    dpToPx(8f, context),
                    dpToPx(8f, context)
                )
                rootLayout.layoutParams = rootLayoutParams

                val nameTextView = TextView(context)
                nameTextView.text = goal.name + "\n"
                nameTextView.textSize = 18f
                nameTextView.gravity = Gravity.CENTER
                //nameTextView.setBackgroundColor(Color.LTGRAY)
                val typedValue = TypedValue()
                val theme = context.getTheme()
                theme.resolveAttribute(com.google.android.material.R.attr.colorSecondaryContainer, typedValue, true)
                nameTextView.setTextColor(typedValue.data)
                nameTextView.setPadding(
                    dpToPx(16f, context),
                    dpToPx(8f, context),
                    dpToPx(16f, context),
                    dpToPx(8f, context)
                )
                val nameLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                rootLayout.addView(nameTextView, nameLayoutParams)

                val tasksLayout = LinearLayout(context)
                tasksLayout.orientation = LinearLayout.VERTICAL
                rootLayout.addView(tasksLayout)

                goal.unfulfilledTasks.forEach { task ->
                    if (task.isNotBlank()) { // Add this line to check if the task is not empty
                        createTaskLayout(context, task, goal, goalsRepository, tasksLayout)
                    }
                }

                displayFulfilledTasks(goal, tasksLayout, context)
                onGoalInfoReady(rootLayout)
            }
        }
    }

    private fun createTaskLayout(
        context: Context,
        task: String,
        goal: GoalBase,
        goalsRepository: OfflineGoalsRepository,
        tasksLayout: LinearLayout
    ) {
        val taskLayout = LinearLayout(context)
        taskLayout.orientation = LinearLayout.HORIZONTAL
        //taskLayout.gravity = Gravity.END
        //taskLayout.setBackgroundResource(R.drawable.box_task)
        val taskLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        taskLayoutParams.setMargins(
            dpToPx(8f, context),
            dpToPx(8f, context),
            dpToPx(8f, context),
            dpToPx(8f, context)
        )
        taskLayout.layoutParams = taskLayoutParams

        val taskTextView = TextView(context)
        taskTextView.text = task
        taskTextView.gravity = Gravity.CENTER_VERTICAL or Gravity.LEFT
        //taskTextView.setBackgroundColor(Color.LTGRAY)
        val typedValue = TypedValue()
        val theme = context.getTheme()
        theme.resolveAttribute(com.google.android.material.R.attr.colorSecondaryContainer, typedValue, true)
        taskTextView.setTextColor(typedValue.data)
        taskTextView.setPadding(
            dpToPx(16f, context),
            dpToPx(8f, context),
            dpToPx(16f, context),
            dpToPx(8f, context)
        )
        val taskHeight = dpToPx(48f, context) // Set the height to 48dp
        val layoutParams = LinearLayout.LayoutParams(0, taskHeight, 1f)
        layoutParams.gravity = Gravity.CENTER_VERTICAL
        layoutParams.marginEnd = dpToPx(16f, context)
        taskTextView.layoutParams = layoutParams

        val circleView = View(context)
        circleView.setBackgroundResource(R.drawable.circle)
        val circleSize = dpToPx(32f, context) // Set the size of the circle to 32dp
        val circleLayoutParams = LinearLayout.LayoutParams(circleSize, circleSize)
        circleLayoutParams.gravity = Gravity.CENTER_VERTICAL

        circleView.setOnClickListener {
            val isChecked = circleView.tag != null && circleView.tag as Boolean
            toggleCircleView(circleView, !isChecked)
            strikeThroughText(taskTextView, !isChecked)
            circleView.tag = !isChecked

            lifecycleScope.launch(Dispatchers.IO) {
                val database = GoalsDatabase.getDatabase(context)
                val taskDao = database.taskDao()
//                val userDao = database.userDao()
//                val user = userDao.getAll()[0]
                val currentTask = taskDao.getAll()[0]

                if (!currentTask.isCompleted) {
                    currentTask.progress++
                }

                if (currentTask.progress >= 3 && !currentTask.isCompleted) {
                    currentTask.isCompleted = true
//                    user.experience += currentTask.reward
                }

                taskDao.update(currentTask)
//                userDao.update(user)
            }

            onTaskStatusChanged(goal, task, !isChecked, goalsRepository, tasksLayout, context)
            tasksLayout.removeView(taskLayout)
        }


        taskLayout.addView(taskTextView)
        taskLayout.addView(circleView, circleLayoutParams)
        tasksLayout.addView(taskLayout)
    }

    private suspend fun markTaskAsFulfilled(goal: GoalBase, task: String, goalsRepository: OfflineGoalsRepository) {
        val updatedUnfulfilledTasks = goal.unfulfilledTasks.toMutableList()
        updatedUnfulfilledTasks.remove(task)

        val updatedFulfilledTasks = goal.fulfilledTasks.toMutableList()
        updatedFulfilledTasks.add(task)

        val updatedGoal = goal.copy(unfulfilledTasks = updatedUnfulfilledTasks, fulfilledTasks = updatedFulfilledTasks)
        goalsRepository.updateGoal(updatedGoal, this)
    }

    private fun strikeThroughText(textView: TextView, strikeThrough: Boolean) {
        if (strikeThrough) {
            textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }



    private suspend fun moveTaskToEnd(goal: GoalBase, task: String, goalsRepository: OfflineGoalsRepository) {
        val updatedUnfulfilledTasks = goal.unfulfilledTasks.toMutableList()
        updatedUnfulfilledTasks.remove(task)

        val updatedFulfilledTasks = goal.fulfilledTasks.toMutableList()
        updatedFulfilledTasks.add(task)

        val updatedGoal = goal.copy(unfulfilledTasks = updatedUnfulfilledTasks, fulfilledTasks = updatedFulfilledTasks)
        goalsRepository.updateGoal(updatedGoal, this)
    }



    private fun toggleCircleView(circleView: View, isChecked: Boolean) {
        if (isChecked) {
            circleView.setBackgroundResource(R.drawable.checkmark)
        } else {
            circleView.setBackgroundResource(R.drawable.circle)
        }
    }

    private fun displayFulfilledTasks(goal: GoalBase, tasksLayout: LinearLayout, context: Context) {
        goal.fulfilledTasks.forEach { task ->
            if (task.isNotBlank()) {
                val taskLayout = LinearLayout(context)
                taskLayout.orientation = LinearLayout.HORIZONTAL
                val taskLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                taskLayoutParams.setMargins(
                    dpToPx(8f, context),
                    dpToPx(8f, context),
                    dpToPx(8f, context),
                    dpToPx(8f, context)
                )
                taskLayout.layoutParams = taskLayoutParams

                val taskTextView = TextView(context)
                taskTextView.text = task
                taskTextView.gravity = Gravity.CENTER_VERTICAL or Gravity.LEFT

                // Create a new SpannableStringBuilder with the given text
                val spannableString = SpannableStringBuilder(task)

                // Set the ForegroundColorSpan on the entire length of the text
                spannableString.setSpan(
                    ForegroundColorSpan(Color.parseColor("#578925")),  // Set the custom green color
                    0,                                                 // Start of the span (inclusive)
                    task.length,                                       // End of the span (exclusive)
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // Apply the colorized text to the TextView
                taskTextView.text = spannableString

                // Set the text color to the custom green color
                taskTextView.setTextColor(Color.parseColor("#578925"))

                taskTextView.setPadding(
                    dpToPx(16f, context),
                    dpToPx(8f, context),
                    dpToPx(16f, context),
                    dpToPx(8f, context)
                )
                val taskHeight = dpToPx(48f, context) // Set the height to 48dp
                val layoutParams = LinearLayout.LayoutParams(0, taskHeight, 1f)
                layoutParams.gravity = Gravity.CENTER_VERTICAL
                layoutParams.marginEnd = dpToPx(16f, context)
                taskTextView.layoutParams = layoutParams

                // Add strike-through to fulfilled tasks
                strikeThroughText(taskTextView, true)

                val checkmarkView = ImageView(context)
                checkmarkView.setImageResource(R.drawable.checkmark)
                val checkmarkSize = dpToPx(32f, context) // Set the size of the checkmark to 32dp
                val checkmarkLayoutParams = LinearLayout.LayoutParams(checkmarkSize, checkmarkSize)
                checkmarkLayoutParams.gravity = Gravity.CENTER_VERTICAL
                taskLayout.addView(taskTextView)
                taskLayout.addView(checkmarkView, checkmarkLayoutParams)

                tasksLayout.addView(taskLayout)
            }
        }
    }


    private fun onTaskStatusChanged(
        goal: GoalBase,
        task: String,
        isChecked: Boolean,
        goalsRepository: OfflineGoalsRepository,
        tasksLayout: LinearLayout,
        context: Context
    ) {
        val updatedUnfulfilledTasks = goal.unfulfilledTasks.toMutableList()
        val updatedFulfilledTasks = goal.fulfilledTasks.toMutableList()

        if (isChecked) {
            updatedUnfulfilledTasks.remove(task)
            goalBaseStart.unfulfilledTasks.remove(task)
            updatedFulfilledTasks.add(task)
            goalBaseStart.fulfilledTasks.add(task)
        } else {
            updatedFulfilledTasks.remove(task)
            goalBaseStart.fulfilledTasks.add(task)
            updatedUnfulfilledTasks.add(task)
            goalBaseStart.unfulfilledTasks.remove(task)
        }

        val updatedGoal = goal.copy(unfulfilledTasks = updatedUnfulfilledTasks, fulfilledTasks = updatedFulfilledTasks)
        lifecycleScope.launch (Dispatchers.IO) {
            goalsRepository.updateGoal(updatedGoal, this@GoalView)

            // Здесь мы проверяем, пуст ли список невыполненных задач
            if (updatedUnfulfilledTasks.isEmpty()) {
                // Получаем доступ к базе данных и достаем задание с идентификатором
                val database = GoalsDatabase.getDatabase(context)
                val taskDao = database.taskDao()
                val userDao = database.userDao()
                val user = userDao.getAll().firstOrNull()

                val task3 = taskDao.getAll()[2]
                val task10 = taskDao.getAll()[3]
                val task50 = taskDao.getAll()[4]

                // Увеличиваем прогресс на 1
                if(task3.progress < 3) { task3.progress += 1 }
                if(task10.progress < 10) { task10.progress += 1 }
                if(task50.progress < 50) { task50.progress += 1 }

                if (task3.progress >= 3 && !task3.isCompleted) {
                    task3.isCompleted = true
                    if (user != null) {
                        user.experience += task3.reward
                    }
                }
                if (task10.progress >= 10 && !task10.isCompleted) {
                    task10.isCompleted = true
                    if (user != null) {
                        user.experience += task10.reward
                    }
                }
                if (task50.progress >= 50 && !task50.isCompleted) {
                    task50.isCompleted = true
                    if (user != null) {
                        user.experience += task50.reward
                    }
                }

                taskDao.update(task3)
                taskDao.update(task10)
                taskDao.update(task50)
                if (user != null) {
                    userDao.update(user)
                }
            }

            withContext(Dispatchers.Main) {
                tasksLayout.removeAllViews()
                updatedUnfulfilledTasks.forEach { unfulfilledTask ->
                    createTaskLayout(context, unfulfilledTask, updatedGoal, goalsRepository, tasksLayout)
                }

                val completedTasksLayout = LinearLayout(context)
                completedTasksLayout.orientation = LinearLayout.VERTICAL
                completedTasksLayout.setBackgroundResource(R.drawable.box_task_completed)

                displayFulfilledTasks(updatedGoal, completedTasksLayout, context)
//                displayFulfilledTasks(updatedGoal, tasksLayout, context)
                tasksLayout.addView(completedTasksLayout)
            }
        }
    }




    fun addNewEditText(addTaskButton: Button, context: Context) {
        //val addTaskButton = findViewById<Button>(R.id.AddTask)
        countUseAddTask++
        if (countUseAddTask > 1){
//            addNewTextView(addTaskButton, context)
            addTask()
        }

        if (countUseAddTask > 0) {

            // Создаем новый EditText
            val editText = EditText(context)
            // Устанавливаем его идентификатор и текст по умолчанию
            val idEditText = View.generateViewId()
            editText.id = idEditText
            lastId = idEditText
            ids.add(idEditText)
            editText.hint = context.getString(R.string.HintTask)

            // Устанавливаем параметры размещения для нового EditText
            val layoutParams = LinearLayout.LayoutParams(
                (resources.displayMetrics.widthPixels * 0.7).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.gravity = Gravity.CENTER
            layoutParams.topMargin = resources.getDimension(R.dimen.margin_10dp).toInt()

            // Добавляем новый EditText в LinearLayout под ScrollView
            val linearLayout = addTaskButton.parent as LinearLayout
            val index = linearLayout.indexOfChild(addTaskButton)
            linearLayout.addView(editText, index, layoutParams)

            // Сдвигаем кнопку addTaskButton вниз на 15dp
            val createTargetButtonLayoutParams =
                addTaskButton.layoutParams as LinearLayout.LayoutParams
            createTargetButtonLayoutParams.topMargin += resources.getDimension(R.dimen.margin_15dp)
                .toInt()
            addTaskButton.layoutParams = createTargetButtonLayoutParams
        }
    }

    fun addTask(){
        val context = this
        val additionalTextView = TextView(context)
        val textInEditText = findViewById<EditText>(lastId)
        additionalTextView.text = textInEditText.text.toString()
        goalBaseStart.unfulfilledTasks.add(textInEditText.text.toString())
        additionalTextView.gravity = Gravity.CENTER
        additionalTextView.setBackgroundColor(Color.LTGRAY)
        val typedValue = TypedValue()
        val theme = context.getTheme()
        theme.resolveAttribute(com.google.android.material.R.attr.colorSecondaryContainer, typedValue, true)
        additionalTextView.setTextColor(typedValue.data)
        additionalTextView.setPadding(
            dpToPx(16f, context),
            dpToPx(8f, context),
            dpToPx(16f, context),
            dpToPx(8f, context)
        )
        val parentLayout = textInEditText.parent as ViewGroup // Получаем родительский ViewGroup
        parentLayout.removeView(textInEditText)

        linearLayout.addView(additionalTextView, layoutParams)
    }

    fun addNewTextView(addTaskButton: Button, context: Context) {
        if (lastId != -1){
            // Создаем новый TextView
            val textView = TextView(context)
            // Устанавливаем его идентификатор и текст по умолчанию
            val idTextView = View.generateViewId()
            textView.id = idTextView
            ids.add(idTextView)
            val textInEditText = findViewById<EditText>(lastId)
            textView.text = textInEditText.text.toString()
            goalBaseStart.unfulfilledTasks.add(textInEditText.text.toString())

            // Устанавливаем параметры размещения для нового TextView
            val layoutParams = LinearLayout.LayoutParams(
                (resources.displayMetrics.widthPixels * 0.7).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.gravity = Gravity.CENTER
            layoutParams.topMargin = resources.getDimension(R.dimen.margin_10dp).toInt()

            // Устанавливаем параметры внешнего вида для нового TextView
            textView.setBackgroundColor(Color.LTGRAY)
            val typedValue = TypedValue()
            val theme = context.getTheme()
            theme.resolveAttribute(com.google.android.material.R.attr.colorSecondaryContainer, typedValue, true)
            textView.setTextColor(typedValue.data)
            textView.setPadding(
                dpToPx(16f, context),
                dpToPx(8f, context),
                dpToPx(16f, context),
                dpToPx(8f, context)
            )

            // Добавляем новый TextView в LinearLayout под ScrollView
            val linearLayout = addTaskButton.parent as LinearLayout
            val index = linearLayout.indexOfChild(addTaskButton)
            linearLayout.addView(textView, index, layoutParams)
        }
    }


    fun dpToPx(dp: Float, context: Context): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun GoToMain(view: View){
        val context = this
//        if (countUseAddTask > 0){
//            val database = GoalsDatabase.getDatabase(context)
//            val goalDao = database.goalDao()
//            val userDao = database.userDao()
//            val taskDao = database.taskDao()
//            val goalsRepository = OfflineGoalsRepository(goalDao, userDao, taskDao)
//            lifecycleScope.launch {
////                val goal = goalsRepository.getGoalStream(idGoal, context).firstOrNull()
////                if (goal != null){
////                    val newNameGoal = goal.name
////                    var newTasks = goal.tasks
////                    if (ids.isNotEmpty()){
////                        ids.forEach{id ->
////                            val editText = findViewById<EditText>(id)
////                            newTasks.add(editText.text.toString())
////                        }
////                    }
////                    val newGoal = GoalBase(
////                        id = goal.id,
////                        name = newNameGoal,
////                        tasks = newTasks
////                    )
////              }
////                    goalsRepository.updateGoal(newGoal, context)
//                val editText = findViewById<EditText>(lastId)
//                goalBaseStart.unfulfilledTasks.add(editText.text.toString())
//                goalsRepository.updateGoal(goalBaseStart, context)
//            }
//        }

        val NameGoal = findViewById<EditText>(R.id.NameGoal)
        val DescriptionGoal = findViewById<EditText>(R.id.DescriptionGoal)
        if (useDeadLineButton) {
            var deadline = selectedDateTime?.timeInMillis ?: 0L
            goalBaseStart.deadline = deadline
        }
        goalBaseStart.name = NameGoal.text.toString()
        goalBaseStart.description = DescriptionGoal.text.toString()

        lifecycleScope.launch (Dispatchers.IO){
            val database = GoalsDatabase.getDatabase(context)
            val goalDao = database.goalDao()
            goalDao.update(goalBaseStart)
        }

        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}