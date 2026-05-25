package com.coderGtm.yantra.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderGtm.yantra.R

private val BubbleBlue = Color(0xFF2962FF)

private fun chevronPath(size: Size): Path = Path().apply {
    moveTo(0f, 0f)
    lineTo(size.width, size.height / 2f)
    lineTo(0f, size.height)
    close()
}

/**
 * Right-pointing triangle. Uses fillMaxHeight() so it always matches the
 * height of its parent Row (which must declare height = IntrinsicSize.Min).
 */
@Composable
private fun RightChevron(color: Color, width: Dp = 14.dp) {
    Canvas(
        modifier = Modifier
            .fillMaxHeight()
            .width(width)
    ) {
        drawPath(chevronPath(this.size), color)
    }
}

/**
 * The blue pill: [white-icon-section][white-chevron][username-text]
 *
 * Height is driven purely by the Text (via IntrinsicSize.Min on the Row).
 * The icon has a fixed size and is centered in the white section — it does
 * NOT use fillMaxHeight so it cannot inflate the pill's intrinsic height.
 */
@Composable
private fun PromptPill(
    username: String,
    fontSize: Float,
    fontFamily: FontFamily?
) {
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
            .background(BubbleBlue),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // White left section: fills the pill height, icon is fixed-size and centered
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(32.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_android),
                contentDescription = null,
                modifier = Modifier.size((fontSize * 0.9f).dp), // scales with font size
                tint = Color.Black
            )
        }

        // White chevron separator
        RightChevron(color = Color.White, width = 10.dp)

        // Username — sole driver of pill's intrinsic height
        Text(
            text = username,
            color = Color.White,
            fontSize = fontSize.sp,
            fontFamily = fontFamily,
            lineHeight = fontSize.sp,
            modifier = Modifier.padding(start = 2.dp, end = 6.dp, top = 0.dp, bottom = 0.dp)
        )
    }
}

/**
 * Static input-line prompt: pill + trailing blue chevron before the EditText.
 */
@Composable
fun ModernInputPrompt(
    username: String,
    fontSize: Float = 16f,
    fontFamily: FontFamily? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .padding(vertical = 3.dp)   // outer spacing — keeps pill compact inside
    ) {
        PromptPill(username = username, fontSize = fontSize, fontFamily = fontFamily)
        RightChevron(color = BubbleBlue, width = 10.dp)
    }
}

/**
 * Per-command chat bubble in the terminal output history.
 */
@Composable
fun ModernChatBubble(
    username: String,
    command: String,
    commandColor: Color,
    fontSize: Float = 16f,
    fontFamily: FontFamily? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .padding(vertical = 3.dp)   // same outer spacing as input prompt
    ) {
        PromptPill(username = username, fontSize = fontSize, fontFamily = fontFamily)
        RightChevron(color = BubbleBlue, width = 10.dp)
        Text(
            text = command,
            color = commandColor,
            fontSize = fontSize.sp,
            fontFamily = fontFamily,
            lineHeight = fontSize.sp,
            modifier = Modifier.padding(start = 4.dp, top = 0.dp, bottom = 0.dp)
        )
    }
}
