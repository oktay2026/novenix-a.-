package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.DesktopWindows
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.data.NovenixCatalog
import com.example.ui.IntentHelpers
import com.example.ui.NovenixUiState
import com.example.ui.NovenixViewModel
import com.example.ui.WebCommand

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun rememberNovenixWebView(
    desktopMode: Boolean,
    viewModel: NovenixViewModel,
    onLaunchFileChooser: (mimeType: String, callback: ValueCallback<Array<Uri>>?) -> Unit
): WebView {
    val context = LocalContext.current
    val defaultUserAgent = remember { WebSettings.getDefaultUserAgent(context) }

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = true
                displayZoomControls = false
                setSupportZoom(true)
                allowFileAccess = false
                allowContentAccess = true
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                userAgentString = if (desktopMode) DESKTOP_USER_AGENT else defaultUserAgent
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    url?.let { viewModel.onPageStarted(it) }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    viewModel.onPageFinished(
                        url = url ?: NovenixCatalog.HOME_URL,
                        title = view?.title,
                        canGoBack = view?.canGoBack() == true,
                        canGoForward = view?.canGoForward() == true
                    )
                }

                override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                    viewModel.onNavigationHistoryUpdated(
                        canGoBack = view?.canGoBack() == true,
                        canGoForward = view?.canGoForward() == true
                    )
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    if (request?.isForMainFrame != true) return false
                    val uri = request.url ?: return false
                    val scheme = uri.scheme?.lowercase().orEmpty()
                    val host = uri.host?.lowercase().orEmpty()

                    if (scheme == "http" || scheme == "https") {
                        return if (host == "novenix.tech" || host.endsWith(".novenix.tech")) {
                            false
                        } else {
                            IntentHelpers.openExternalUrl(context, uri.toString())
                            true
                        }
                    }

                    when (scheme) {
                        "tel" -> IntentHelpers.dialPhone(context, uri.toString())
                        "mailto" -> {
                            try {
                                context.startActivity(Intent(Intent.ACTION_SENDTO, uri))
                            } catch (_: Exception) {
                                IntentHelpers.sendEmail(context)
                            }
                        }
                        "intent" -> {
                            try {
                                val parsedIntent = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
                                context.startActivity(parsedIntent)
                            } catch (_: Exception) {
                                IntentHelpers.openExternalUrl(context, NovenixCatalog.HOME_URL)
                            }
                        }
                        else -> IntentHelpers.openExternalUrl(context, uri.toString())
                    }
                    return true
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    if (request?.isForMainFrame == true) {
                        viewModel.onWebError(error?.description?.toString())
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    viewModel.onProgressChanged(newProgress)
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    title?.let { viewModel.onTitleReceived(it) }
                }

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    val mime = fileChooserParams?.acceptTypes?.firstOrNull()?.takeIf { it.isNotBlank() } ?: "*/*"
                    onLaunchFileChooser(mime, filePathCallback)
                    return true
                }
            }

            loadUrl(NovenixCatalog.HOME_URL)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.webCommands.collect { command ->
            when (command) {
                is WebCommand.LoadUrl -> webView.loadUrl(command.url)
                WebCommand.Reload -> webView.reload()
                WebCommand.GoBack -> if (webView.canGoBack()) webView.goBack()
                WebCommand.GoForward -> if (webView.canGoForward()) webView.goForward()
                is WebCommand.SetDesktopMode -> {
                    webView.settings.userAgentString =
                        if (command.enabled) DESKTOP_USER_AGENT else defaultUserAgent
                    webView.settings.useWideViewPort = true
                    webView.settings.loadWithOverviewMode = true
                    webView.reload()
                }
            }
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            webView.destroy()
        }
    }

    return webView
}

@Composable
fun WebPortalScreen(
    webView: WebView,
    uiState: NovenixUiState,
    viewModel: NovenixViewModel,
    onOpenContactTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        WebPortalTopBar(
            uiState = uiState,
            menuExpanded = menuExpanded,
            onMenuExpandedChange = { menuExpanded = it },
            onBack = { viewModel.goBackInWeb() },
            onForward = { viewModel.goForwardInWeb() },
            onRefresh = { viewModel.reloadWeb() },
            onToggleBookmark = { viewModel.toggleCurrentBookmark() },
            onToggleDesktopMode = { viewModel.toggleDesktopMode() },
            onGoHome = { viewModel.goHome() },
            onShare = { IntentHelpers.shareLink(context, uiState.currentUrl, uiState.pageTitle) },
            onOpenExternal = { IntentHelpers.openExternalUrl(context, uiState.currentUrl) }
        )

        AnimatedVisibility(visible = uiState.isLoading && uiState.progress in 1..99) {
            LinearProgressIndicator(
                progress = { uiState.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(
                factory = {
                    (webView.parent as? ViewGroup)?.removeView(webView)
                    webView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Initial branded launch overlay while first frame loads
            androidx.compose.animation.AnimatedVisibility(
                visible = !uiState.hasInitialLoadCompleted && uiState.isLoading && uiState.errorMessage == null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                InitialLoadingOverlay(progress = uiState.progress)
            }

            // Clean offline / error recovery view
            androidx.compose.animation.AnimatedVisibility(
                visible = uiState.errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                WebErrorOverlay(
                    onRetry = { viewModel.reloadWeb() },
                    onOpenContact = onOpenContactTab
                )
            }
        }
    }
}

@Composable
private fun WebPortalTopBar(
    uiState: NovenixUiState,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onRefresh: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onGoHome: () -> Unit,
    onShare: () -> Unit,
    onOpenExternal: () -> Unit
) {
    val parsedHost = remember(uiState.currentUrl) {
        Uri.parse(uiState.currentUrl).host ?: "novenix.tech"
    }
    val isHttps = uiState.currentUrl.startsWith("https://", ignoreCase = true)

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    enabled = uiState.canGoBack,
                    modifier = Modifier.testTag("web_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.nav_back)
                    )
                }

                IconButton(
                    onClick = onForward,
                    enabled = uiState.canGoForward,
                    modifier = Modifier.testTag("web_forward_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = stringResource(R.string.nav_forward)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isHttps) Icons.Rounded.Lock else Icons.Rounded.Language,
                            contentDescription = null,
                            tint = if (isHttps) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.pageTitle,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = parsedHost,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.testTag("web_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = stringResource(R.string.nav_refresh)
                    )
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.testTag("web_bookmark_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isCurrentUrlBookmarked) {
                            Icons.Rounded.Bookmark
                        } else {
                            Icons.Outlined.BookmarkBorder
                        },
                        contentDescription = stringResource(
                            if (uiState.isCurrentUrlBookmarked) R.string.nav_remove_bookmark
                            else R.string.nav_bookmark
                        ),
                        tint = if (uiState.isCurrentUrlBookmarked) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Box {
                    IconButton(
                        onClick = { onMenuExpandedChange(true) },
                        modifier = Modifier.testTag("web_more_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = stringResource(R.string.nav_more)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { onMenuExpandedChange(false) }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_home)) },
                            leadingIcon = { Icon(Icons.Rounded.Home, contentDescription = null) },
                            onClick = {
                                onMenuExpandedChange(false)
                                onGoHome()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (uiState.desktopMode) "Mobil Görünüme Geç"
                                    else stringResource(R.string.menu_desktop_mode)
                                )
                            },
                            leadingIcon = { Icon(Icons.Rounded.DesktopWindows, contentDescription = null) },
                            onClick = {
                                onMenuExpandedChange(false)
                                onToggleDesktopMode()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.nav_share)) },
                            leadingIcon = { Icon(Icons.Rounded.Share, contentDescription = null) },
                            onClick = {
                                onMenuExpandedChange(false)
                                onShare()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_open_browser)) },
                            leadingIcon = { Icon(Icons.Rounded.OpenInBrowser, contentDescription = null) },
                            onClick = {
                                onMenuExpandedChange(false)
                                onOpenExternal()
                            }
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun InitialLoadingOverlay(progress: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_novenix_icon),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Novenix Teknoloji Grubu",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "novenix.tech bağlanıyor...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
            if (progress > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "%$progress",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun WebErrorOverlay(
    onRetry: () -> Unit,
    onOpenContact: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.widthIn(max = 460.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_novenix_hero),
                        contentDescription = stringResource(R.string.cd_hero_banner),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.25f),
                                        MaterialTheme.colorScheme.surfaceContainer
                                    )
                                )
                            )
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.18f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                            .size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.WifiOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.error_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.error_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onRetry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("retry_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.retry))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onOpenContact,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("offline_contact_button")
                    ) {
                        Text(stringResource(R.string.view_contact_info))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
