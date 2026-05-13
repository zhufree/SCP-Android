package info.free.scp.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.free.scp.data.user.UserDashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserDashboardViewModel(
    private val repository: UserDashboardRepository = UserDashboardRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserDashboardUiState())
    val uiState: StateFlow<UserDashboardUiState> = _uiState.asStateFlow()

    fun refreshUserDashboard() {
        viewModelScope.launch {
            val stats = repository.loadUserStats()
            val history = repository.loadRecentHistory()
            _uiState.update {
                it.copy(
                    readCount = stats.readCount,
                    likeCount = stats.likeCount,
                    recentHistory = history
                )
            }
        }
    }
}
