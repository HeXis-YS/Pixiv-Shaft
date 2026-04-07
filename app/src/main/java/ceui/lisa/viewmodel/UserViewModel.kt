package ceui.lisa.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ceui.lisa.models.UserDetailResponse
import ceui.loxia.Event

class UserViewModel : ViewModel() {

    private var userLiveData: MutableLiveData<UserDetailResponse>? = null

    fun getUser(): MutableLiveData<UserDetailResponse> {
        if (userLiveData == null) {
            userLiveData = MutableLiveData()
        }
        return userLiveData!!
    }

    @get:JvmName("getUserLiveData")
    val user: MutableLiveData<UserDetailResponse>
        get() = getUser()

    @JvmField
    val isUserMuted = MutableLiveData<Boolean>()

    @JvmField
    val isUserBlocked = MutableLiveData<Boolean>()

    @JvmField
    val refreshEvent = MutableLiveData<Event<Int>>()
}
