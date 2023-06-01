package com.example.mygoallyapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.mygoallyapp.Data.GoalsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MotivationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MotivationFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_motivation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val task1ProgressTextView: TextView = view.findViewById(R.id.dailyTask1Progress)
        val task2ProgressTextView: TextView = view.findViewById(R.id.dailyTask2Progress)

        val achievement1ProgressTextView: TextView = view.findViewById(R.id.achievement1Progress)
        val achievement2ProgressTextView: TextView = view.findViewById(R.id.achievement2Progress)
        val achievement3ProgressTextView: TextView = view.findViewById(R.id.achievement3Progress)

        val level: TextView = view.findViewById(R.id.level)
        val experience: TextView = view.findViewById(R.id.experience)

        lifecycleScope.launch(Dispatchers.IO) {
            val database = GoalsDatabase.getDatabase(requireContext())
            val taskDao = database.taskDao()
            val userDao = database.userDao()
            val tasks = taskDao.getAll()
            val user = userDao.getAll()[0]

            if (tasks[0].isCompleted && !tasks[0].rewardIssued){
                user.experience += tasks[0].reward
                tasks[0].rewardIssued = true
            }
            userDao.update(user)
            taskDao.update(tasks[0])

            var levelUser = user.experience / 100
            var experienceUser = user.experience % 100

            level.text = "Уровень: ${levelUser}"
            experience.text = "Опыт: ${experienceUser}"

            if (tasks.isNotEmpty()) {
                tasks[0]?.let { task ->
                    withContext(Dispatchers.Main) {
                        task1ProgressTextView.text = "${task.progress}/3"
                    }
                }
            }

            if (tasks.size > 1) {
                if (tasks[1].isCompleted){
                    tasks[1].progress = 1
                }
                tasks[1]?.let { task ->
                    withContext(Dispatchers.Main) {
                        task2ProgressTextView.text = "${task.progress}/1"
                    }
                }
            }

            if (tasks.size > 2) {
                tasks[2]?.let { task ->
                    withContext(Dispatchers.Main) {
                        achievement1ProgressTextView.text = "${task.progress}/3"
                    }
                }
            }

            if (tasks.size > 3) {
                tasks[3]?.let { task ->
                    withContext(Dispatchers.Main) {
                        achievement2ProgressTextView.text = "${task.progress}/10"
                    }
                }
            }

            if (tasks.size > 4) {
                tasks[4]?.let { task ->
                    withContext(Dispatchers.Main) {
                        achievement3ProgressTextView.text = "${task.progress}/50"
                    }
                }
            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MotivationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
