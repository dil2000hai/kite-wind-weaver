package com.kitewind.weaver.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kitewind.weaver.R
import com.kitewind.weaver.core.theme.*
import com.kitewind.weaver.domain.model.SaveStatus

@Composable
fun MeadowCard(modifier: Modifier = Modifier, selected: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = modifier, shape = RoundedCornerShape(18.dp), shadowElevation = if (selected) 6.dp else 3.dp,
        color = CloudWhite.copy(alpha = 0.91f),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) BlossomPrimary else CloudWhite.copy(alpha = 0.70f))) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun StatCard(labelRes: Int, value: Int, modifier: Modifier = Modifier) {
    MeadowCard(modifier) {
        Text(stringResource(R.string.number_value, value), style = MaterialTheme.typography.headlineSmall, color = BlossomPrimary)
        Text(stringResource(labelRes), style = MaterialTheme.typography.labelSmall, color = Ink)
    }
}

@Composable
fun GrowthMeter(grown: Int, target: Int, modifier: Modifier = Modifier) {
    val summary = stringResource(R.string.flowers_progress, grown, target)
    val fraction by animateFloatAsState(grown.toFloat() / target.coerceAtLeast(1), tween(if (LocalReducedMotion.current) 120 else 200), label = "growth")
    Column(modifier.semantics { stateDescription = summary }, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(summary, style = MaterialTheme.typography.bodyMedium, color = Ink)
        LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth().height(5.dp),
            color = BlossomPrimary, trackColor = DustyPink.copy(alpha = 0.45f))
    }
}

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(6.dp)) {
            Text(stringResource(R.string.back), color = BlossomPrimary)
        }
        Text(title, style = MaterialTheme.typography.headlineMedium, color = Ink, modifier = Modifier.weight(1f))
    }
}

@Composable
fun MenuButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = onClick, modifier = modifier.heightIn(min = 48.dp), shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = CloudWhite.copy(alpha = 0.9f)), border = BorderStroke(1.dp, DustyPink)) {
        Image(painterResource(R.drawable.icon_home), null, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.menu))
    }
}

@Composable
fun SaveStatusLine(status: SaveStatus, onRetry: () -> Unit) {
    if (status == SaveStatus.Saving || status == SaveStatus.SaveError) {
        MeadowCard(Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }) {
            Text(stringResource(if (status == SaveStatus.Saving) R.string.saving else R.string.save_error),
                style = MaterialTheme.typography.bodyMedium, color = Ink)
            if (status == SaveStatus.SaveError) SecondaryButton(R.string.retry_save, onRetry)
        }
    }
}

@Composable
fun SettingToggleRow(labelRes: Int, descriptionRes: Int, checked: Boolean, onChange: (Boolean) -> Unit) {
    val status = stringResource(if (checked) R.string.on_state else R.string.off_state)
    Row(Modifier.fillMaxWidth().heightIn(min = 72.dp).semantics(mergeDescendants = true) { stateDescription = status },
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(labelRes), style = MaterialTheme.typography.titleMedium, color = Ink)
            Text(stringResource(descriptionRes), style = MaterialTheme.typography.bodyMedium, color = Ink.copy(alpha = 0.85f))
        }
        Switch(checked = checked, onCheckedChange = onChange, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
            colors = SwitchDefaults.colors(checkedTrackColor = BlossomPrimary, checkedThumbColor = CloudWhite,
                uncheckedTrackColor = DustyPink, uncheckedThumbColor = Ink))
    }
}
