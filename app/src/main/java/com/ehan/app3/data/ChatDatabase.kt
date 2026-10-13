package com.ehan.app3.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ChatMessage::class, DownloadedMedia::class],
    version = 5,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao
    abstract fun downloadedMediaDao(): DownloadedMediaDao

    companion object {

        @Volatile
        private var INSTANCE: ChatDatabase? = null

        private val MIGRATION_1_2 =
            object : Migration(1, 2) {

                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        ALTER TABLE messages
                        ADD COLUMN isProcessed INTEGER
                        NOT NULL DEFAULT 0
                        """
                    )
                }
            }

        private val MIGRATION_2_3 =
            object : Migration(2, 3) {

                override fun migrate(
                    db: SupportSQLiteDatabase
                ) {
                    db.execSQL(
                        """
                        ALTER TABLE messages
                        ADD COLUMN status TEXT
                        NOT NULL DEFAULT 'PENDING'
                        """
                    )

                    db.execSQL(
                        """
                        UPDATE messages
                        SET status = 'PROCESSED'
                        WHERE isBot = 1
                        """
                    )

                    db.execSQL(
                        """
                        UPDATE messages
                        SET status = 'PROCESSED'
                        WHERE isBot = 0
                        AND isProcessed = 1
                        """
                    )
                }
            }

        private val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        ALTER TABLE messages
                        ADD COLUMN payloadJson TEXT
                        DEFAULT NULL
                        """
                    )
                }
            }

        private val MIGRATION_4_5 =
            object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS downloaded_media (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            title TEXT NOT NULL,
                            mediaType TEXT NOT NULL,
                            sourcePlatform TEXT NOT NULL,
                            filePath TEXT,
                            downloadUrl TEXT NOT NULL,
                            thumbnailUrl TEXT,
                            fileSizeFormatted TEXT,
                            timestamp INTEGER NOT NULL,
                            durationFormatted TEXT
                        )
                        """
                    )
                }
            }

        fun getDatabase(
            context: Context
        ): ChatDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        ChatDatabase::class.java,
                        "chat_database"
                    )
                        .addMigrations(
                            MIGRATION_1_2,
                            MIGRATION_2_3,
                            MIGRATION_3_4,
                            MIGRATION_4_5
                        )
                        .fallbackToDestructiveMigration()
                        .build()

                INSTANCE = instance

                instance
            }
        }
    }
}
