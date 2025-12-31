package com.lanrhyme.shardlauncher.ui.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lanrhyme.shardlauncher.api.ApiClient
import com.lanrhyme.shardlauncher.model.BmclapiManifest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameDownloadViewModel : ViewModel() {

    private val _versions = MutableStateFlow<List<BmclapiManifest.Version>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _selectedVersionTypes = MutableStateFlow(setOf(VersionType.Release))
    private val _isLoading = MutableStateFlow(false)

    // Cache
    private var cachedVersions: List<BmclapiManifest.Version>? = null
    private var lastSourceWasBmclapi: Boolean? = null

    val searchQuery = _searchQuery.asStateFlow()
    val selectedVersionTypes = _selectedVersionTypes.asStateFlow()
    val isLoading = _isLoading.asStateFlow()

    val filteredVersions: StateFlow<List<BmclapiManifest.Version>> = combine(
        _versions,
        _searchQuery,
        _selectedVersionTypes
    ) { versions, query, types ->
        versions.filter { version ->
            val typeMatches = when (version.type) {
                "release" -> VersionType.Release in types
                "snapshot" -> VersionType.Snapshot in types
                "old_alpha", "old_beta" -> VersionType.Ancient in types
                else -> false
            }
            val queryMatches = version.id.contains(query, ignoreCase = true)
            typeMatches && queryMatches
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleVersionType(type: VersionType) {
        val currentTypes = _selectedVersionTypes.value.toMutableSet()
        if (type in currentTypes) {
            currentTypes.remove(type)
        } else {
            currentTypes.add(type)
        }
        _selectedVersionTypes.value = currentTypes
    }

    fun loadVersions(forceRefresh: Boolean = false) {
        val useBmclapi = com.lanrhyme.shardlauncher.settings.AllSettings.fileDownloadSource.state == com.lanrhyme.shardlauncher.settings.enums.MirrorSourceType.MIRROR_FIRST
        if (!forceRefresh && cachedVersions != null && lastSourceWasBmclapi == useBmclapi) {
            _versions.value = cachedVersions!!
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val versionsFromApi = if (useBmclapi) {
                    try {
                        ApiClient.bmclapiService.getGameVersionManifest().versions
                    } catch (e: Exception) {
                        // fallback to default api
                        val latest = ApiClient.versionApiService.getLatestVersions().release
                        listOf(BmclapiManifest.Version(latest.versionId, latest.versionType, "", "", ""))
                    }
                } else {
                    val latest = ApiClient.versionApiService.getLatestVersions().release
                    listOf(BmclapiManifest.Version(latest.versionId, latest.versionType, "", "", ""))
                }
                _versions.value = versionsFromApi
                // Update cache
                cachedVersions = versionsFromApi
                lastSourceWasBmclapi = useBmclapi
            } finally {
                _isLoading.value = false
            }
        }
    }
}
