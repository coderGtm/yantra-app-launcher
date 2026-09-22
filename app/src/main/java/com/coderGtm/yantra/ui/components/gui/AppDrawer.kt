package com.coderGtm.yantra.ui.components.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.coderGtm.yantra.gui.drawer.repository.AppRepository
import com.coderGtm.yantra.gui.web.YantraAppResolver
import com.coderGtm.yantra.ui.screens.gui.NativeGuiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    state: NativeGuiState,
    onClose: () -> Unit,
    onDrop: (String, Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appRepository = remember { AppRepository(context) }
    val appResolver = remember { YantraAppResolver(context) }
    val apps = remember { appRepository.getInstalledApps() }
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    
    var componentOffset by remember { mutableStateOf(Offset.Zero) }

    val filteredApps = remember(apps, state.searchQuery) {
        if (state.searchQuery.isBlank()) {
            apps
        } else {
            apps.filter {
                it.label.contains(state.searchQuery, ignoreCase = true) ||
                it.packageName.contains(state.searchQuery, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .onGloballyPositioned { componentOffset = it.positionInWindow() }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Search Bar
            TextField(
                value = state.searchQuery,
                onValueChange = { state.searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                placeholder = { Text("Search apps...") },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            
            Row(modifier = Modifier.weight(1f)) {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Adaptive(80.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredApps) { app ->
                        var itemPosition by remember { mutableStateOf(Offset.Zero) }
                        
                        HomeItemView(
                            packageName = app.packageName,
                            onClick = {
                                appResolver.launch(app.packageName)
                                onClose()
                            },
                            onLongClick = {
                                state.selectedAppForMenu = app
                            },
                            modifier = Modifier
                                .onGloballyPositioned { itemPosition = it.positionInWindow() }
                                .pointerInput(app.packageName) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { offset ->
                                            state.draggingPackageName = app.packageName
                                            state.dragOffset = itemPosition + offset
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            state.dragOffset += dragAmount
                                        },
                                        onDragEnd = {
                                            onDrop(app.packageName, state.dragOffset)
                                            state.draggingPackageName = null
                                        },
                                        onDragCancel = {
                                            state.draggingPackageName = null
                                        }
                                    )
                                }
                        )
                    }
                }
                
                AlphabetIndex(
                    onLetterClick = { letter ->
                        coroutineScope.launch {
                            val index = filteredApps.indexOfFirst { 
                                val firstChar = it.label.firstOrNull() ?: return@indexOfFirst false
                                if (letter == '#') !firstChar.isLetter()
                                else it.label.startsWith(letter, ignoreCase = true)
                            }
                            if (index != -1) {
                                gridState.animateScrollToItem(index)
                            }
                        }
                    }
                )
            }
        }
    }
}
