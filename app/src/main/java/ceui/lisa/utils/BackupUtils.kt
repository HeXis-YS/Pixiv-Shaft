package ceui.lisa.utils

import android.content.Context
import ceui.lisa.activities.Shaft
import ceui.lisa.database.AppDatabase
import ceui.lisa.database.IllustHistoryEntity
import ceui.lisa.database.MuteEntity
import ceui.lisa.database.SearchEntity
import ceui.lisa.database.UserEntity
import ceui.lisa.feature.FeatureEntity
import ceui.lisa.helper.IllustNovelFilter

object BackupUtils {

    class BackupEntity {
        var settings: Settings? = null
        var muteEntityList: List<MuteEntity>? = null
        var featureEntityList: List<FeatureEntity>? = null
        var searchEntityList: List<SearchEntity>? = null
        var userEntityList: List<UserEntity>? = null
        var illustHistoryEntityList: List<IllustHistoryEntity>? = null
    }

    @JvmStatic
    fun getBackupString(context: Context, backupViewHistory: Boolean): String {
        val backupEntity = BackupEntity()
        backupEntity.settings = Shaft.sSettings
        val searchDao = AppDatabase.searchDao(context)
        val downloadDao = AppDatabase.downloadDao(context)
        backupEntity.muteEntityList = searchDao.allMuteEntities
        backupEntity.featureEntityList = downloadDao.getAllFeatureEntities()
        backupEntity.searchEntityList = searchDao.allSearchEntities
        backupEntity.userEntityList = downloadDao.getAllUser()
        if (backupViewHistory) {
            backupEntity.illustHistoryEntityList = downloadDao.getAllViewHistoryEntities()
        }
        return Shaft.sGson.toJson(backupEntity)
    }

    @JvmStatic
    fun restoreBackups(context: Context, backupString: String): Boolean {
        return try {
            val backupEntity = Shaft.sGson.fromJson(backupString, BackupEntity::class.java)
            val settings = backupEntity.settings
            if (settings != null) {
                Local.setSettings(settings)
            }

            val searchDao = AppDatabase.searchDao(context)
            val downloadDao = AppDatabase.downloadDao(context)

            val muteEntityList = backupEntity.muteEntityList
            if (!muteEntityList.isNullOrEmpty()) {
                for (muteEntity in muteEntityList) {
                    searchDao.insertMuteTag(muteEntity)
                }
                IllustNovelFilter.invalidateAll()
            }

            val featureEntityList = backupEntity.featureEntityList
            if (!featureEntityList.isNullOrEmpty()) {
                for (featureEntity in featureEntityList) {
                    downloadDao.insertFeature(featureEntity)
                }
            }

            val searchEntityList = backupEntity.searchEntityList
            if (!searchEntityList.isNullOrEmpty()) {
                for (searchEntity in searchEntityList) {
                    searchDao.insert(searchEntity)
                }
            }

            val userEntityList = backupEntity.userEntityList
            if (!userEntityList.isNullOrEmpty()) {
                for (userEntity in userEntityList) {
                    downloadDao.insertUser(userEntity)
                }
            }

            val illustHistoryEntityList = backupEntity.illustHistoryEntityList
            if (!illustHistoryEntityList.isNullOrEmpty()) {
                for (illustHistoryEntity in illustHistoryEntityList) {
                    downloadDao.insert(illustHistoryEntity)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
