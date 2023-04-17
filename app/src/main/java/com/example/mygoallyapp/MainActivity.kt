package com.example.mygoallyapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.mygoallyapp.Data.GoalBase
import com.example.mygoallyapp.Data.GoalsDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        lifecycleScope.launch {
            goals.collect { goalList ->
                linearLayout.removeAllViews()
                goalList.forEach { goal ->
                    val textView = TextView(context)
                    textView.text = goal.name
                    textView.id = goal.id
                    textView.gravity = Gravity.CENTER
                    textView.setBackgroundColor(Color.LTGRAY)
                    textView.setTextColor(Color.BLACK)
                    textView.setPadding(
                        dpToPx(16f, context),
                        dpToPx(8f, context),
                        dpToPx(16f, context),
                        dpToPx(8f, context)
                    )
                    textView.setOnClickListener {
                        val intent = Intent(context, GoalView::class.java)
                        intent.putExtra("goal_id", textView.id)
                        context.startActivity(intent)
                    }


                    linearLayout.addView(textView, layoutParams)
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


