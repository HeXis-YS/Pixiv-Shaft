package ceui.lisa.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ceui.lisa.feature.FeatureEntity

@Dao
interface DownloadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(illustTask: DownloadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDownloading(entity: DownloadingEntity)

    @Delete
    fun deleteDownloading(entity: DownloadingEntity)

    @Delete
    fun delete(userEntity: DownloadEntity)

    @Query("SELECT * FROM illust_download_table ORDER BY downloadTime DESC LIMIT :limit OFFSET :offset")
    fun getAll(limit: Int, offset: Int): List<DownloadEntity>

    @Query("SELECT * FROM illust_downloading_table")
    fun getAllDownloading(): List<DownloadingEntity>

    @Query("DELETE FROM illust_download_table")
    fun deleteAllDownload()

    @Query("DELETE FROM illust_downloading_table")
    fun deleteAllDownloading()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(illustHistoryEntity: IllustHistoryEntity)

    @Delete
    fun delete(userEntity: IllustHistoryEntity)

    @Query("DELETE FROM illust_table")
    fun deleteAllHistory()

    @Query("SELECT * FROM illust_table ORDER BY time DESC LIMIT :limit OFFSET :offset")
    fun getAllViewHistory(limit: Int, offset: Int): List<IllustHistoryEntity>

    @Query("SELECT * FROM illust_table")
    fun getAllViewHistoryEntities(): List<IllustHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(userEntity: UserEntity)

    @Delete
    fun deleteUser(userEntity: UserEntity)

    @Query("SELECT * FROM user_table ORDER BY loginTime DESC")
    fun getAllUser(): List<UserEntity>

    @Query("SELECT * FROM user_table limit 1")
    fun getCurrentUser(): UserEntity?

    @Query("SELECT * FROM upload_image_table ORDER BY uploadTime DESC")
    fun getUploadedImage(): List<ImageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUploadedImage(imageEntity: ImageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFeature(holder: FeatureEntity)

    @Query("SELECT * FROM feature_table ORDER BY dateTime DESC LIMIT :limit OFFSET :offset")
    fun getFeatureList(limit: Int, offset: Int): List<FeatureEntity>

    @Delete
    fun deleteFeature(userEntity: FeatureEntity)

    @Query("DELETE FROM feature_table")
    fun deleteAllFeature()

    @Query("SELECT * FROM feature_table")
    fun getAllFeatureEntities(): List<FeatureEntity>
}
