package top.yogiczy.mytv.ui.screens.leanback.classicpanel.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.items
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import kotlinx.coroutines.flow.distinctUntilChanged
import top.yogiczy.mytv.data.entities.IptvGroup
import top.yogiczy.mytv.data.entities.IptvGroupList
import top.yogiczy.mytv.ui.theme.LeanbackTheme
import top.yogiczy.mytv.ui.utils.handleLeanbackKeyEvents
import kotlin.math.max

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LeanbackClassicPanelIptvGroupList(
    modifier: Modifier = Modifier,
    iptvGroupListProvider: () -> IptvGroupList = { IptvGroupList() },
    initialIptvGroupProvider: () -> IptvGroup = { IptvGroup() },
    exitFocusRequesterProvider: () -> FocusRequester = { FocusRequester.Default },
    onIptvGroupFocused: (IptvGroup) -> Unit = {},
    onIptvGroupLongSelected: (IptvGroup) -> Unit = {},
    onUserAction: () -> Unit = {},
) {
    val iptvGroupList = iptvGroupListProvider()
    val initialIptvGroup = initialIptvGroupProvider()

    val focusRequester = remember { FocusRequester() }
    var focusedIptvGroup by remember { mutableStateOf(initialIptvGroup) }

    val listState = rememberTvLazyListState(max(0, iptvGroupList.indexOf(initialIptvGroup) - 2))

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { _ -> onUserAction() }
    }

    TvLazyColumn(
        state = listState,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .width(140.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background.copy(0.9f))
            .focusRequester(focusRequester)
            .focusProperties {
                exit = {
                    focusRequester.saveFocusedChild()
                    exitFocusRequesterProvider()
                }
                enter = {
                    if (focusRequester.restoreFocusedChild()) FocusRequester.Cancel
                    else FocusRequester.Default
                }
            },
    ) {
        items(iptvGroupList) { iptvGroup ->
            val isSelected by remember { derivedStateOf { iptvGroup == focusedIptvGroup } }

            LeanbackClassicPanelIptvGroupItem(
                iptvGroupProvider = { iptvGroup },
                isSelectedProvider = { isSelected },
                initialFocusedProvider = { iptvGroup == initialIptvGroup },
                onFocused = {
                    focusedIptvGroup = it
                    onIptvGroupFocused(it)
                },
                onLongSelected = { onIptvGroupLongSelected(iptvGroup) },
            )
        }
    }
}

@Composable
private fun LeanbackClassicPanelIptvGroupItem(
    modifier: Modifier = Modifier,
    iptvGroupProvider: () -> IptvGroup = { IptvGroup() },
    isSelectedProvider: () -> Boolean = { false },
    initialFocusedProvider: () -> Boolean = { false },
    onFocused: (IptvGroup) -> Unit = {},
    onLongSelected: () -> Unit = {},
) {
    val iptvGroup = iptvGroupProvider()
    val isSelected = isSelectedProvider()

    val focusRequester = remember { FocusRequester() }
    var hasFocused by rememberSaveable { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    val localContentColor = LocalContentColor.current
    val containerColor = remember(isFocused, isSelected) {
        if (isFocused) colorScheme.onBackground
        else if (isSelected) colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else Color.Transparent
    }
    val contentColor = remember(isFocused, isSelected) {
        if (isFocused) colorScheme.background
        else if (isSelected) colorScheme.onBackground
        else localContentColor
    }

    LaunchedEffect(Unit) {
        if (!hasFocused && initialFocusedProvider()) {
            focusRequester.requestFocus()
        }
        hasFocused = true
    }

    Box(
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged {
                isFocused = it.isFocused || it.hasFocus

                if (isFocused) {
                    onFocused(iptvGroup)
                }
            }
            .handleLeanbackKeyEvents(
                onSelect = {
                    focusRequester.requestFocus()
                },
                onLongSelect = onLongSelected,
            )
            .focusable()
            .fillMaxWidth()
            .background(containerColor, MaterialTheme.shapes.small)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = iptvGroup.name,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = contentColor,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun LeanbackClassicPanelIptvGroupListPreview() {
    LeanbackTheme {
        LeanbackClassicPanelIptvGroupList(
            modifier = Modifier.padding(20.dp),
            iptvGroupListProvider = { IptvGroupList.EXAMPLE },
        )
    }
}
