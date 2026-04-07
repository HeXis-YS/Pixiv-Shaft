package ceui.lisa.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchModel : ViewModel() {
    val keyword = MutableLiveData<String>()
    val starSize = MutableLiveData<String>()
    val searchType = MutableLiveData<String>()
    val sortType = MutableLiveData<String>()
    val lastSortType = MutableLiveData<String>()
    val startDate = MutableLiveData<String>()
    val endDate = MutableLiveData<String>()
    val nowGo = MutableLiveData<String>()
    @get:JvmName("getIsNovel")
    val isNovel = MutableLiveData<Boolean>()
    @get:JvmName("getIsPremium")
    val isPremium = MutableLiveData<Boolean>()
    val r18Restriction = MutableLiveData<Int>()
}
