package com.example.mygoallyapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mygoallyapp.Data.GoalBase
import com.example.mygoallyapp.Data.GoalsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GoalsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GoalsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_goals, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentDateTextView: TextView = view.findViewById(R.id.current_date_text_view)
        val dateFormat = SimpleDateFormat("EEEE, d MMMM, yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        currentDateTextView.text = currentDate

        val scrollView = view.findViewById<ScrollView>(R.id.listGoals)
        val database = GoalsDatabase.getDatabase(requireContext())
        val goalDao = database.goalDao()

        lifecycleScope.launch(Dispatchers.IO) {
            val goalsBase = goalDao.getAllGoals()
            withContext(Dispatchers.Main) {
                showGoalsInScrollView(goalsBase, scrollView, requireContext())
            }

            val userDao = database.userDao()
            val currentUser = userDao.getAll().firstOrNull()

            val currentDateTime = System.currentTimeMillis()

            if (currentUser?.isLastLoginOlderThanOneDay(currentDateTime) == true) {
                val taskDao = database.taskDao()
                taskDao.resetDailyTasks()

                currentUser.lastLogin = currentDateTime
                val currentTask = taskDao.getAll()[1]
                currentTask.isCompleted = true
                currentUser.experience += currentTask.reward

                userDao.update(currentUser)
                taskDao.update(currentTask)
            }
        }

        // Приветствие в main
        val greetingTextView: TextView = view.findViewById(R.id.goals)
        val currentTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val morningStart = 5
        val dayStart = 12
        val eveningStart = 18


        val greeting: String = when (currentTime) {
            in morningStart until dayStart -> getString(R.string.morning_greeting)
            in dayStart until  eveningStart ->getString(R.string.day_greeting)
            in eveningStart until 24 -> getString(R.string.evening_greeting)
            else -> getString(R.string.night_greeting)
        }
        greetingTextView.text = greeting
    }


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
                    val deadline = goal.deadline

                    // Check if the deadline is not zero
                    if (deadline != 0L) {
                        val deadlineDate = Date(deadline)

                        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                        val deadlineYear = Calendar.getInstance().apply { time = deadlineDate }.get(
                            Calendar.YEAR)
                        val sdf = if (deadlineYear == currentYear) {
                            SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                        } else {
                            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        }

                        deadlineTextView.text = "${sdf.format(deadlineDate)}"
                        deadlineTextView.setTextColor(Color.parseColor("#FF7A00"))
                        deadlineTextView.setPadding(
                            dpToPx(16f, context),
                            dpToPx(4f, context), // Reduce padding to minimize space
                            dpToPx(16f, context),
                            dpToPx(4f, context) // Reduce padding to minimize space
                        )
                    }

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
                    if (deadline != 0L) {
                        goalLayout.addView(deadlineTextView)
                    }
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

    fun dpToPx(dp: Float, context: Context): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GoalsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GoalsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}