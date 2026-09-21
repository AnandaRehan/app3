package com.ehan.app3.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ChatMessage::class],
    version = 3,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao

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
                            MIGRATION_2_3
                        )
                        .build()

                INSTANCE = instance

                instance
            }
        }
    }
}