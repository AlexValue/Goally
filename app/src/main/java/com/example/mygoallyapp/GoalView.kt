package com.example.mygoallyapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.example.mygoallyapp.Data.GoalBase
import com.example.mygoallyapp.Data.GoalsDatabase
import com.example.mygoallyapp.Data.OfflineGoalsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class GoalView : AppCompatActivity() {
    var idGoal = -1
    var ids = mutableListOf<Int>()
    var addTaskUse = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_view)

        val intent = getIntent()
        val goalId = intent.getIntExtra("goal_id", -1)
        idGoal = goalId

        showGoalInfo(goalId, this)

        val addTaskButton = findViewById<Button>(R.id.AddTask)
        addTaskButton.setOnClickListener {
            addNewEditText(this)
        }
    }

    fun showGoalInfo(id: Int, context: Context) {
        val database = GoalsDatabase.getDatabase(context)
        val goalDao = database.goalDao()
        val goalsRepository = OfflineGoalsRepository(goalDao)

        GlobalScope.launch(Dispatchers.Main) {
            val goal = goalsRepository.getGoalStream(id, context).firstOrNull()
            if (goal != null) {
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

                val tasksTextView = TextView(context)
                tasksTextView.text = "${goal.tasks.joinToString(separator = "\n\n")}"
                tasksTextView.gravity = Gravity.CENTER
                tasksTextView.setBackgroundColor(Color.LTGRAY)
                tasksTextView.setTextColor(Color.BLACK)
                tasksTextView.setPadding(
                    dpToPx(16f, context),
                    dpToPx(8f, context),
                    dpToPx(16f, context),
                    dpToPx(8f, context)
                )

                val linearLayout = LinearLayout(context)
                linearLayout.orientation = LinearLayout.VERTICAL
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                linearLayout.addView(nameTextView, layoutParams)
                linearLayout.addView(tasksTextView, layoutParams)

                val scrollView = findViewById<ScrollView>(R.id.scrollView)
                scrollView.removeAllViews()
                scrollView.addView(linearLayout)
            } else {
                // Обработка ситуации, когда goal равен null
                Toast.makeText(context, "Goal not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun addNewEditText(context: Context) {
        val addTaskButton = findViewById<Button>(R.id.AddTask)
        addTaskUse = true

        // Создаем новый EditText
        val editText = EditText(context)
        // Устанавливаем его идентификатор и текст по умолчанию
        val idEditText = View.generateViewId()
        editText.id = idEditText
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



    fun dpToPx(dp: Float, context: Context): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun GoToMain(view: View){
        val context = this
        if (addTaskUse){
            val database = GoalsDatabase.getDatabase(context)
            val goalDao = database.goalDao()
            val goalsRepository = OfflineGoalsRepository(goalDao)
            GlobalScope.launch(Dispatchers.Main) {
                val goal = goalsRepository.getGoalStream(idGoal, context).firstOrNull()
                if (goal != null){
                    val newNameGoal = goal.name
                    var newTasks = goal.tasks
                    if (ids.isNotEmpty()){
                        ids.forEach{id ->
                            val editText = findViewById<EditText>(id)
                            newTasks.add(editText.text.toString())
                        }
                    }
                    val newGoal = GoalBase(
                        id = goal.id,
                        name = newNameGoal,
                        tasks = newTasks
                    )
                    goalsRepository.updateGoal(newGoal, context)
                }
            }
        }

        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}