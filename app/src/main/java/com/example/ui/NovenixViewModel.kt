package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BookmarkItem
import com.example.data.NovenixCatalog
import com.example.data.SolutionCategory
import com.example.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppTab {
    WEB,
    SOLUTIONS,
    CONTACT
}

sealed interface WebCommand {
    data class LoadUrl(val url: String) : WebCommand
    data object Reload : WebCommand
    data object GoBack : WebCommand
    data object GoForward : WebCommand
    data class SetDesktopMode(val enabled: Boolean) : WebCommand
}

data class NovenixUiState(
    val selectedTab: AppTab = AppTab.WEB,
    val currentUrl: String = NovenixCatalog.HOME_URL,
    val pageTitle: String = "Novenix Teknoloji Grubu",
    val progress: Int = 0,
    val isLoading: Boolean = true,
    val hasInitialLoadCompleted: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val errorMessage: String? = null,
    val desktopMode: Boolean = false,
    val bookmarks: List<BookmarkItem> = emptyList(),
    val selectedCategory: SolutionCategory = SolutionCategory.ALL
) {
    val isCurrentUrlBookmarked: Boolean
        get() = bookmarks.any { it.url == currentUrl }
}

class NovenixViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = UserPreferencesRepository(application.applicationContext)

    private val internalState = MutableStateFlow(NovenixUiState())

    private val _webCommands = MutableSharedFlow<WebCommand>(extraBufferCapacity = 8)
    val webCommands = _webCommands.asSharedFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarEvents = _snackbarEvents.asSharedFlow()

    val uiState: StateFlow<NovenixUiState> = combine(
        internalState,
        prefsRepo.desktopModeFlow,
        prefsRepo.bookmarksFlow
    ) { state, desktopMode, bookmarks ->
        state.copy(
            desktopMode = desktopMode,
            bookmarks = bookmarks
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NovenixUiState()
    )

    fun selectTab(tab: AppTab) {
        internalState.update { it.copy(selectedTab = tab) }
    }

    fun selectCategory(category: SolutionCategory) {
        internalState.update { it.copy(selectedCategory = category) }
    }

    fun openUrlInWebTab(url: String) {
        internalState.update {
            it.copy(
                selectedTab = AppTab.WEB,
                errorMessage = null
            )
        }
        _webCommands.tryEmit(WebCommand.LoadUrl(url))
    }

    fun reloadWeb() {
        internalState.update { it.copy(errorMessage = null, isLoading = true) }
        _webCommands.tryEmit(WebCommand.Reload)
    }

    fun goHome() {
        openUrlInWebTab(NovenixCatalog.HOME_URL)
    }

    fun goBackInWeb() {
        _webCommands.tryEmit(WebCommand.GoBack)
    }

    fun goForwardInWeb() {
        _webCommands.tryEmit(WebCommand.GoForward)
    }

    fun toggleDesktopMode() {
        val next = !uiState.value.desktopMode
        viewModelScope.launch {
            prefsRepo.setDesktopMode(next)
            _webCommands.emit(WebCommand.SetDesktopMode(next))
            _snackbarEvents.emit(
                if (next) "Masaüstü görünümü etkinleştirildi" else "Mobil görünüme dönüldü"
            )
        }
    }

    fun toggleCurrentBookmark() {
        val current = uiState.value
        viewModelScope.launch {
            val added = prefsRepo.toggleBookmark(current.currentUrl, current.pageTitle)
            _snackbarEvents.emit(
                if (added) "Sayfa yer imlerine eklendi" else "Yer iminden kaldırıldı"
            )
        }
    }

    fun removeBookmark(url: String) {
        viewModelScope.launch {
            prefsRepo.removeBookmark(url)
            _snackbarEvents.emit("Yer imi silindi")
        }
    }

    fun onPageStarted(url: String) {
        internalState.update {
            it.copy(
                currentUrl = url,
                isLoading = true,
                errorMessage = null
            )
        }
    }

    fun onPageFinished(url: String, title: String?, canGoBack: Boolean, canGoForward: Boolean) {
        internalState.update {
            it.copy(
                currentUrl = url,
                pageTitle = title?.takeIf { t -> t.isNotBlank() && !t.startsWith("http") } ?: "Novenix",
                isLoading = false,
                hasInitialLoadCompleted = true,
                progress = 100,
                canGoBack = canGoBack,
                canGoForward = canGoForward
            )
        }
    }

    fun onNavigationHistoryUpdated(canGoBack: Boolean, canGoForward: Boolean) {
        internalState.update {
            it.copy(
                canGoBack = canGoBack,
                canGoForward = canGoForward
            )
        }
    }

    fun onProgressChanged(progress: Int) {
        internalState.update {
            it.copy(
                progress = progress,
                isLoading = progress < 100
            )
        }
    }

    fun onTitleReceived(title: String) {
        if (title.isNotBlank() && !title.startsWith("http")) {
            internalState.update { it.copy(pageTitle = title) }
        }
    }

    fun onWebError(description: String?) {
        internalState.update {
            it.copy(
                isLoading = false,
                hasInitialLoadCompleted = true,
                errorMessage = description ?: "Sayfa yüklenirken bir ağ hatası oluştu."
            )
        }
    }
}
