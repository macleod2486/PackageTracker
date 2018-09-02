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

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class USPSApi
{
    String userID;
    String APIUrl;

    public USPSApi(String userID, String APIUrl)
    {
        this.userID = userID;
        this.APIUrl = APIUrl;
    }

    public String getTrackingInfo(String trackingIDs)
    {
        //Generate request
        String xml = "<TrackFieldRequest USERID=\""+userID+"\">";

        String[] listOfIds = trackingIDs.split(",");

        for (String id: listOfIds)
        {
            xml += "<TrackID ID=\""+id+"\"></TrackID>";
        }

        xml += "</TrackFieldRequest>";

        String result = "";

        this.APIUrl = Uri.parse(this.APIUrl).buildUpon()
                .appendQueryParameter("API","TrackV2")
                .appendQueryParameter("XML",xml)
                .build().toString();

        Log.i("USPSApi",this.APIUrl);

        HttpURLConnection connection = null;
        try
        {
            URL url = new URL(APIUrl);

            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line = reader.readLine();
            while(line != null)
            {
                Log.i("USPSApi",line.toString());
                line = reader.readLine();
            }

            connection.disconnect();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            result = "Error";
        }

        return result;
    }
}