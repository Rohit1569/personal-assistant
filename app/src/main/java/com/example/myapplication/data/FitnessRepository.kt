package com.example.myapplication.data

import android.util.Log
import com.example.myapplication.api.FitnessApi
import com.example.myapplication.models.FitnessProfile
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FitnessRepository @Inject constructor(
    private val api: FitnessApi
) {
    private val _refreshSignal = MutableSharedFlow<Unit>(replay = 1)
    val refreshSignal = _refreshSignal.asSharedFlow()

    init {
        _refreshSignal.tryEmit(Unit)
    }

    fun triggerRefresh() {
        _refreshSignal.tryEmit(Unit)
    }

    fun getProfile(): Flow<FitnessProfile?> = refreshSignal
        .onStart { emit(Unit) }
        .flatMapLatest {
            flow {
                try {
                    val response = api.getProfile()
                    if (response.isSuccessful) {
                        emit(response.body())
                    } else if (response.code() == 404) {
                        emit(null)
                    }
                } catch (e: Exception) {
                    Log.e("FitnessRepo", "Error fetching profile: ${e.message}")
                    emit(null)
                }
            }
        }

    suspend fun saveProfile(profile: FitnessProfile): Boolean {
        return try {
            val response = api.createOrUpdateProfile(profile)
            if (response.isSuccessful) {
                triggerRefresh()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("FitnessRepo", "Error saving profile: ${e.message}")
            false
        }
    }

    suspend fun deleteProfile(): Boolean {
        return try {
            val response = api.deleteProfile()
            if (response.isSuccessful) {
                triggerRefresh()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("FitnessRepo", "Error deleting profile: ${e.message}")
            false
        }
    }
}
