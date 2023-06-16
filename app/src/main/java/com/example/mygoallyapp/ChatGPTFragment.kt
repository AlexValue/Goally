package com.example.mygoallyapp

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import java.net.URLEncoder
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.mygoallyapp.Data.GoalsDatabase
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatGPTFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatGPTFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)   // время ожидания подключения
        .writeTimeout(20, TimeUnit.SECONDS)     // время ожидания записи
        .readTimeout(30, TimeUnit.SECONDS)      // время ожидания чтения
        .build()
    lateinit var sendGoal: EditText
    lateinit var send: ImageButton
    lateinit var txtResponse: ScrollView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_gpt, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sendGoal = view.findViewById(R.id.Goal)
        send = view.findViewById(R.id.sendButton)
        txtResponse = view.findViewById(R.id.tasks)


        send.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val database = GoalsDatabase.getDatabase(requireContext())
                val userDao = database.userDao()
                var user = userDao.getAll()[0]

                if(user.countUseChatGPT >= 5){
                    withContext(Dispatchers.Main) {
                        showTasksInScrollView("Вы истратили количество запросов на день", txtResponse, requireContext())
                    }
                } else {
                    user.countUseChatGPT++
                    userDao.update(user)

                    withContext(Dispatchers.Main) {
                        showTasksInScrollView("Пожалуйста, подождите...", txtResponse, requireContext())

                        val question = sendGoal.text.toString().trim()
                        if(question.isNotEmpty()){
                            getResponse(question) { response ->
                                activity?.runOnUiThread {
                                    showTasksInScrollView(response, txtResponse, requireContext())
                                }
                            }
                        }
                    }
                }
            }
        }

        // Спрятать информацию о роботе после начала ввода текста
        sendGoal.addTextChangedListener(object : TextWatcher {
            private var isInputStarted = false
            private val relativeLayout = requireView().findViewById<RelativeLayout>(R.id.infoRobotHelp)
            private val frameLayout = requireView().findViewById<FrameLayout>(R.id.inputField)
            private val tasksScrollView = requireView().findViewById<ScrollView>(R.id.tasks)
            private val titleRobotHelp = requireView().findViewById<TextView>(R.id.titleRobotHelp)

            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isInputStarted) {
                    isInputStarted = true
                    val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
                    relativeLayout.startAnimation(animation)
                    relativeLayout.visibility = View.GONE
                    val examplesFrameLayout = requireView().findViewById<FrameLayout>(R.id.examples)
                    examplesFrameLayout.visibility = View.GONE

                    // Сдвигаем frameLayout и tasks вверх
                    val titleBottom = titleRobotHelp.bottom
                    val frameLayoutTranslationY = (titleBottom - frameLayout.top + 100).toFloat()
                    val tasksScrollViewTranslationY = (titleBottom - tasksScrollView.top + 300).toFloat()

                    frameLayout.animate().translationY(frameLayoutTranslationY).duration = 400
                    tasksScrollView.animate().translationY(tasksScrollViewTranslationY).duration = 400
                }
            }
        })
    }

    fun showTasksInScrollView(goalsString: String, scrollView: ScrollView, context: Context) {
        val trimmedGoalsString = if (goalsString.startsWith("\n\n---")) {
            goalsString.substring(5)
        } else {
            goalsString
        }
        val goals = trimmedGoalsString.split("\n---")


        val linearLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val margin1 = dpToPx(10f, context)
        val margin2 = dpToPx(20f, context)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(margin2, margin1, margin2, 0)
        }

        goals.forEach { goal ->
            val textView = TextView(context).apply {
                text = goal
                gravity = Gravity.LEFT
                setTextColor(Color.BLACK)
                setBackgroundResource(R.drawable.goal_item_background)
                setPadding(
                    dpToPx(16f, context),
                    dpToPx(16f, context),
                    dpToPx(16f, context),
                    dpToPx(16f, context) // Reduce padding to minimize space
                )
            }

            linearLayout.addView(textView, layoutParams)
        }

        scrollView.removeAllViews()
        scrollView.addView(linearLayout)
    }

    fun dpToPx(dp: Float, context: Context): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun getResponse(question: String, callback: (String) -> Unit){
        val encodedInput = URLEncoder.encode(question, "UTF-8")

        val request = Request.Builder()
            .url("https://famous-marzipan-f2f861.netlify.app/.netlify/functions/api?question=$encodedInput")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error","API failed",e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body=response.body?.string()
                if (body != null) {
                    Log.v("data",body)
                }
                else{
                    Log.v("data","empty")
                }
                val jsonObject= JSONObject(body)
                val jsonArray: JSONArray =jsonObject.getJSONArray("choices")
                val textResult=jsonArray.getJSONObject(0).getString("text")
                callback(textResult)
            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChatGPTFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatGPTFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
