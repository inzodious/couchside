package couchside

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import couchside.shim.Apps

/** What opens when the app starts another app: the simulated TV only has the one app, so the rest are a name on a dark screen. */
class StandInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val name = Apps.label(componentName?.packageName, componentName?.packageName).toString()
        setContent {
            Box(Modifier.fillMaxSize().background(Color(0xFF0B0D12)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BasicText(name, style = TextStyle(color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold))
                    BasicText("This app would open here on the TV. Only ${couchside.shim.Target.label} runs in the simulator.", style = TextStyle(color = Color(0xFFB8BECB), fontSize = 14.sp))
                    BasicText("Esc goes back. Home returns to ${couchside.shim.Target.homeLabel}.", style = TextStyle(color = Color(0xFF7D8494), fontSize = 12.sp))
                }
            }
        }
    }
}
