package ceui.lisa.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ceui.lisa.feature.FeatureEntity

@Database(
    entities = [
        IllustHistoryEntity::class,
        IllustRecmdEntity::class,
        DownloadEntity::class,
        UserEntity::class,
        SearchEntity::class,
        ImageEntity::class,
        MuteEntity::class,
        UUIDEntity::class,
        FeatureEntity::class,
        DownloadingEntity::class,
    ],
    version = 25
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recmdDao(): IllustRecmdDao

    abstract fun downloadDao(): DownloadDao

    abstract fun searchDao(): SearchDao

    companion object {
        @JvmField
        val DATABASE_NAME = "roomDemo-database"

        private var instance: AppDatabase? = null

        private val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE feature_table ADD COLUMN seriesId INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE search_table ADD COLUMN pinned INTEGER NOT NULL DEFAULT 0")
            }
        }

        @JvmStatic
        fun getAppDatabase(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_23_24)
                    .addMigrations(MIGRATION_24_25)
                    .build()
            }
            return instance!!
        }

        @JvmStatic
        fun destroyInstance() {
            instance = null
        }

        @JvmStatic
        fun recmdDao(context: Context): IllustRecmdDao {
            return getAppDatabase(context).recmdDao()
        }

        @JvmStatic
        fun downloadDao(context: Context): DownloadDao {
            return getAppDatabase(context).downloadDao()
        }

        @JvmStatic
        fun searchDao(context: Context): SearchDao {
            return getAppDatabase(context).searchDao()
        }
    }
}
