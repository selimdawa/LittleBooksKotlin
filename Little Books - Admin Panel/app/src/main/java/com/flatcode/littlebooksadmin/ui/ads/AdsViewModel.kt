package com.flatcode.littlebooksadmin.ui.ads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.model.ADs
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.repository.AdsRepository
import com.flatcode.littlebooksadmin.repository.UserRepository
import com.flatcode.littlebooksadmin.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdsViewModel @Inject constructor(
    private val adsRepository: AdsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _adsUsers = MutableStateFlow<Resource<List<User>>>(Resource.Loading())
    val adsUsers: StateFlow<Resource<List<User>>> = _adsUsers

    private val _userAds = MutableStateFlow<Resource<List<ADs>>>(Resource.Loading())
    val userAds: StateFlow<Resource<List<ADs>>> = _userAds

    private val _userInfo = MutableStateFlow<Resource<User>>(Resource.Loading())
    val userInfo: StateFlow<Resource<User>> = _userInfo

    fun loadAdsUsers(orderBy: String = DATA.AD_LOAD) {
        viewModelScope.launch {
            adsRepository.getAdsUsers(orderBy).collect {
                _adsUsers.value = it
            }
        }
    }

    fun loadUserAds(userId: String, orderBy: String = DATA.NAME) {
        viewModelScope.launch {
            _userInfo.value = userRepository.getUserById(userId)
            
            adsRepository.getUserAds(userId, orderBy).collect {
                _userAds.value = it
            }
        }
    }
}


