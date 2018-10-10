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
import android.net.Uri;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class USPSApi
{
    String userID;
    String APIUrl;
    String[] listOfIds;

    PackageDatabaseManager manager;

    public USPSApi(String userID, String APIUrl, Context context)
    {
        this.userID = userID;
        this.APIUrl = APIUrl;
        this.manager = new PackageDatabaseManager(context,"PackageManager",null, 1);
    }

    public String getTrackingInfo(String trackingIDs)
    {
        //Generate request
        String xml = "<TrackFieldRequest USERID=\""+userID+"\">";

        listOfIds = trackingIDs.split(",");

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

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(connection.getInputStream());

            connection.disconnect();

            storeInitial(doc);
        }
        catch (Exception e)
        {
            Log.i("USPSAPIError",e.getMessage());
            result = "Error";
        }

        return result;
    }

    public boolean storeInitial(Document doc)
    {
        boolean completed;

        try
        {
            //0 - Event Time
            //1 - Event Date
            //2 - Event description
            //3 - Event City
            //4 - Event State
            //5 - Event Zipcode
            //6 - Event Country

            String trackingNumber;
            String time;
            String date;
            String description;
            String city;
            String state;
            String zipcode;
            String country;

            //Summary (Roughly the most recent part of the tracking)
            NodeList summary = doc.getElementsByTagName("TrackSummary");
            NodeList summaryNodes;
            ArrayList<String> entries = manager.getEntries(null, null, true);

            for(int index = 0; index < summary.getLength(); index++)
            {
                summaryNodes = summary.item(index).getChildNodes();

                trackingNumber = listOfIds[index];
                if(entries.contains(trackingNumber)) continue;
                time = summaryNodes.item(0).getTextContent();
                date = summaryNodes.item(1).getTextContent();
                description = summaryNodes.item(2).getTextContent();
                city = summaryNodes.item(3).getTextContent();
                state = summaryNodes.item(4).getTextContent();
                zipcode = summaryNodes.item(5).getTextContent();
                country = summaryNodes.item(6).getTextContent();

                manager.addEntry(trackingNumber,"USPS", date, time, description, city, state, zipcode, country);
            }

            completed = true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Log.e("USPSApiError",e.getMessage());
            completed = false;
        }

        return completed;
    }

    public void updateHistory(String trackingNumber)
    {

    }

    public ArrayList<String> getHistory(String trackingNumber)
    {
        String trackingId = manager.getTrackingId(trackingNumber);
        ArrayList<String> fullHistory = manager.getHistory(null, trackingId, null, false);

        return fullHistory;
    }
}