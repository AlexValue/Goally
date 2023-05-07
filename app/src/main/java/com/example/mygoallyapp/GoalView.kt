package com.example.mygoallyapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.mygoallyapp.Data.GoalBase
import com.example.mygoallyapp.Data.GoalsDatabase
import com.example.mygoallyapp.Data.OfflineGoalsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoalView : AppCompatActivity() {
    var idGoal = -1
    var ids = mutableListOf<Int>()
    var lastId = -1
    var countUseAddTask = 0
    var goalBaseStart = GoalBase( name = "", unfulfilledTasks = mutableListOf(), fulfilledTasks = mutableListOf())

    private lateinit var linearLayout: LinearLayout
    private lateinit var layoutParams: LinearLayout.LayoutParams
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_view)

        val intent = getIntent()
        val goalId = intent.getIntExtra("goal_id", -1)
        idGoal = goalId

        showGoalInfo1(goalId, this) { goalInfoView ->
            // Получаем корневой макет активности
            val mainLayout = findViewById<LinearLayout>(R.id.main_layout)

            // Удаляем все предыдущие дочерние элементы, если они есть
            mainLayout.removeAllViews()

            // Добавляем goalInfoView в корневой макет активности
            mainLayout.addView(goalInfoView)
        }
        //val addTaskButton = findViewById<Button>(R.id.AddTask)
//        addTaskButton.setOnClickListener {
//            addNewEditText(this)
//        }
    }

    fun showGoalInfo1(id: Int, context: Context, onGoalInfoReady: (View) -> Unit) {
        val database = GoalsDatabase.getDatabase(context)
        val goalDao = database.goalDao()
        val goalsRepository = OfflineGoalsRepository(goalDao)

        GlobalScope.launch(Dispatchers.Main) {
            val goal = goalsRepository.getGoalStream(id, context).firstOrNull()
            if (goal != null) {
                goalBaseStart = goal
                val rootLayout = LinearLayout(context)
                rootLayout.orientation = LinearLayout.VERTICAL

                val nameTextView = TextView(context)
                nameTextView.text = goal.name + "\n"
                nameTextView.textSize = 18f
                nameTextView.gravity = Gravity.CENTER
                nameTextView.setBackgroundColor(Color.LTGRAY)
                nameTextView.setTextColor(Color.BLACK)
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
                    createTaskLayout(context, task, goal, goalsRepository, tasksLayout)
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
        taskLayout.gravity = Gravity.END

        val taskTextView = TextView(context)
        taskTextView.text = task
        taskTextView.gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
        taskTextView.setBackgroundColor(Color.LTGRAY)
        taskTextView.setTextColor(Color.BLACK)
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
            if (!isChecked) {
                GlobalScope.launch {
                    moveTaskToEnd(goal, task, goalsRepository)
                    withContext(Dispatchers.Main) {
                        tasksLayout.removeView(taskLayout)
                        createTaskLayout(context, task, goal, goalsRepository, tasksLayout)
                    }
                }
            }
        }
        taskLayout.addView(taskTextView)
        taskLayout.addView(circleView, circleLayoutParams)
        tasksLayout.addView(taskLayout)
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
        updatedUnfulfilledTasks.add(task)

        val updatedGoal = goal.copy(unfulfilledTasks = updatedUnfulfilledTasks)
        goalsRepository.updateGoal(updatedGoal, this)
    }



    fun showGoalInfo(id: Int, context: Context) {
        val database = GoalsDatabase.getDatabase(context)
        val goalDao = database.goalDao()
        val goalsRepository = OfflineGoalsRepository(goalDao)

        GlobalScope.launch(Dispatchers.Main) {
            val goal = goalsRepository.getGoalStream(id, context).firstOrNull()
            if (goal != null) {
                goalBaseStart = goal
                val nameTextView = TextView(context)
                nameTextView.text = goal.name + "\n"
                nameTextView.textSize = 18f
                nameTextView.gravity = Gravity.CENTER
                nameTextView.setBackgroundColor(Color.LTGRAY)
                nameTextView.setTextColor(Color.BLACK)
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

                val tasksLayout = LinearLayout(context)
                tasksLayout.orientation = LinearLayout.VERTICAL

                goal.unfulfilledTasks.forEach { task ->
                    val taskLayout = LinearLayout(context)
                    taskLayout.orientation = LinearLayout.HORIZONTAL
                    taskLayout.gravity = Gravity.END

                    val taskTextView = TextView(context)
                    taskTextView.text = task
                    taskTextView.gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
                    taskTextView.setBackgroundColor(Color.LTGRAY)
                    taskTextView.setTextColor(Color.BLACK)
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
                    taskLayout.addView(taskTextView)
                    taskLayout.addView(circleView, circleLayoutParams)

                    tasksLayout.addView(taskLayout)
                }

                val tasksLayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                tasksLayoutParams.topMargin = dpToPx(16f, context)
                tasksLayoutParams.bottomMargin = dpToPx(16f, context)

                linearLayout = LinearLayout(context)
                linearLayout.orientation = LinearLayout.VERTICAL
                linearLayout.setBackgroundColor(Color.WHITE)
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                linearLayout.addView(nameTextView, nameLayoutParams)
                linearLayout.addView(tasksLayout, tasksLayoutParams)

                val scrollView = findViewById<ScrollView>(R.id.scrollView)
                scrollView.removeAllViews()
                scrollView.addView(linearLayout)

                val addButton = Button(context)
                addButton.text = "Добавить задачу"
                addButton.setBackgroundColor(Color.parseColor("#FF6200EE"))
                addButton.setTextColor(Color.WHITE)
                addButton.setPadding(
                    dpToPx(16f, context),
                    dpToPx(8f, context),
                    dpToPx(16f, context),
                    dpToPx(8f, context)
                )
                addButton.setOnClickListener {
                    addNewEditText(addButton ,context)
//                    val editText = EditText(context)
//                    editText.hint = "Введите задачу"
//                    editText.setPadding(
//                        dpToPx(16f, context),
//                        dpToPx(8f, context),
//                        dpToPx(16f, context),
//                        dpToPx(8f, context)
//                    )
//                    linearLayout.addView(editText, layoutParams)
                }
                linearLayout.addView(addButton, layoutParams)
            } else {
                // Обработка ситуации, когда goal равен null
                Toast.makeText(context, "Goal not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleCircleView(circleView: View, isChecked: Boolean) {
        if (isChecked) {
            circleView.setBackgroundResource(R.drawable.checkmark)
        } else {
            circleView.setBackgroundResource(R.drawable.circle)
        }
    }

    private fun displayFulfilledTasks(goal: GoalBase, tasksLayout:  LinearLayout, context: Context) {
        goal.fulfilledTasks.forEach { task ->
            val taskLayout = LinearLayout(context)
            taskLayout.orientation = LinearLayout.HORIZONTAL
            taskLayout.gravity = Gravity.END

            val taskTextView = TextView(context)
            taskTextView.text = task
            taskTextView.gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
            taskTextView.setBackgroundColor(Color.LTGRAY)
            taskTextView.setTextColor(Color.BLACK)
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



    fun addNewEditText(addTaskButton: Button, context: Context) {
        //val addTaskButton = findViewById<Button>(R.id.AddTask)
        countUseAddTask++
        if (countUseAddTask > 1){
//            addNewTextView(addTaskButton, context)
            addTask()
        }

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
        val createTargetButtonLayoutParams = addTaskButton.layoutParams as LinearLayout.LayoutParams
        createTargetButtonLayoutParams.topMargin += resources.getDimension(R.dimen.margin_15dp).toInt()
        addTaskButton.layoutParams = createTargetButtonLayoutParams
    }

    fun addTask(){
        val context = this
        val additionalTextView = TextView(context)
        val textInEditText = findViewById<EditText>(lastId)
        additionalTextView.text = textInEditText.text.toString()
        goalBaseStart.unfulfilledTasks.add(textInEditText.text.toString())
        additionalTextView.gravity = Gravity.CENTER
        additionalTextView.setBackgroundColor(Color.LTGRAY)
        additionalTextView.setTextColor(Color.BLACK)
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
            textView.setTextColor(Color.BLACK)
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
        if (countUseAddTask > 0){
            val database = GoalsDatabase.getDatabase(context)
            val goalDao = database.goalDao()
            val goalsRepository = OfflineGoalsRepository(goalDao)
            GlobalScope.launch(Dispatchers.Main) {
//                val goal = goalsRepository.getGoalStream(idGoal, context).firstOrNull()
//                if (goal != null){
//                    val newNameGoal = goal.name
//                    var newTasks = goal.tasks
//                    if (ids.isNotEmpty()){
//                        ids.forEach{id ->
//                            val editText = findViewById<EditText>(id)
//                            newTasks.add(editText.text.toString())
//                        }
//                    }
//                    val newGoal = GoalBase(
//                        id = goal.id,
//                        name = newNameGoal,
//                        tasks = newTasks
//                    )
//              }
//                    goalsRepository.updateGoal(newGoal, context)
                val editText = findViewById<EditText>(lastId)
                goalBaseStart.unfulfilledTasks.add(editText.text.toString())
                goalsRepository.updateGoal(goalBaseStart, context)
            }
        }

        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}