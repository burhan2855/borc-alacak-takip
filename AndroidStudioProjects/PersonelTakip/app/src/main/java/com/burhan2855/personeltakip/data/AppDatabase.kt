package com.burhan2855.personeltakip.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Employee::class, WorkLog::class, Adjustment::class], version = 9, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun workLogDao(): WorkLogDao
    abstract fun adjustmentDao(): AdjustmentDao

    companion object {
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE employees ADD COLUMN iban TEXT")
                database.execSQL("ALTER TABLE employees ADD COLUMN phoneNumber TEXT")
                database.execSQL("ALTER TABLE employees ADD COLUMN email TEXT")
                database.execSQL("ALTER TABLE employees ADD COLUMN address TEXT")
            }
        }
    }
}
