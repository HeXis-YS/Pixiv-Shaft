package ceui.lisa.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class AppLevelViewModel(application: Application) : AndroidViewModel(application) {

    private val followUserStatus: ConcurrentMap<Int, MutableLiveData<Int>> = ConcurrentHashMap()
    private val starIllustStatus: ConcurrentMap<Int, MutableLiveData<Int>> = ConcurrentHashMap()
    private val starNovelStatus: ConcurrentMap<Int, MutableLiveData<Int>> = ConcurrentHashMap()

    fun getFollowUserLiveData(userId: Int): MutableLiveData<Int> {
        var data = followUserStatus[userId]
        if (data == null) {
            data = MutableLiveData(FollowUserStatus.UNKNOWN)
            followUserStatus[userId] = data
        }
        return data
    }

    fun updateFollowUserStatus(userId: Int, status: Int) {
        updateFollowUserStatus(userId, status, UpdateMethod.NORMAL)
    }

    fun updateFollowUserStatus(userId: Int, status: Int, method: Int) {
        val data = followUserStatus[userId]
        when (method) {
            UpdateMethod.IF_ABSENT -> {
                if (data != null) {
                    val currentValue = data.value
                    if (currentValue != null && currentValue == FollowUserStatus.UNKNOWN) {
                        data.value = status
                    }
                } else {
                    followUserStatus[userId] = MutableLiveData(status)
                }
            }

            UpdateMethod.FORCE_REPLACE -> {
                if (data != null) {
                    data.value = status
                } else {
                    followUserStatus[userId] = MutableLiveData(status)
                }
            }

            else -> {
                if (data != null) {
                    val currentValue = data.value
                    if (currentValue != null
                        && FollowUserStatus.isPreciseFollow(currentValue)
                        && status == FollowUserStatus.FOLLOWED
                    ) {
                        return
                    }
                    data.value = status
                } else {
                    followUserStatus[userId] = MutableLiveData(status)
                }
            }
        }
    }

    fun getStarIllustLiveData(illustId: Int): MutableLiveData<Int> {
        var data = starIllustStatus[illustId]
        if (data == null) {
            data = MutableLiveData(StarIllustStatus.UNKNOWN)
            starIllustStatus[illustId] = data
        }
        return data
    }

    fun updateStarIllustStatus(illustId: Int, status: Int) {
        val data = starIllustStatus[illustId]
        if (data != null) {
            data.value = status
        } else {
            starIllustStatus[illustId] = MutableLiveData(status)
        }
    }

    class FollowUserStatus private constructor() {
        companion object {
            @JvmField
            val UNKNOWN = 0

            @JvmField
            val NOT_FOLLOW = 1

            @JvmField
            val FOLLOWED = 2

            @JvmField
            val FOLLOWED_PUBLIC = 3

            @JvmField
            val FOLLOWED_PRIVATE = 4

            @JvmStatic
            fun isFollowed(status: Int): Boolean {
                return status == FOLLOWED || status == FOLLOWED_PUBLIC || status == FOLLOWED_PRIVATE
            }

            @JvmStatic
            fun isPreciseFollow(status: Int): Boolean {
                return status == FOLLOWED_PUBLIC || status == FOLLOWED_PRIVATE
            }

            @JvmStatic
            fun isPublicFollowed(status: Int): Boolean {
                return status == FOLLOWED_PUBLIC
            }

            @JvmStatic
            fun isPrivateFollowed(status: Int): Boolean {
                return status == FOLLOWED_PRIVATE
            }
        }
    }

    class StarIllustStatus private constructor() {
        companion object {
            @JvmField
            val UNKNOWN = 0

            @JvmField
            val NOT_STAR = 1

            @JvmField
            val STARRED = 2

            @JvmField
            val STARRED_PUBLIC = 3

            @JvmField
            val STARRED_PRIVATE = 4

            @JvmStatic
            fun isStarred(status: Int): Boolean {
                return status == STARRED || status == STARRED_PUBLIC || status == STARRED_PRIVATE
            }

            @JvmStatic
            fun isPublicStarred(status: Int): Boolean {
                return status == STARRED_PUBLIC
            }

            @JvmStatic
            fun isPrivateStarred(status: Int): Boolean {
                return status == STARRED_PRIVATE
            }
        }
    }

    class UpdateMethod private constructor() {
        companion object {
            @JvmField
            val NORMAL = 0

            @JvmField
            val IF_ABSENT = 1

            @JvmField
            val FORCE_REPLACE = 2
        }
    }
}
