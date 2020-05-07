/*
 *
 *   PackageTrackerUSPS
 *    a library that contains classes necessary to handle USPS data
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

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

class USPSApi(private val userID: String, private val context: Context?) {
    private val crashlytics: FirebaseCrashlytics
    private var APIUrl: String? = null
    var trackingNumber: String? = null
    var newEntries: HashMap<String, ArrayList<String>>? = null
        private set
    var manager: USPSManager
    fun getTrackingInfo(trackingID: String): Document? {
        var doc: Document? = null

        //Generate request
        var xml = "<TrackFieldRequest USERID=\"$userID\">"
        xml += "<TrackID ID=\"$trackingID\"></TrackID>"
        xml += "</TrackFieldRequest>"
        val baseURL = context!!.resources.getString(R.string.USPSAPI)
        APIUrl = Uri.parse(baseURL).buildUpon()
                .appendQueryParameter("API", "TrackV2")
                .appendQueryParameter("XML", xml)
                .build().toString()
        Log.i("USPSApi", APIUrl!!)
        val connection: HttpURLConnection
        try {
            val url = URL(APIUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.doOutput = true
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            doc = builder.parse(connection.inputStream)
            connection.disconnect()
        } catch (e: Exception) {
            Log.i("USPSAPIError", e.message!!)
            crashlytics.recordException(e)
        }
        return doc
    }

    fun storeInitial(doc: Document?): Boolean {
        var completed: Boolean
        try {
            //0 - Event Time
            //1 - Event Date
            //2 - Event description
            //3 - Event City
            //4 - Event State
            //5 - Event Zipcode
            //6 - Event Country
            var time: String
            var date: String
            var description: String
            var city: String
            var state: String
            var zipcode: String
            var country: String

            //Summary (Roughly the most recent part of the tracking)
            val summary = doc!!.getElementsByTagName("TrackSummary")
            var summaryNodes: NodeList
            val entries = manager.entries
            for (index in 0 until summary.length) {
                summaryNodes = summary.item(index).childNodes
                if (!entries.contains(trackingNumber)) {
                    time = summaryNodes.item(0).textContent
                    date = summaryNodes.item(1).textContent
                    description = summaryNodes.item(2).textContent
                    city = summaryNodes.item(3).textContent
                    state = summaryNodes.item(4).textContent
                    zipcode = summaryNodes.item(5).textContent
                    country = summaryNodes.item(6).textContent
                    Log.i("USPSApi", trackingNumber + "," + date + "," + time + "," + description + "," + city + "," + state + "," + zipcode + "," + country)
                    manager.addEntry(trackingNumber, date, time, description, city, state, zipcode, country, 0)
                }
            }
            completed = true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("USPSApiError", e.message!!)
            crashlytics.recordException(e)
            completed = false
        }
        return completed
    }

    fun updateHistory(trackingNumber: String) {
        val result = getTrackingInfo(trackingNumber)
        try {
            newEntries = HashMap()
            val entries = ArrayList<String>()

            //0 - Event Time
            //1 - Event Date
            //2 - Event description
            //3 - Event City
            //4 - Event State
            //5 - Event Zipcode
            //6 - Event Country
            var time: String
            var date: String
            var description: String
            var city: String
            var state: String
            var zipcode: String
            var country: String

            //Check and update history
            val completeHistory = getHistory(trackingNumber)
            lateinit var completeEntry: String
            val trackDetail = result!!.getElementsByTagName("TrackDetail")

            //Add the details first
            for (index in trackDetail.length - 1 downTo 0) {
                val trackNodes = trackDetail.item(index).childNodes
                time = trackNodes.item(0).textContent
                date = trackNodes.item(1).textContent
                description = trackNodes.item(2).textContent
                city = trackNodes.item(3).textContent
                state = trackNodes.item(4).textContent
                zipcode = trackNodes.item(5).textContent
                country = trackNodes.item(6).textContent
                completeEntry = "$date,$time,$description,$city,$state,$zipcode,$country"
                Log.i("USPSApi", completeEntry)
                if (!completeHistory.contains(completeEntry)) {
                    entries.add(completeEntry)
                    manager.addHistory(trackingNumber, date, time, description, city, state, zipcode, country, 0)
                }
            }

            //Then add the latest entry at the top last.
            val summary = result.getElementsByTagName("TrackSummary")
            val summaryNodes = summary.item(0).childNodes
            time = summaryNodes.item(0).textContent
            date = summaryNodes.item(1).textContent
            description = summaryNodes.item(2).textContent
            city = summaryNodes.item(3).textContent
            state = summaryNodes.item(4).textContent
            zipcode = summaryNodes.item(5).textContent
            country = summaryNodes.item(6).textContent
            completeEntry = "$date,$time,$description,$city,$state,$zipcode,$country"
            if (!completeHistory.contains(completeEntry)) {
                entries.add(completeEntry)
                manager.addHistory(trackingNumber, date, time, description, city, state, zipcode, country, 0)
            }
            newEntries!![trackingNumber] = entries
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("USPSAPI", e.message!!)
            crashlytics.recordException(e)
        }
    }

    fun initialHistory(trackingNumber: String, activity: FragmentActivity) {

        //Reuse get tracking info for doc.
        val result = getTrackingInfo(trackingNumber)
        try {
            //0 - Event Time
            //1 - Event Date
            //2 - Event description
            //3 - Event City
            //4 - Event State
            //5 - Event Zipcode
            //6 - Event Country
            var time: String
            var date: String
            var description: String
            var city: String
            var state: String
            var zipcode: String
            var country: String

            val errors = result!!.getElementsByTagName("Error")
            if(errors.length > 0)
            {
                val errorNodes = errors.item(0).childNodes
                val errorDescription = errorNodes.item(1).textContent

                activity.run {

                    runOnUiThread{
                        Toast.makeText(context, errorDescription, Toast.LENGTH_LONG).show()
                    }
                }
                throw Exception(errorDescription)
            }

            //Check and update history
            val completeHistory = getHistory(trackingNumber)
            lateinit var completeEntry: String
            val trackDetail = result.getElementsByTagName("TrackDetail")

            //Add the rest of the entire history
            for (index in trackDetail.length - 1 downTo 0) {
                val trackNodes = trackDetail.item(index).childNodes
                time = trackNodes.item(0).textContent
                date = trackNodes.item(1).textContent
                description = trackNodes.item(2).textContent
                city = trackNodes.item(3).textContent
                state = trackNodes.item(4).textContent
                zipcode = trackNodes.item(5).textContent
                country = trackNodes.item(6).textContent
                completeEntry = "$date,$time,$description,$city,$state,$zipcode,$country"
                Log.i("USPSApi", completeEntry)
                if (!completeHistory.contains(completeEntry)) manager.addHistory(trackingNumber, date, time, description, city, state, zipcode, country, 1)
            }

            //Summary (Roughly the most recent part of the tracking)
            val summary = result.getElementsByTagName("TrackSummary")
            val summaryNodes = summary.item(0).childNodes
            time = summaryNodes.item(0).textContent
            date = summaryNodes.item(1).textContent
            description = summaryNodes.item(2).textContent
            city = summaryNodes.item(3).textContent
            state = summaryNodes.item(4).textContent
            zipcode = summaryNodes.item(5).textContent
            country = summaryNodes.item(6).textContent
            completeEntry = "$date,$time,$description,$city,$state,$zipcode,$country"
            if (!completeHistory.contains(completeEntry)) manager.addHistory(trackingNumber, date, time, description, city, state, zipcode, country, 1)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("USPSAPI", e.message!!)
            crashlytics.recordException(e)
        }
    }

    fun getHistory(trackingNumber: String?): ArrayList<String> {
        return manager.getHistory(trackingNumber)
    }

    val trackingNumbers: ArrayList<String>
        get() = manager.entries

    fun closeDatabase() {
        manager.close()
    }

    init {
        manager = USPSManager(context, "USPS", null, 1)
        crashlytics = FirebaseCrashlytics.getInstance()
    }
}