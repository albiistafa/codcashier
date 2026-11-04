package dev.codcow.kasirku.ui.components
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.widthIn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.IconButton
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.ui.theme.AppTheme

/** The v3 green band: title row with pill actions, then whatever sits on the band (search, tabs). */
@Composable
fun GreenBand(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 22.dp),
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val hero = AppTheme.colors.hero.first()
    Column(
        modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(0f to hero, 0.7f to hero.copy(alpha = 0.92f), 1f to MaterialTheme.colorScheme.background))
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp).background(AppTheme.colors.onHero.copy(alpha = 0.18f), CircleShape)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = AppTheme.colors.onHero)
                }
            }
            Text(
                title,
                style = AppTheme.typography.heading4Bold,
                color = AppTheme.colors.onHero,
                modifier = Modifier.weight(1f).semantics { heading() }
            )
            actions()
        }
        content()
    }
}

/** Translucent pill button that sits on the green band. */
@Composable
fun BandPill(onClick: () -> Unit, enabled: Boolean = true, content: @Composable RowScope.() -> Unit) {
    val onHero = AppTheme.colors.onHero
    Surface(onClick = onClick, enabled = enabled, shape = CircleShape, color = onHero.copy(alpha = 0.18f), contentColor = onHero) {
        Row(
            Modifier.heightIn(min = 40.dp).padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            content = content
        )
    }
}

/** White search pill on the band. */
@Composable
fun SearchPill(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onFilterClick: (() -> Unit)? = null
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder, style = AppTheme.typography.paragraph1) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = onFilterClick?.let { open ->
            { IconButton(onClick = open) { Icon(Icons.Default.Tune, contentDescription = "Filter", tint = MaterialTheme.colorScheme.primary) } }
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        textStyle = AppTheme.typography.paragraph1,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier.fillMaxWidth().semantics { contentDescription = placeholder }
    )
}

/** Top bar for pushed screens (forms, details): the band with a back button and title only. */
@Composable
fun BandTopBar(title: String, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    GreenBand(
        title = title,
        onBack = onBack,
        modifier = Modifier.background(AppTheme.colors.hero.first()).statusBarsPadding(),
        contentPadding = PaddingValues(start = 12.dp, end = 16.dp, top = 10.dp, bottom = 18.dp),
        actions = actions
    )
}

/** Lets a header inside a padded column paint edge to edge. */
fun Modifier.fullBleed(inset: Dp) = layout { measurable, constraints ->
    val extra = inset.roundToPx() * 2
    val placeable = measurable.measure(constraints.copy(minWidth = constraints.maxWidth + extra, maxWidth = constraints.maxWidth + extra))
    layout(placeable.width - extra, placeable.height) { placeable.place(-extra / 2, 0) }
}

/** Content column on wide windows: full width on phones, centred at 640dp on tablets so rows don't stretch. */
fun Modifier.readableWidth() = fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).widthIn(max = 640.dp)
