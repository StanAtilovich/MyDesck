package ru.stan.mydesck.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.stan.mydesck.R
import ru.stan.mydesck.utils.CountryHelper


class DialogSpinnerHelper {
    fun showSpinnerDialog(context: Context, list: ArrayList<String>, tvSelection: TextView) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        val rootView = LayoutInflater.from(context).inflate(R.layout.spinner_layout, null)
        val adapter = RcViewDialogSpinnerAdapter(tvSelection, dialog)
        val rcView = rootView.findViewById<RecyclerView>(R.id.rc_SpView)
        val sv = rootView.findViewById<SearchView>(R.id.sv_spinner)
        rcView.layoutManager = LinearLayoutManager(context)
        rcView.adapter = adapter
        dialog.setView(rootView)
        adapter.updateAdapter(list)
        setSearchView(adapter, list, sv)
        dialog.show()
    }

    private fun setSearchView(
        adapter: RcViewDialogSpinnerAdapter,
        list: ArrayList<String>,
        sv: SearchView
    ) {
        sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val tempList = CountryHelper.filterListData(list, newText)
                adapter.updateAdapter(tempList)
                return true
            }

        })
    }
}