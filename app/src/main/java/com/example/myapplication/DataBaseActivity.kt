package com.example.myapplication

import android.content.Intent
import android.graphics.Color
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
import com.crepowermay.ezui.collection.CardList
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodels.MainState
import java.util.concurrent.CompletableFuture

class DataBaseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DatabaseScreen(intent)
            }
        }
    }

    @Composable
    fun DatabaseScreen(intent: Intent) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                DataBaseText(
                    intent = intent,
                    modifier = Modifier.padding(innerPadding)
                )
                val context = LocalContext.current
                // confirm dialog
                val confirmBox = remember {
                    Confirm(
                        context, "Title", "Message", {
                            // do something
                        }, {
                            // do something
                        }
                    )
                }

                val cardList = remember {
                    CardList(context)
                }
                cardList.setCustomHeight(200)
                cardList.setCustomStyle(Color.WHITE, Color.BLACK)
                cardList.setData(listOf("1", "2", "3", "4", "5"))
                cardList.setOnLoadMoreListener { callback ->
                    // do something
                    CompletableFuture.supplyAsync {
                        val httpRequest = HttpRequest()
                        // 請求成功
                        httpRequest.get(
                            "http://10.0.2.2:3000/longTime",
                            { responseData ->

                                // 請求成功
                                callback.onDataLoaded(responseData.data)
                                true
                            },
                            { throwable ->

                                // 請求失敗
                                throwable.printStackTrace()
                                false
                            }).join() // 等待請求完成
                        true
                    }
                }

                var statefulButton = remember {
                    StatefulButton(
                        context
                    )
                }

                val coroutineScope = rememberCoroutineScope()
                statefulButton.setEnabled(true)
                statefulButton.setText("返回")

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

                statefulButton.setOnClickListener {

                    // 返回前一頁
                    finish()

                }

                AndroidView(
                    factory = { statefulButton },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun DataBaseText(intent: Intent, modifier: Modifier) {

    val confirm = intent.getStringExtra("confirm")

    Text(
        text = "這是第二頁，前一頁的值${confirm}",
        modifier = modifier
    )
}
