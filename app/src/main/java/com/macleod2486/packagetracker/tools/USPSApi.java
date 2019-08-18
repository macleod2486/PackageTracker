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

import com.macleod2486.packagetracker.R;

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
    private Context context;
    private String[] listOfIds;

    USPSManager manager;

    public USPSApi(String userID, Context context)
    {
        this.userID = userID;
        this.context = context;
        this.manager = new USPSManager(context,"USPS",null, 1);
    }

    public Document getTrackingInfo(String trackingID)
    {
        Document doc = null;

        //Generate request
        String xml = "<TrackFieldRequest USERID=\""+userID+"\">";

        xml += "<TrackID ID=\""+trackingID+"\"></TrackID>";

        xml += "</TrackFieldRequest>";

        String baseURL = context.getResources().getString(R.string.USPSAPI);
        this.APIUrl = Uri.parse(baseURL).buildUpon()
                .appendQueryParameter("API","TrackV2")
                .appendQueryParameter("XML",xml)
                .build().toString();

        Log.i("USPSApi",this.APIUrl);

        HttpURLConnection connection;
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
            ArrayList<String> entries = manager.getEntries();

            for(int index = 0; index < summary.getLength(); index++)
            {
                summaryNodes = summary.item(index).getChildNodes();

                trackingNumber = listOfIds[index];
                if(!entries.contains(trackingNumber))
                {
                    time = summaryNodes.item(0).getTextContent();
                    date = summaryNodes.item(1).getTextContent();
                    description = summaryNodes.item(2).getTextContent();
                    city = summaryNodes.item(3).getTextContent();
                    state = summaryNodes.item(4).getTextContent();
                    zipcode = summaryNodes.item(5).getTextContent();
                    country = summaryNodes.item(6).getTextContent();

                    Log.i("USPSApi",trackingNumber+","+date+","+time+","+description+","+city+","+state+","+zipcode+","+country);

                    manager.addEntry(trackingNumber, date, time, description, city, state, zipcode, country);
                }

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

        int trackingId = manager.getTrackingId(trackingNumber);
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

            //Check and update history
            ArrayList<String> completeHistory = getHistory(trackingNumber);
            String completeEntry = "";

            NodeList summaryNodes = summary.item(0).getChildNodes();

            time = summaryNodes.item(0).getTextContent();
            date = summaryNodes.item(1).getTextContent();
            description = summaryNodes.item(2).getTextContent();
            city = summaryNodes.item(3).getTextContent();
            state = summaryNodes.item(4).getTextContent();
            zipcode = summaryNodes.item(5).getTextContent();
            country = summaryNodes.item(6).getTextContent();

            completeEntry = date+","+time+","+description+","+city+","+state+","+zipcode+","+country;

            if(!completeHistory.contains(completeEntry))
                manager.addHistory(trackingId, date, time, description, city, state, zipcode, country);

            NodeList trackDetail = result.getElementsByTagName("TrackDetail");

            //Add the rest of the entire history
            for(int index = 0; index < trackDetail.getLength(); index++)
            {
                NodeList trackNodes = trackDetail.item(index).getChildNodes();

                time = trackNodes.item(0).getTextContent();
                date = trackNodes.item(1).getTextContent();
                description = trackNodes.item(2).getTextContent();
                city = trackNodes.item(3).getTextContent();
                state = trackNodes.item(4).getTextContent();
                zipcode = trackNodes.item(5).getTextContent();
                country = trackNodes.item(6).getTextContent();

                completeEntry = date+","+time+","+description+","+city+","+state+","+zipcode+","+country;
                Log.i("USPSApi", completeEntry);

                if(!completeHistory.contains(completeEntry))
                    manager.addHistory(trackingId, date, time, description, city, state, zipcode, country);

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
        int trackingId = manager.getTrackingId(trackingNumber);
        return manager.getHistory(trackingId);
    }

    public ArrayList<String> getTrackingNumbers()
    {
        return manager.getEntries();
    }

    public void closeDatabase()
    {
        manager.close();
    }
}