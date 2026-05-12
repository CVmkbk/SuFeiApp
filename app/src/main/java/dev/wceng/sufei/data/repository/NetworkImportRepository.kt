package dev.wceng.sufei.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkImportRepository @Inject constructor() : ImportRepository {

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    override val importState: StateFlow<ImportState> = _importState.asStateFlow()

    override suspend fun startImportIfNeeded() {
        _importState.value = ImportState.Importing(0.5f)
        kotlinx.coroutines.delay(800)
        _importState.value = ImportState.Success
    }
}