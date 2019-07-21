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
    private String userID;
    private String APIUrl;
    private String[] listOfIds;

    USPSManager manager;

    public USPSApi(String userID, String APIUrl, Context context)
    {
        this.userID = userID;
        this.APIUrl = APIUrl;
        this.manager = new USPSManager(context,"PackageManager",null, 1);
    }

    public Document getTrackingInfo(String trackingIDs)
    {
        Document doc = null;

        //Generate request
        String xml = "<TrackFieldRequest USERID=\""+userID+"\">";

        listOfIds = trackingIDs.split(",");

        for (String id: listOfIds)
        {
            xml += "<TrackID ID=\""+id+"\"></TrackID>";
        }

        xml += "</TrackFieldRequest>";

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
            doc = builder.parse(connection.getInputStream());

            connection.disconnect();

            storeInitial(doc);
        }
        catch (Exception e)
        {
            Log.i("USPSAPIError",e.getMessage());
        }

        return doc;
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
            ArrayList<String> entries = manager.getEntries(null, null,false);

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

                Log.i("USPSApi",date+","+time+","+description+","+city+","+state+","+zipcode+","+country);

                manager.addEntry(trackingNumber, date, time, description, city, state, zipcode, country);
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

        //Reuse get tracking info for doc.

        String trackingId = manager.getTrackingId(trackingNumber);
        Document result = getTrackingInfo(trackingNumber);

        try
        {
            //0 - Event Time
            //1 - Event Date
            //2 - Event description
            //3 - Event City
            //4 - Event State
            //5 - Event Zipcode
            //6 - Event Country

            String time;
            String date;
            String description;
            String city;
            String state;
            String zipcode;
            String country;

            //Summary (Roughly the most recent part of the tracking)
            NodeList summary = result.getElementsByTagName("TrackSummary");
            NodeList summaryNodes;
            ArrayList<String> entries = manager.getEntries(null, null, true);

            //Check and update history
            ArrayList<String> completeHistory = getHistory(trackingNumber);
            String completeEntry = "";

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

                completeEntry = date+","+time+","+description+","+city+","+state+","+zipcode+","+country;

                if(!completeHistory.contains(completeEntry))
                    manager.addHistory(Integer.parseInt(trackingId), date, time, description, city, state, zipcode, country);

            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Log.e("USPSAPI",e.getMessage());
        }

    }

    public ArrayList<String> getHistory(String trackingNumber)
    {
        String trackingId = manager.getTrackingId(trackingNumber);
        return manager.getHistory(null, trackingId, null, false);
    }

    public ArrayList<String> getTrackingNumbers()
    {
        return manager.getEntries(null, null, true);
    }
}