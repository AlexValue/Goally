package com.example.mygoallyapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mygoallyapp.Data.GoalBase
import com.example.mygoallyapp.Data.GoalsDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentDateTextView: TextView = findViewById(R.id.current_date_text_view)
        val dateFormat = SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        currentDateTextView.text = currentDate

        //Добавление нужных в коде элементов интерфеса и получение доступа к базе данных
        val scrollView = findViewById<ScrollView>(R.id.listGoals)
        val database = GoalsDatabase.getDatabase(application)
        val goalDao = database.goalDao()
        var goalsBase = goalDao.getAllGoals()
        showGoalsInScrollView(goalsBase, scrollView, this)
    }


    /**
    Эта функция принимает поток [Flow] списка [List] [GoalBase] и отображает цели в [ScrollView] с
    [TextView] для каждой цели. Функция использует [lifecycleScope.launch] для сбора данных из потока
    в корутине и обновляет представление на основном потоке, используя [ScrollView] и [LinearLayout].

    @param goals - [Flow] из [List] элементов [GoalBase], представляющих список целей, которые будут отображаться.
    @param scrollView - [ScrollView], где будут отображаться [TextView] с целями.
    @param context - [Context], используется для создания [LinearLayout] и для преобразования dp в пиксели.
     */
    fun showGoalsInScrollView(goals: Flow<List<GoalBase>>, scrollView: ScrollView, context: Context) {
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.VERTICAL

        val margin1 = dpToPx(10f, context)
        val margin2 = dpToPx(20f, context)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(margin2, margin1, margin2, 0)
        }

        lifecycleScope.launch {
            goals.collect { goalList ->
                linearLayout.removeAllViews()
                goalList.forEach { goal ->
                    val goalLayout = LinearLayout(context)
                    goalLayout.orientation = LinearLayout.VERTICAL
                    goalLayout.setBackgroundResource(R.drawable.goal_item_background)
                    goalLayout.setOnClickListener {
                        val intent = Intent(context, GoalView::class.java)
                        intent.putExtra("goal_id", goal.id)
                        context.startActivity(intent)
                    }

                    // Create GoalBase name TextView
                    val textView = TextView(context)
                    textView.text = goal.name
                    textView.id = goal.id
                    textView.gravity = Gravity.LEFT
                    textView.setTextColor(Color.BLACK)
                    textView.setPadding(
                        dpToPx(16f, context),
                        dpToPx(16f, context),
                        dpToPx(16f, context),
                        dpToPx(4f, context) // Reduce padding to minimize space
                    )

                    // Create Deadline TextView
                    val deadlineTextView = TextView(context)
                    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                    deadlineTextView.text = "${sdf.format(goal.getDeadlineAsDate())}"
                    deadlineTextView.setTextColor(Color.parseColor("#FF7A00"))
                    deadlineTextView.setPadding(
                        dpToPx(16f, context),
                        dpToPx(4f, context), // Reduce padding to minimize space
                        dpToPx(16f, context),
                        dpToPx(4f, context) // Reduce padding to minimize space
                    )

                    // Create Tasks ratio TextView
                    val tasksRatioTextView = TextView(context)
                    tasksRatioTextView.text = "${goal.fulfilledTasks.size - 1} / ${goal.allTask}"
                    tasksRatioTextView.setTextColor(Color.parseColor("#909090"))
                    tasksRatioTextView.setPadding(
                        dpToPx(16f, context),
                        dpToPx(4f, context), // Reduce padding to minimize space
                        dpToPx(16f, context),
                        dpToPx(4f, context) // Reduce padding to minimize space
                    )

                    // Create Tasks ratio ProgressBar
                    val tasksRatioProgressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
                    tasksRatioProgressBar.progressDrawable = ContextCompat.getDrawable(context, R.drawable.custom_progress_bar)
                    tasksRatioProgressBar.max = goal.allTask
                    tasksRatioProgressBar.progress = goal.fulfilledTasks.size - 1
                    tasksRatioProgressBar.setPadding(
                        dpToPx(16f, context),
                        dpToPx(4f, context), // Reduce padding to minimize space
                        dpToPx(16f, context),
                        dpToPx(16f, context)
                    )

                    // Add views to the goalLayout
                    goalLayout.addView(textView)
                    goalLayout.addView(deadlineTextView)
                    goalLayout.addView(tasksRatioTextView)
                    goalLayout.addView(tasksRatioProgressBar)

                    // Add goalLayout to the main layout
                    linearLayout.addView(goalLayout, layoutParams)
                }

                scrollView.removeAllViews()
                scrollView.addView(linearLayout)
            }
        }
    }



    /**
    Функция получает масштаб пикселей на экране устройства и умножает на количество dp,
    чтобы получить эквивалентное количество пикселей в зависимости от разрешения экрана.
    Также добавляет 0.5f и преобразуем результат в целое число, чтобы обеспечить правильное округление

    @param dp - конвертируемое значение
    @param context - переменная позволяет получать разрешение экрана
    @return dp
     */
    fun dpToPx(dp: Float, context: Context): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun GoToAddGoal(view: View) {
        val intent = Intent(this, CreateGoal::class.java)
        startActivity(intent)
    }
}


