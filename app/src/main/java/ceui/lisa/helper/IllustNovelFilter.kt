package ceui.lisa.helper

import android.text.TextUtils
import ceui.lisa.activities.Shaft
import ceui.lisa.database.AppDatabase
import ceui.lisa.database.MuteEntity
import ceui.lisa.models.IllustsBean
import ceui.lisa.models.NovelBean
import ceui.lisa.models.TagsBean
import ceui.lisa.utils.Common
import ceui.lisa.utils.Params
import java.util.ArrayList
import java.util.HashSet
import java.util.regex.Pattern

object IllustNovelFilter {
    private var mutedWorksCache: List<MuteEntity>? = null
    private var mutedTagsCache: MutableList<TagsBean>? = null
    private var mutedUserIdsCache: MutableSet<Int>? = null

    @JvmStatic
    fun judge(illust: IllustsBean): Boolean {
        return judgeID(illust) || judgeTag(illust) || judgeUserID(illust)
    }

    @JvmStatic
    fun judge(illust: NovelBean): Boolean {
        return judgeID(illust) || judgeTag(illust) || judgeUserID(illust)
    }

    @JvmStatic
    fun judgeID(illust: IllustsBean): Boolean {
        val temp = getMutedWorksInternal()
        var isBanned = false
        if (!Common.isEmpty(temp)) {
            for (muteEntity in temp) {
                if (muteEntity.id == illust.id) {
                    isBanned = true
                    break
                }
            }
        }
        return isBanned
    }

    @JvmStatic
    fun judgeID(illust: NovelBean): Boolean {
        val temp = getMutedWorksInternal()
        var isBanned = false
        if (!Common.isEmpty(temp)) {
            for (muteEntity in temp) {
                if (muteEntity.id == illust.id) {
                    isBanned = true
                    break
                }
            }
        }
        return isBanned
    }

    @JvmStatic
    fun judgeUserID(illust: IllustsBean): Boolean {
        return getMutedUserIdsInternal().contains(illust.user.userId)
    }

    @JvmStatic
    fun judgeUserID(illust: NovelBean): Boolean {
        return getMutedUserIdsInternal().contains(illust.user.userId)
    }

    @JvmStatic
    fun judgeTag(illustsBean: IllustsBean): Boolean {
        val tagString = illustsBean.tagString
        if (TextUtils.isEmpty(tagString)) {
            return false
        }

        val temp = getMutedTags()
        for (bean in temp) {
            if (bean.isEffective) {
                val name = "*#" + bean.name + ","
                if (bean.filter_mode == 0 && tagString.contains(name)) {
                    illustsBean.setShield(true)
                    return true
                } else if (bean.filter_mode == 1 && Pattern.compile(bean.name).matcher(tagString).find()) {
                    illustsBean.setShield(true)
                    return true
                }
            }
        }
        return false
    }

    @JvmStatic
    fun judgeTag(illustsBean: NovelBean): Boolean {
        val tagString = illustsBean.tagString
        if (TextUtils.isEmpty(tagString)) {
            return false
        }

        val temp = getMutedTags()
        for (bean in temp) {
            if (bean.isEffective) {
                val name = "*#" + bean.name + ","
                if (bean.filter_mode == 0 && tagString.contains(name)) {
                    return true
                } else if (bean.filter_mode == 1 && Pattern.compile(bean.name).matcher(tagString).find()) {
                    return true
                }
            }
        }
        return false
    }

    @JvmStatic
    fun judgeR18Filter(illustsBean: IllustsBean): Boolean {
        if (!Shaft.sSettings.isR18FilterTempEnable()) {
            return false
        }
        val tagString = illustsBean.tagString
        val isHit = tagString.contains("*#R-18,") || tagString.contains("*#R-18G,")
        illustsBean.setShield(isHit)
        return isHit
    }

    @JvmStatic
    fun judgeR18Filter(illustsBean: NovelBean): Boolean {
        if (!Shaft.sSettings.isR18FilterTempEnable()) {
            return false
        }
        val tagString = illustsBean.tagString
        return tagString.contains("*#R-18,") || tagString.contains("*#R-18G,")
    }

    @JvmStatic
    fun getMutedTags(): List<TagsBean> {
        return ArrayList(getMutedTagsInternal())
    }

    @JvmStatic
    fun getMutedWorks(): List<MuteEntity> {
        return ArrayList(getMutedWorksInternal())
    }

    @JvmStatic
    @Synchronized
    fun invalidateMutedWorks() {
        mutedWorksCache = null
    }

    @JvmStatic
    @Synchronized
    fun invalidateMutedTags() {
        mutedTagsCache = null
    }

    @JvmStatic
    @Synchronized
    fun invalidateMutedUsers() {
        mutedUserIdsCache = null
    }

    @JvmStatic
    @Synchronized
    fun invalidateAll() {
        invalidateMutedWorks()
        invalidateMutedTags()
        invalidateMutedUsers()
    }

    @Synchronized
    private fun getMutedWorksInternal(): List<MuteEntity> {
        if (mutedWorksCache == null) {
            mutedWorksCache = AppDatabase.searchDao(Shaft.getContext()).mutedWorks
        }
        return mutedWorksCache!!
    }

    @Synchronized
    private fun getMutedTagsInternal(): List<TagsBean> {
        if (mutedTagsCache == null) {
            mutedTagsCache = ArrayList()
            val muteEntities = AppDatabase.searchDao(Shaft.getContext()).allMutedTags
            if (!Common.isEmpty(muteEntities)) {
                for (muteEntity in muteEntities) {
                    val bean = Shaft.sGson.fromJson(muteEntity.tagJson, TagsBean::class.java)
                    mutedTagsCache!!.add(bean)
                }
            }
        }
        return mutedTagsCache!!
    }

    @Synchronized
    private fun getMutedUserIdsInternal(): Set<Int> {
        if (mutedUserIdsCache == null) {
            mutedUserIdsCache = HashSet()
            val muteEntities = AppDatabase.searchDao(Shaft.getContext()).allMuteEntities
            if (!Common.isEmpty(muteEntities)) {
                for (muteEntity in muteEntities) {
                    if (muteEntity.type == Params.MUTE_USER) {
                        mutedUserIdsCache!!.add(muteEntity.id)
                    }
                }
            }
        }
        return mutedUserIdsCache!!
    }
}
