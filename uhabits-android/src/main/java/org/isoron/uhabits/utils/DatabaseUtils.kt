/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.isoron.uhabits.utils

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import org.isoron.uhabits.HabitsApplication.Companion.isTestMode
import org.isoron.uhabits.HabitsDatabaseOpener
import org.isoron.uhabits.core.DATABASE_FILENAME
import org.isoron.uhabits.core.DATABASE_VERSION
import org.isoron.uhabits.core.utils.DateFormats.Companion.getBackupDateFormat
import org.isoron.uhabits.core.utils.DateUtils.Companion.getLocalTime
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat

object DatabaseUtils {
    private var opener: HabitsDatabaseOpener? = null

    @JvmStatic
    fun getDatabaseFile(context: Context): File {
        val databaseFilename = databaseFilename
        val root = context.filesDir.path
        return File("$root/../databases/$databaseFilename")
    }

    private val databaseFilename: String
        get() {
            var databaseFilename: String = DATABASE_FILENAME
            if (isTestMode()) databaseFilename = "test.db"
            return databaseFilename
        }

    fun initializeDatabase(context: Context?) {
        opener = HabitsDatabaseOpener(
            context!!,
            databaseFilename,
            DATABASE_VERSION
        )
    }

    @JvmStatic
    @Throws(IOException::class)
    fun saveDatabaseCopy(context: Context, dir: File): String {
        val downloads_filename = "Loop_Habits_Backup.db"
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, downloads_filename)
            put(MediaStore.Downloads.MIME_TYPE, "application/x-sqlite3")
            put(MediaStore.Downloads.RELATIVE_PATH, "Download/Loop-Habits-DB")
        }
        // First try to find and delete any existing file
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("$downloads_filename%")
        val resolver = context.contentResolver
        Log.i("DatabaseUtils", "Checking for existing backup file...")
        Log.i("DatabaseUtils", "Query selection: $selection")
        Log.i("DatabaseUtils", "Query args: ${selectionArgs.joinToString()}")
        // Query for existing file
        resolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Downloads._ID),
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                // File exists, delete it
                val id = cursor.getLong(0)
                val uri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id)
                val deleteCount = resolver.delete(uri, null, null)
                Log.i("DatabaseUtils", "Found existing backup file, deleted $deleteCount files")
            } else {
                Log.i("DatabaseUtils", "No existing backup file found")
            }
        }
        Log.i("DatabaseUtils", "Creating new backup file...")
        // Now create the new file
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IOException("Failed to create new MediaStore record.")
        Log.i("DatabaseUtils", "Writing backup data...")
        resolver.openOutputStream(uri, "wt")?.use { outputStream ->
            getDatabaseFile(context).inputStream().use { input ->
                input.copyTo(outputStream)
            }
        } ?: throw IOException("Failed to open output stream")
        Log.i("DatabaseUtils", "Successfully wrote backup to downloads")

        // Create backup in the app's backup directory
        val dateFormat: SimpleDateFormat = getBackupDateFormat()
        val date = dateFormat.format(getLocalTime())
        val filename = "${dir.absolutePath}/Loop Habits Backup $date.db"
        Log.i("DatabaseUtils", "Writing: $filename")
        val db = getDatabaseFile(context)
        val dbCopy = File(filename)
        db.copyTo(dbCopy)
        return dbCopy.absolutePath
    }

    fun openDatabase(): SQLiteDatabase {
        checkNotNull(opener)
        return opener!!.writableDatabase
    }
}
