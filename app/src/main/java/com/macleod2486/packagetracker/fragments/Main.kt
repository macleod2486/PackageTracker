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

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import androidx.fragment.app.Fragment
import com.macleod2486.packagetracker.PackageTrackerApplication
import com.macleod2486.packagetracker.R
import com.macleod2486.packagetrackerusps.USPSManager
import java.util.*

class Main : Fragment() {
    lateinit var main: View
    lateinit var entries: ArrayList<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val tempManager = USPSManager(context, "USPS", null, PackageTrackerApplication.databaseVersion)
        main = inflater.inflate(R.layout.content_main, container, false)
        val add = main.findViewById<Button>(R.id.add)
        add.setOnClickListener { PackageTrackerApplication.navController.navigate(R.id.action_main2_to_USPS2) }
        val manager = USPSManager(context, "USPS", null, PackageTrackerApplication.databaseVersion)
        entries = manager.entries
        manager.close()
        if (entries.size == 0) entries.add("No current entries")
        val entryList = main.findViewById<ListView>(R.id.entries)
        val arrayAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, entries)
        entryList.adapter = arrayAdapter
        entryList.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            if (!entries.contains("No current entries")) {
                val bundle = Bundle()
                bundle.putString("trackingnumber", entries.get(position))
                PackageTrackerApplication.navController.navigate(R.id.action_main2_to_USPSDetail, bundle)
            }
        }
        entryList.onItemLongClickListener = OnItemLongClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            if (!entries.contains("No current entries")) {
                val alertBuilder = AlertDialog.Builder(activity)
                alertBuilder.setMessage("Delete ${entries.get(position)} ?")
                alertBuilder.setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                    tempManager.deleteEntryAndHistory(entries[position])
                    val tempEntries = tempManager.entries
                    if (tempEntries.size == 0) tempEntries.add("No current entries")
                    entries = tempEntries
                    tempManager.close()
                    val tempAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, tempEntries)
                    entryList.adapter = tempAdapter
                    tempManager.close()
                }
                alertBuilder.setNeutralButton("Edit") { _: DialogInterface?, _: Int ->
                    val inputView = EditText(context)
                    inputView.inputType = InputType.TYPE_CLASS_TEXT

                    val editAlertBuilder = AlertDialog.Builder(activity)
                    editAlertBuilder.setView(inputView)
                    editAlertBuilder.setTitle("Nickname for tracking number")
                    editAlertBuilder.setPositiveButton("Ok") {_: DialogInterface?, _: Int ->
                        tempManager.addNick(nick = inputView.text.toString(), trackingNumber = entries[position])

                    }
                    editAlertBuilder.setNegativeButton("Cancel") {_: DialogInterface?, _: Int ->

                    }
                    val editDialog = editAlertBuilder.create()
                    editDialog.show()
                }
                alertBuilder.setNegativeButton("No") { _: DialogInterface?, _: Int -> }
                val dialog = alertBuilder.create()
                dialog.show()
            }
            true
        }
        return main
    }
}