package com.example.mygoallyapp

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
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

    private val client = OkHttpClient()
    lateinit var sendGoal: EditText
    lateinit var send: ImageButton
    lateinit var txtResponse: ScrollView
//    lateinit var txtResponse: TextView

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

        // set onClickListener for 'send' button instead of 'sendGoal'
        send.setOnClickListener {
            // setting response tv on below line.
            showGoalsInScrollView("Пожалуйста, подождите...", txtResponse, requireContext())
//            txtResponse.text = "Please wait.."

            // validating text
            val question = sendGoal.text.toString().trim()
            Toast.makeText(context,question, Toast.LENGTH_SHORT).show()
            if(question.isNotEmpty()){
                getResponse(question) { response ->
                    activity?.runOnUiThread {
                        showGoalsInScrollView(response, txtResponse, requireContext())
//                        txtResponse.text = response
                    }
                }
            }
        }
    }

    fun showGoalsInScrollView(goalsString: String, scrollView: ScrollView, context: Context) {
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

        // setting text on for question on below line.
        //sendGoal.setText("")

        val apiKey="YOUR_API"
        val url="https://api.openai.com/v1/engines/text-davinci-003/completions"
        val handledQuestion = "Предоставьте краткий список задач для цели '$question' в следующем формате: \\n---текст подзадачи\\n---текст подзадачи\\n---текст подзадачи\\n\\nЗадачи должны быть краткими и без дополнительных деталей."
        val requestBody="""
            {
            "prompt": "$handledQuestion",
            "max_tokens": 350,
            "temperature": 0
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
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
