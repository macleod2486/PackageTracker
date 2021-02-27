/*
 *
 *   PackageTracker
 *    a simple application for keeping track of multiple packages from multiple services
 *
 *    Copyright (C) 2018  Manuel Gonzales Jr.
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see [http://www.gnu.org/licenses/].
 *
 */
package com.macleod2486.packagetrackerusps

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.*

class USPSManager(context: Context?, name: String?, factory: CursorFactory?, version: Int) : SQLiteOpenHelper(context, name, factory, version) {
    private val db: SQLiteDatabase
    override fun onCreate(db: SQLiteDatabase) {
        Log.i("USPSManager", "On create called")
        db.execSQL("Create table if not exists TrackingNumbers (trackingnumber TEXT, nick TEXT)")
        db.execSQL("Create table if not exists History (id INTEGER not null, trackingnumber TEXT, historyInfo TEXT, date TEXT, time TEXT, city TEXT, state TEXT, zipcode TEXT, country TEXT, seen INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.i("USPSManager", "Upgrade called")
        if(newVersion == 3)
        {
            val convert = db.query("History", null, "", null, null, null, null);
            convert.moveToFirst()
            do {

                val id = convert.getString(convert.getColumnIndex("id"))
                val time = convert.getString(convert.getColumnIndex("time"))
                val convertTo = convertToMilitaryTime(time)

                val dataUpdate = ContentValues()
                dataUpdate.put("time", convertTo)

                db.update("History", dataUpdate, "id=?", arrayOf(id))

            }while(convert.moveToNext())
            convert.close()
        }

        if(newVersion >= 3)
        {
            db.execSQL("ALTER TABLE TrackingNumbers ADD COLUMN nick TEXT;")
        }
    }

    fun addEntry(trackingNumber: String?) {
        val insert = ContentValues()
        insert.put("trackingnumber", trackingNumber)
        db.insert("TrackingNumbers", null, insert)
    }

    fun addHistory(trackingNumber: String?, date: String?, time: String?, historyInfo: String?, city: String?, state: String?, zipcode: String?, country: String?, seen: Int) {
        val cursor = db.rawQuery("select max(id) from History", null)
        cursor.moveToFirst()
        val id = cursor.getInt(0) + 1
        val insert = ContentValues()
        insert.put("id", id)
        insert.put("trackingnumber", trackingNumber)
        insert.put("date", date)
        insert.put("historyinfo", historyInfo)
        insert.put("time", convertToMilitaryTime(time))
        insert.put("city", city)
        insert.put("state", state)
        insert.put("zipcode", zipcode)
        insert.put("country", country)
        insert.put("seen", seen)
        db.insert("History", null, insert)
        cursor.close()
    }

    fun deleteEntryAndHistory(trackingNumber: String?) {
        db.delete("TrackingNumbers", "trackingnumber = ?", arrayOf(trackingNumber))
        db.delete("History", "trackingnumber = ?", arrayOf(trackingNumber))
    }

    val entries: ArrayList<String>
        get() {
            val entries = ArrayList<String>()
            val cursor = db.query("TrackingNumbers", null, null, null, null, null, null)
            cursor.moveToFirst()
            if (cursor.count > 0) {
                do {
                    val trackingNumber = cursor.getString(cursor.getColumnIndex("trackingnumber"))
                    val nickName = cursor.getString(cursor.getColumnIndex("nick"))
                    val entry = if(!nickName.isNullOrBlank()) nickName else trackingNumber
                    entries.add(entry)
                } while (cursor.moveToNext())
            }
            cursor.close()
            return entries
        }

    fun getHistory(trackingNumber: String?): ArrayList<String> {
        var cursor = db.query("History", null, "trackingnumber = ?", arrayOf(trackingNumber), null, null, null)
        if(cursor.count == 0)
        {
            val trueTrackingNumber = getTrackingNumber(trackingNumber)
            if(trueTrackingNumber.isNotBlank())
                cursor = db.query("History", null, "trackingnumber = ?", arrayOf(trueTrackingNumber), null, null, null)
        }
        cursor.moveToFirst()
        val history = ArrayList<String>()
        if (cursor.count > 0) {
            do {
                val date = cursor.getString(cursor.getColumnIndex("date"))
                val time = convertFromMilitaryTime(cursor.getString(cursor.getColumnIndex("time")))
                val historyinfo = cursor.getString(cursor.getColumnIndex("historyInfo"))
                val city = cursor.getString(cursor.getColumnIndex("city"))
                val state = cursor.getString(cursor.getColumnIndex("state"))
                val zipcode = cursor.getString(cursor.getColumnIndex("zipcode"))
                val country = cursor.getString(cursor.getColumnIndex("country"))
                Log.i("USPSManager", "$date,$time,$historyinfo,$city,$state,$zipcode,$country")
                history.add("$date,$time,$historyinfo,$city,$state,$zipcode,$country")
            } while (cursor.moveToNext())
        }
        cursor.close()
        return history
    }

    fun getHistoryForDisplay(trackingNumber: String?): ArrayList<String> {
        var cursor = db.query("History", null, "trackingnumber = ?", arrayOf(trackingNumber), null, null, "date DESC, time DESC")
        Log.i("USPSManager", "Cursor size ${cursor.count}")
        if(cursor.count == 0)
        {
            val trueTrackingNumber = getTrackingNumber(trackingNumber)
            cursor = db.query("History", null, "trackingnumber = ?", arrayOf(trueTrackingNumber), null, null, "date DESC, time DESC")
        }
        cursor.moveToFirst()
        val history = ArrayList<String>()
        if (cursor.count > 0) {
            do {
                val date = cursor.getString(cursor.getColumnIndex("date"))
                val time = convertFromMilitaryTime(cursor.getString(cursor.getColumnIndex("time")))
                val historyinfo = cursor.getString(cursor.getColumnIndex("historyInfo"))
                val city = cursor.getString(cursor.getColumnIndex("city"))
                val state = cursor.getString(cursor.getColumnIndex("state"))
                val zipcode = cursor.getString(cursor.getColumnIndex("zipcode"))
                val country = cursor.getString(cursor.getColumnIndex("country"))
                Log.i("USPSManager", "$date,$time,$historyinfo,$city,$state,$zipcode,$country")
                var historyString = ""

                //Format display.
                if (!date.isEmpty()) historyString += date
                if (!time.isEmpty()) historyString += """
                $time
                 """.replaceIndent("\n")
                            if (!historyinfo.isEmpty()) historyString += """
            
                 $historyinfo
                 """.trimIndent()
                            if (!city.isEmpty()) historyString += """
            
                 $city
                 """.trimIndent()
                            if (!state.isEmpty()) historyString += """
            
                 $state
                 """.trimIndent()
                            if (!zipcode.isEmpty()) historyString += """
            
                 $zipcode
                 """.trimIndent()
                            if (!country.isEmpty()) historyString += """
            
                 $country
                 """.trimIndent()
                            history.add(historyString)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return history
    }

    fun setNick(trackingNumber: String, nick: String)
    {
        val nickValue = ContentValues()
        nickValue.put("nick", nick)

        val cursor = db.query("TrackingNumbers", null, "trackingnumber = ?", arrayOf(trackingNumber), null, null, null)

        if(cursor.count == 0)
        {
            val trueTrackingNumber = getTrackingNumber(trackingNumber)
            db.update("TrackingNumbers",  nickValue, "trackingnumber = ?", arrayOf(trueTrackingNumber))
        }
        else
        {
            db.update("TrackingNumbers",  nickValue, "trackingnumber = ?", arrayOf(trackingNumber))
        }
    }

    init {
        db = this.writableDatabase
        Log.i("USPSManager", "Initializer called.")
    }

    private fun convertFromMilitaryTime(time: String) : String
    {
        var conversion = ""
        if(time.isNotBlank() && (!time.contains("am") || !time.contains("pm")))
        {
            var shift = 0
            var hours = Integer.parseInt(time.substring(0, time.length - 2))
            if(hours > 12) shift = 12
            hours -= shift;
            val minutes = time.substring(time.length - 2)

            conversion = hours.toString()+":"+minutes

            if(shift == 0) conversion += " am"
            else conversion += " pm"
        }
        return conversion
    }

    private fun convertToMilitaryTime(time: String?) : String
    {
        var conversion = ""
        if(!time.isNullOrBlank() && (time.contains("am") || time.contains("pm")))
        {
            var shift = 0
            if(time.toLowerCase(Locale.getDefault()).contains("pm")) shift = 12
            val split = time.split(":")
            val hours = Integer.parseInt(split[0]) + shift
            conversion = hours.toString() + split[1].substring(0, 2)
        }
        return conversion
    }

    private fun getTrackingNumber(nickname: String?): String
    {
        val number = db.query("TrackingNumbers", null, "nick = ?", arrayOf(nickname), null, null, null)
        number.moveToFirst()
        if(number.count > 0)
        {
            Log.i("USPSManager", "Number ${number.count}")
            val trackingNumber = number.getString(number.getColumnIndex("trackingnumber"))
            number.close()
            return trackingNumber
        }
        else
        {
            return ""
        }
    }
}