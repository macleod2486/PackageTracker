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

package com.macleod2486.packagetracker.tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class PackageDatabaseManager extends SQLiteOpenHelper
{
    private SQLiteDatabase db;

    public PackageDatabaseManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context, name, factory, version);

        db = this.getWritableDatabase();

        Log.i("PackageDatabaseManager","Initializer called.");
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        Log.i("PackageDatabaseManager","On create called");

        db.execSQL("Create table if not exists TrackingNumbers (id int not null, trackingnumber string(80), service string(30), primary key(id))");
        db.execSQL("Create table if not exists History (id int not null, trackingnumberid string(80), historyInfo string(500), date string(200), time string(30), city string(50), state string(2), zipcode string(5), country string(2), seen int(2), primary key(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.i("PackageDatabaseManager","Upgrade called");
    }

    public void addEntry(String trackingNumber, String service, String date, String time, String historyInfo, String city, String state, String zipcode, String country)
    {
        Cursor cursor;
        cursor = db.rawQuery("select max(id) from TrackingNumbers",null);
        cursor.moveToFirst();

        int id = cursor.getInt(0) + 1;
        ContentValues insert = new ContentValues();
        insert.put("id", id);
        insert.put("trackingnumber", trackingNumber);
        insert.put("service", service);

        db.insert("TrackingNumbers",null,insert);
        cursor.close();

        addHistory(id, date, time, historyInfo, city, state, zipcode, country);
    }

    public void addHistory(int trackingNumberId, String date, String time, String historyInfo, String city, String state, String zipcode, String country)
    {
        Cursor cursor;
        cursor = db.rawQuery("select max(id) from TrackingNumbers",null);
        cursor.moveToFirst();

        int id = cursor.getInt(0) + 1;
        ContentValues insert = new ContentValues();
        insert.put("id",id);
        insert.put("trackingnumberid", trackingNumberId);
        insert.put("date", date);
        insert.put("historyinfo", historyInfo);
        insert.put("date", date);
        insert.put("city", city);
        insert.put("state", state);
        insert.put("zipcode", zipcode);
        insert.put("country", country);

        db.insert("History",null,insert);
        cursor.close();
    }

    public void updateEntry()
    {

    }

    public void deleteEntryAndHistory()
    {

    }

    public ArrayList<String> getEntries(Cursor cursor, ArrayList<String> entries, boolean notReachedend)
    {
        if(cursor == null)
        {
            cursor = db.rawQuery("select trackingnumber from TrackingNumbers",null);
            cursor.moveToFirst();
        }
        if(entries == null) entries = new ArrayList<String>();

        if(cursor.getCount() > 0 && notReachedend)
        {
            entries.add(cursor.getString(cursor.getColumnIndex("trackingnumber")));
            notReachedend = cursor.moveToNext();
            getEntries(cursor, entries, notReachedend);
        }

        return entries;
    }

    public void getHistory()
    {

    }
}