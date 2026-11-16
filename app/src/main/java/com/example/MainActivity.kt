package com.example

import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ContactPhone
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.ContactPhone
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.NovenixCatalog
import com.example.ui.AppTab
import com.example.ui.NovenixViewModel
import com.example.ui.screens.ContactScreen
import com.example.ui.screens.SolutionsScreen
import com.example.ui.screens.WebPortalScreen
import com.example.ui.screens.rememberNovenixWebView
import com.example.ui.theme.NovenixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NovenixTheme(darkTheme = true) {
                NovenixApp()
            }
        }
    }
}

@Composable
fun NovenixApp(
    viewModel: NovenixViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingFileCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        pendingFileCallback?.onReceiveValue(uris.toTypedArray())
        pendingFileCallback = null
    }

    DisposableEffect(Unit) {
        onDispose {
            pendingFileCallback?.onReceiveValue(null)
            pendingFileCallback = null
        }
    }

    val webView = rememberNovenixWebView(
        desktopMode = uiState.desktopMode,
        viewModel = viewModel,
        onLaunchFileChooser = { mime, callback ->
            pendingFileCallback?.onReceiveValue(null)
            pendingFileCallback = callback
            fileChooserLauncher.launch(mime)
        }
    )

    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    BackHandler(enabled = uiState.selectedTab == AppTab.WEB && uiState.canGoBack) {
        viewModel.goBackInWeb()
    }
    BackHandler(enabled = uiState.selectedTab != AppTab.WEB) {
        viewModel.selectTab(AppTab.WEB)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useRail) {
                    NovenixBottomBar(
                        selectedTab = uiState.selectedTab,
                        onSelectTab = viewModel::selectTab
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NovenixNavigationRail(
                        selectedTab = uiState.selectedTab,
                        onSelectTab = viewModel::selectTab
                    )
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (uiState.selectedTab) {
                        AppTab.WEB -> {
                            WebPortalScreen(
                                webView = webView,
                                uiState = uiState,
                                viewModel = viewModel,
                                onOpenContactTab = { viewModel.selectTab(AppTab.CONTACT) }
                            )
                        }
                        AppTab.SOLUTIONS -> {
                            SolutionsScreen(
                                uiState = uiState,
                                onSelectCategory = viewModel::selectCategory,
                                onOpenUrl = viewModel::openUrlInWebTab,
                                onOpenContact = { viewModel.selectTab(AppTab.CONTACT) },
                                onRemoveBookmark = viewModel::removeBookmark
                            )
                        }
                        AppTab.CONTACT -> {
                            ContactScreen(
                                onOpenWebsite = { viewModel.openUrlInWebTab(NovenixCatalog.HOME_URL) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NovenixBottomBar(
    selectedTab: AppTab,
    onSelectTab: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == AppTab.WEB,
            onClick = { onSelectTab(AppTab.WEB) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == AppTab.WEB) Icons.Rounded.Language else Icons.Outlined.Language,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.tab_web)) },
            modifier = Modifier.testTag("tab_web")
        )
        NavigationBarItem(
            selected = selectedTab == AppTab.SOLUTIONS,
            onClick = { onSelectTab(AppTab.SOLUTIONS) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == AppTab.SOLUTIONS) Icons.Rounded.Category else Icons.Outlined.Category,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.tab_solutions)) },
            modifier = Modifier.testTag("tab_solutions")
        )
        NavigationBarItem(
            selected = selectedTab == AppTab.CONTACT,
            onClick = { onSelectTab(AppTab.CONTACT) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == AppTab.CONTACT) Icons.Rounded.ContactPhone else Icons.Outlined.ContactPhone,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.tab_contact)) },
            modifier = Modifier.testTag("tab_contact")
        )
    }
}

@Composable
private fun NovenixNavigationRail(
    selectedTab: AppTab,
    onSelectTab: (AppTab) -> Unit
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationRailItem(
            selected = selectedTab == AppTab.WEB,
            onClick = { onSelectTab(AppTab.WEB) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == AppTab.WEB) Icons.Rounded.Language else Icons.Outlined.Language,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.tab_web)) },
            modifier = Modifier.testTag("tab_web")
        )
        NavigationRailItem(
            selected = selectedTab == AppTab.SOLUTIONS,
            onClick = { onSelectTab(AppTab.SOLUTIONS) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == AppTab.SOLUTIONS) Icons.Rounded.Category else Icons.Outlined.Category,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.tab_solutions)) },
            modifier = Modifier.testTag("tab_solutions")
        )
        NavigationRailItem(
            selected = selectedTab == AppTab.CONTACT,
            onClick = { onSelectTab(AppTab.CONTACT) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == AppTab.CONTACT) Icons.Rounded.ContactPhone else Icons.Outlined.ContactPhone,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.tab_contact)) },
            modifier = Modifier.testTag("tab_contact")
        )
    }
}
