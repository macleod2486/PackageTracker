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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
        db.execSQL("Create table if not exists History (id int not null, trackingnumberid string(80), historyInfo string(500), date string(200), time string(30), city string(50), state string(2), string zipcode(5), string country(2), seen int(2), primary key(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.i("PackageDatabaseManager","Upgrade called");
    }
}