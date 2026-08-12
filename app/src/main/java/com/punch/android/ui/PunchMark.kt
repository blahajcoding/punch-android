package com.punch.android.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.punch.android.R
import com.punch.android.ui.theme.PunchMint

internal val emptyGreetings = listOf(
    "The mic is yours",
    "I left the kettle on",
    "Pi's already judging",
    "Ask me like you mean it",
    "No thoughts, just vibes",
    "I'll try not to hallucinate",
    "Your intern is ready",
    "Say the quiet part",
    "We can still undo this",
    "What's cooking, human",
    "I brought the green accent",
    "Type something unwise",
)

@Composable
fun PunchMark(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.punch_mark),
        contentDescription = null,
        colorFilter = ColorFilter.tint(PunchMint),
        modifier = modifier
            .size(36.dp)
            .testTag("punch_mark"),
    )
}
