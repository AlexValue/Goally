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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scrollView = findViewById<ScrollView>(R.id.listGoals)
        val goal = intent.getParcelableExtra<Goal>("goal")
        if (goal != null){
            goals.add(goal)
        }
        if (goals.count() > 0) {
            showGoalsInScrollView(goals, scrollView, this)

//            Toast.makeText(
//                this,
//                "Goal name: ${goal?.NameGoal}, tasks: ${goal?.Tasks}",
//                Toast.LENGTH_SHORT
//            ).show()
        }
    }

    fun showGoalsInScrollView(goals: List<Goal>, scrollView: ScrollView, context: Context) {
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.VERTICAL
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        goals.forEach { goal ->
            val textView = TextView(context)
            textView.text = goal.NameGoal
            textView.gravity = Gravity.CENTER
            textView.setBackgroundColor(Color.LTGRAY)
            textView.setTextColor(Color.BLACK)
            textView.setPadding(
                dpToPx(16f, context),
                dpToPx(8f, context),
                dpToPx(16f, context),
                dpToPx(8f, context)
            )

            linearLayout.addView(textView, layoutParams)
        }

        scrollView.addView(linearLayout)
    }

    fun dpToPx(dp: Float, context: Context): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    var goals = mutableListOf<Goal>()

    fun GoToAddGoal(view: View) {
        val intent = Intent(this, CreateGoal::class.java)
        startActivity(intent)
    }

}


