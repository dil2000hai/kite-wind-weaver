package com.kitewind.weaver.presentation.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kitewind.weaver.R
import com.kitewind.weaver.core.ui.*

@Composable
fun PauseDialog(onResume: () -> Unit, onLand: () -> Unit, onMenu: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.pause_title)) },
        text = { Text(stringResource(R.string.pause_message)) },
        confirmButton = { PrimaryButton(R.string.resume, onResume) },
        dismissButton = { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryButton(R.string.land, onLand)
            MenuButton(onMenu)
        } })
}
