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

public class USPSManager extends SQLiteOpenHelper
{
    private SQLiteDatabase db;

    public USPSManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context, name, factory, version);

        db = this.getWritableDatabase();

        Log.i("USPSManager","Initializer called.");
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        Log.i("USPSManager","On create called");

        db.execSQL("Create table if not exists TrackingNumbers (trackingnumber TEXT)");
        db.execSQL("Create table if not exists History (id INTEGER not null, trackingnumber TEXT, historyInfo TEXT, date TEXT, time TEXT, city TEXT, state TEXT, zipcode TEXT, country TEXT, seen INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.i("USPSManager","Upgrade called");
    }

    public void addEntry(String trackingNumber, String date, String time, String historyInfo, String city, String state, String zipcode, String country)
    {
        ContentValues insert = new ContentValues();
        insert.put("trackingnumber", trackingNumber);

        db.insert("TrackingNumbers",null,insert);

        addHistory(trackingNumber, date, time, historyInfo, city, state, zipcode, country);
    }

    public void addHistory(String trackingNumber, String date, String time, String historyInfo, String city, String state, String zipcode, String country)
    {
        Cursor cursor;
        cursor = db.rawQuery("select max(id) from History",null);
        cursor.moveToFirst();

        int id = cursor.getInt(0) + 1;
        ContentValues insert = new ContentValues();
        insert.put("id",id);
        insert.put("trackingnumber", trackingNumber);
        insert.put("date", date);
        insert.put("historyinfo", historyInfo);
        insert.put("time", time);
        insert.put("city", city);
        insert.put("state", state);
        insert.put("zipcode", zipcode);
        insert.put("country", country);

        db.insert("History",null,insert);
        cursor.close();
    }

    public void deleteEntryAndHistory(String trackingNumber)
    {
        db.delete("TrackingNumbers", "trackingnumber = ?", new String[]{trackingNumber});
        db.delete("History", "trackingnumber = ?", new String[]{trackingNumber});
    }

    public ArrayList<String> getEntries()
    {
        ArrayList<String> entries = new ArrayList<String>();
        Cursor cursor = db.query("TrackingNumbers",null, null, null, null, null, null);
        cursor.moveToFirst();
        if(cursor.getCount() > 0)
        {
            do
            {
                entries.add(cursor.getString(cursor.getColumnIndex("trackingnumber")));
            }
            while(cursor.moveToNext());
        }

        return entries;
    }

    public ArrayList<String> getHistory(String trackingNumber)
    {
        Cursor cursor = db.query("History",null,"trackingnumber = ?",new String[]{trackingNumber}, null, null, null);
        cursor.moveToFirst();

        ArrayList<String> history = new ArrayList<>();

        if(cursor.getCount() > 0)
        {
            do
            {
                String date = cursor.getString(cursor.getColumnIndex("date"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String historyinfo = cursor.getString(cursor.getColumnIndex("historyInfo"));
                String city = cursor.getString(cursor.getColumnIndex("city"));
                String state = cursor.getString(cursor.getColumnIndex("state"));
                String zipcode = cursor.getString(cursor.getColumnIndex("zipcode"));
                String country = cursor.getString(cursor.getColumnIndex("country"));

                Log.i("USPSManager",date+","+time+","+historyinfo+","+city+","+state+","+zipcode+","+country);

                history.add(date+","+time+","+historyinfo+","+city+","+state+","+zipcode+","+country);
            }while(cursor.moveToNext());
        }

        cursor.close();
        return history;
    }

    public ArrayList<String> getHistoryForDisplay(String trackingNumber)
    {
        Cursor cursor = db.query("History",null,"trackingnumber = ?",new String[]{trackingNumber}, null, null, "id DESC");
        cursor.moveToFirst();

        ArrayList<String> history = new ArrayList<String>();

        if(cursor.getCount() > 0)
        {
            do
            {
                String date = cursor.getString(cursor.getColumnIndex("date"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String historyinfo = cursor.getString(cursor.getColumnIndex("historyInfo"));
                String city = cursor.getString(cursor.getColumnIndex("city"));
                String state = cursor.getString(cursor.getColumnIndex("state"));
                String zipcode = cursor.getString(cursor.getColumnIndex("zipcode"));
                String country = cursor.getString(cursor.getColumnIndex("country"));

                Log.i("USPSManager",date+","+time+","+historyinfo+","+city+","+state+","+zipcode+","+country);

                String historyString = "";

                //Format display.
                if(!date.isEmpty()) historyString += date;

                if(!time.isEmpty()) historyString += "\n" + time;

                if(!historyinfo.isEmpty()) historyString += "\n" + historyinfo;

                if(!city.isEmpty()) historyString += "\n" + city;

                if(!state.isEmpty()) historyString += "\n" + state;

                if(!zipcode.isEmpty()) historyString += "\n" + zipcode;

                if(!country.isEmpty()) historyString += "\n" + country;

                history.add(historyString);
            }while(cursor.moveToNext());
        }

        cursor.close();
        return history;
    }

}