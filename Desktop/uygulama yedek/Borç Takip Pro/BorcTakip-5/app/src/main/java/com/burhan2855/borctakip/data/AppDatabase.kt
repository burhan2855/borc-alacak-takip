package com.burhan2855.borctakip.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.burhan2855.borctakip.data.calendar.CalendarEvent
import com.burhan2855.borctakip.data.calendar.CalendarEventDao
import com.burhan2855.borctakip.data.calendar.CalendarSettings
import com.burhan2855.borctakip.data.calendar.CalendarSettingsDao

@Database(
    entities = [
        Contact::class,
        Transaction::class,
        CalendarEvent::class,
        CalendarSettings::class,
        PartialPayment::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun transactionDao(): TransactionDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun calendarSettingsDao(): CalendarSettingsDao
    abstract fun partialPaymentDao(): PartialPaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "debt_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
