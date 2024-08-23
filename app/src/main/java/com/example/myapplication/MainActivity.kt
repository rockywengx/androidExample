package com.example.myapplication
import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.crepowermay.ezui.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodels.MainState
import com.example.myapplication.viewmodels.MainViewModel
import java.util.concurrent.CompletableFuture

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(viewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetState()
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Greeting(
                name = "Android",
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            val context = LocalContext.current
            // confirm dialog
            val confirmBox = remember {
                Confirm(
                    context,
                    "Confirm",
                    "Are you sure?",
                    {
                        val intent = Intent(context, DataBaseActivity::class.java)
                        intent.putExtra("confirm", viewModel.state.value?.confirm)
                        context.startActivity(intent)
                    },
                    {
                        viewModel.setState(MainState(""))
                    }
                )
            }
            // message dialog
            val message = remember {
                Message(
                    context
                )
            }

            // stateful button
            val statefulButton = remember {
                StatefulButton(
                    context
                )
            }
            val coroutineScope = rememberCoroutineScope()
            statefulButton.setEnabled(true)
            statefulButton.setText("開始")

            // 設定按鈕風格
            val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels
            val buttonWidth = screenWidth / 2
            var buttonParams =  RelativeLayout.LayoutParams(
                buttonWidth,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            buttonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
            buttonParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)

            statefulButton.setButtonLayoutParams(buttonParams)
            statefulButton.setAction { view ->
                CompletableFuture.supplyAsync {
                    val httpRequest = HttpRequest()
                    // 請求成功
                    httpRequest.get(
                        "http://10.0.2.2:3000/longTime",
                        { responseData ->

                            // 請求成功
                            viewModel.setState(MainState(responseData))
                            true
                        },
                        { throwable ->
                            message.show("error","Request failed")

                            // 請求失敗
                            throwable.printStackTrace()
                            false
                        }).join() // 等待請求完成
                    true
                }
            }
            statefulButton.setOnClickListener {
                /*coroutineScope.launch {
                    delay(4000)
                    confirmBox.show()
                }*/

                confirmBox.show()
            }

            AndroidView(
                factory = { statefulButton },
                modifier = Modifier.align(Alignment.Center)
            )

        }
    }
}

@Composable
fun Greeting(name: String, viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val appState = viewModel.state.observeAsState()

    Text(
        text = "Hello $name!, your selection is ${appState.value?.confirm}",
        modifier = modifier
    )
}
