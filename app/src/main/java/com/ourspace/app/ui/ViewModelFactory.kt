package com.ourspace.app.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ourspace.app.data.repository.AuthRepository
import com.ourspace.app.data.repository.FeaturesRepository
import com.ourspace.app.data.repository.UserRepository
import com.ourspace.app.ui.auth.AuthViewModel
import com.ourspace.app.ui.features.FeaturesViewModel
import com.ourspace.app.ui.user.UserViewModel

class ViewModelFactory(
    private val application: Application,
    private val userRepository: UserRepository,
    private val featuresRepository: FeaturesRepository,
    private val authRepository: AuthRepository,
    private val musicRepository: com.ourspace.app.data.repository.MusicRepository = com.ourspace.app.data.repository.MusicRepository()
) : ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(UserViewModel::class.java) -> {
                UserViewModel(application, userRepository) as T
            }
            modelClass.isAssignableFrom(FeaturesViewModel::class.java) -> {
                FeaturesViewModel(featuresRepository, musicRepository) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(authRepository) as T
            }
            else -> super.create(modelClass)
        }
    }
}
