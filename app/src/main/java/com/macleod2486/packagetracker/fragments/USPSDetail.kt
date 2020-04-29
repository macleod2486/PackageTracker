/*
 *
 *   PackageTracker
 *    a simple application for keeping track of multiple packages from multiple services
 *
 *    Copyright (C) 2019  Manuel Gonzales Jr.
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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.macleod2486.packagetracker.PackageTrackerApplication
import com.macleod2486.packagetracker.R
import com.macleod2486.packagetrackerusps.USPSManager

class USPSDetail : Fragment() {
    var trackingNumber: String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val uspsDetailView = inflater.inflate(R.layout.fragment_usps_detail, container, false)
        trackingNumber = requireArguments().getString("trackingnumber")
        if (trackingNumber != null) {
            val toolbar: Toolbar = uspsDetailView.findViewById(R.id.uspsdetailstoolbar)
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
            toolbar.title = trackingNumber
            toolbar.setTitleTextColor(resources.getColor(android.R.color.white))
            toolbar.setNavigationOnClickListener { view: View? -> PackageTrackerApplication.navController.popBackStack() }
            val manager = USPSManager(context, "USPS", null, 1)
            val historyList = manager.getHistoryForDisplay(trackingNumber)
            manager.close()
            val arrayAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, historyList)
            val statusList = uspsDetailView.findViewById<ListView>(R.id.history)
            statusList.adapter = arrayAdapter
        }
        return uspsDetailView
    }
}