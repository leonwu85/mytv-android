package top.yogiczy.mytv.ui.screens.leanback.settings.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.itemsIndexed
import top.yogiczy.mytv.ui.screens.leanback.settings.LeanbackSettingsCategories
import top.yogiczy.mytv.ui.theme.LeanbackGlass
import top.yogiczy.mytv.ui.theme.LeanbackGlassSurface
import top.yogiczy.mytv.ui.theme.LeanbackTheme
import top.yogiczy.mytv.ui.utils.handleLeanbackKeyEvents

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LeanbackSettingsCategoryList(
    modifier: Modifier = Modifier,
    focusedCategoryProvider: () -> LeanbackSettingsCategories = { LeanbackSettingsCategories.entries.first() },
    onFocused: (LeanbackSettingsCategories) -> Unit = {},
) {
    var hasFocused = rememberSaveable { false }

    TvLazyColumn(
        contentPadding = PaddingValues(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.focusRestorer()
    ) {
        itemsIndexed(LeanbackSettingsCategories.entries) { index, category ->
            val isSelected by remember { derivedStateOf { focusedCategoryProvider() == category } }
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(Unit) {
                if (index == 0 && !hasFocused) {
                    focusRequester.requestFocus()
                    hasFocused = true
                }
            }

            LeanbackSettingsCategoryItem(
                modifier = Modifier.focusRequester(focusRequester),
                icon = category.icon,
                title = category.title,
                isSelectedProvider = { isSelected },
                onFocused = { onFocused(category) },
            )
        }
    }
}

@Composable
private fun LeanbackSettingsCategoryItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    isSelectedProvider: () -> Boolean = { false },
    onFocused: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val isSelected = isSelectedProvider()
    val contentColor = MaterialTheme.colorScheme.onSurface

    LeanbackGlassSurface(
        focused = isFocused,
        selected = isSelected,
        containerColor = when {
            isFocused -> LeanbackGlass.FocusContainer
            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            else -> LeanbackGlass.OverlaySoft
        },
        borderColor = when {
            isFocused -> LeanbackGlass.Focus
            isSelected -> LeanbackGlass.Focus.copy(alpha = 0.56f)
            else -> LeanbackGlass.StrokeSoft
        },
        contentColor = contentColor,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged {
                isFocused = it.isFocused || it.hasFocus
                if (isFocused) {
                    onFocused()
                }
            }
            .handleLeanbackKeyEvents(
                onSelect = {
                    if (isFocused) focusManager.moveFocus(FocusDirection.Right)
                    else focusRequester.requestFocus()
                }
            )
            .focusable(),
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                )
            }
        }
    }
}

@Preview
@Composable
private fun LeanbackSettingsCategoryItemPreview() {
    LeanbackTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LeanbackSettingsCategoryItem(
                icon = LeanbackSettingsCategories.ABOUT.icon,
                title = LeanbackSettingsCategories.ABOUT.title,
            )

            LeanbackSettingsCategoryItem(
                icon = LeanbackSettingsCategories.ABOUT.icon,
                title = LeanbackSettingsCategories.ABOUT.title,
                isSelectedProvider = { true },
            )
        }
    }
}

@Preview
@Composable
private fun LeanbackSettingsCategoryListPreview() {
    LeanbackTheme {
        LeanbackSettingsCategoryList()
    }
}
