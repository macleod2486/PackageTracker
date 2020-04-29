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
package com.macleod2486.packagetracker.fragments

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.macleod2486.packagetracker.R
import com.macleod2486.packagetrackerusps.USPSApi

class USPS : Fragment()
{
    lateinit var addUSPSTracking: Button
    lateinit var trackingIDs: String
    lateinit var progress: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val uspsView = inflater.inflate(R.layout.fragment_usps, container, false)
        val toolbar: Toolbar = uspsView.findViewById(R.id.uspstoolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        toolbar.title = "Add tracking numbers"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.white))
        toolbar.setNavigationOnClickListener { view: View? -> requireActivity().supportFragmentManager.popBackStack() }
        val text = uspsView.findViewById<EditText>(R.id.trackingEntry)

        addUSPSTracking = uspsView.findViewById(R.id.addUSPS)
        addUSPSTracking.setOnClickListener(View.OnClickListener { v: View? ->
            trackingIDs = text.text.toString().replace("\\s".toRegex(), "")
            InitalizeEntry().execute()
        })

        progress = uspsView.findViewById(R.id.uspsAddProgress)
        progress.setVisibility(View.INVISIBLE)
        return uspsView
    }

    private inner class InitalizeEntry : AsyncTask<Void?, Void?, Void?>() {
        protected override fun doInBackground(vararg params: Void?): Void? {
            activity!!.runOnUiThread { progress!!.visibility = View.VISIBLE }
            val userId = activity!!.resources.getString(R.string.USPSApiUserID)
            val apiTool = USPSApi(userId, context)
            val ids = trackingIDs!!.split(",").toTypedArray()
            for (id in ids) {
                val result = apiTool.getTrackingInfo(id)
                apiTool.trackingNumber = id
                apiTool.storeInitial(result)
                apiTool.initialHistory(id)
            }
            apiTool.closeDatabase()
            return null
        }

        override fun onPostExecute(result: Void?) {
            activity!!.runOnUiThread { progress!!.visibility = View.INVISIBLE }
            val manager = activity!!.supportFragmentManager
            manager.popBackStack()
        }
    }
}