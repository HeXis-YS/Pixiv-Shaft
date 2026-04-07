package ceui.lisa.helper

import ceui.lisa.activities.Shaft
import ceui.lisa.database.IllustHistoryEntity
import ceui.lisa.models.IllustsBean
import ceui.lisa.models.UserBean
import ceui.lisa.models.UserPreviewsBean
import ceui.lisa.viewmodel.AppLevelViewModel

object AppLevelViewModelHelper {
    @JvmStatic
    fun <T> fill(list: List<T>) {
        if (list.isEmpty()) {
            return
        }
        val firstItemClass = (list[0] as Any)::class.java
        when (firstItemClass) {
            IllustsBean::class.java -> {
                for (illustsBean in list as List<IllustsBean>) {
                    val userId = illustsBean.user.id
                    val followUserStatus = getFollowUserStatus(illustsBean.user)
                    Shaft.appViewModel.updateFollowUserStatus(userId, followUserStatus)
                }
            }

            UserPreviewsBean::class.java -> {
                for (userPreviewsBean in list as List<UserPreviewsBean>) {
                    val userId = userPreviewsBean.user.id
                    val followUserStatus = getFollowUserStatus(userPreviewsBean.user)
                    Shaft.appViewModel.updateFollowUserStatus(userId, followUserStatus)
                }
            }

            UserBean::class.java -> {
                for (userBean in list as List<UserBean>) {
                    val userId = userBean.id
                    val followUserStatus = getFollowUserStatus(userBean)
                    Shaft.appViewModel.updateFollowUserStatus(userId, followUserStatus)
                }
            }

            IllustHistoryEntity::class.java -> {
                for (entity in list as List<IllustHistoryEntity>) {
                    val illustsBean = Shaft.sGson.fromJson(entity.illustJson, IllustsBean::class.java)
                    val userBean = illustsBean.user
                    val userId = userBean.id
                    val followUserStatus = getFollowUserStatus(userBean)
                    Shaft.appViewModel.updateFollowUserStatus(
                        userId,
                        followUserStatus,
                        AppLevelViewModel.UpdateMethod.IF_ABSENT,
                    )
                }
            }
        }
    }

    private fun getFollowUserStatus(user: UserBean): Int {
        return if (user.isIs_followed) {
            AppLevelViewModel.FollowUserStatus.FOLLOWED
        } else {
            AppLevelViewModel.FollowUserStatus.NOT_FOLLOW
        }
    }

    @JvmStatic
    fun updateFollowUserStatus(user: UserBean, method: Int) {
        Shaft.appViewModel.updateFollowUserStatus(user.id, getFollowUserStatus(user), method)
    }
}
