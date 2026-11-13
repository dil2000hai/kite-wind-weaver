package com.kitewind.weaver.presentation.dialog

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kitewind.weaver.R
import com.kitewind.weaver.core.ui.SecondaryButton

@Composable
fun ResetMeadowDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.reset_title)) },
        text = { Text(stringResource(R.string.reset_message)) },
        confirmButton = { SecondaryButton(R.string.reset, onConfirm) },
        dismissButton = { SecondaryButton(R.string.cancel, onDismiss) })
}
