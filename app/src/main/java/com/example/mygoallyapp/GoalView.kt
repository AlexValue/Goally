package com.example.mygoallyapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.example.mygoallyapp.Data.GoalsDatabase
import com.example.mygoallyapp.Data.OfflineGoalsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class GoalView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_view)

        val intent = getIntent()
        val goalId = intent.getIntExtra("goal_id", -1)

        showGoalInfo(goalId, this)
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


    fun dpToPx(dp: Float, context: Context): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun GoToMain(view: View){
        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}