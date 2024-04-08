package ru.stan.mydesck.utils

import android.content.Context
import org.json.JSONObject
import ru.stan.mydesck.R
import java.io.IOException
import java.io.InputStream


object CountryHelper {
    fun getAllCountryes(context: Context): ArrayList<String> {
        var tempArray = ArrayList<String>()
        try {
            val inputStreem: InputStream = context.assets.open("countriesToCities.json")
            val size: Int = inputStreem.available()
            val bytesArray = ByteArray(size)
            inputStreem.read(bytesArray)
            val jsonFile = String(bytesArray)
            val JsonObject = JSONObject(jsonFile)
            val countryNames = JsonObject.names()
            if (countryNames != null) {
                for (n in 0 until countryNames.length()) {
                    tempArray.add(countryNames.getString(n))
                }
            }
        } catch (e: IOException) {

        }
        return tempArray
    }

    fun filterListData(list: ArrayList<String>, searchText: String?): ArrayList<String> {
        val tempList = ArrayList<String>()
        tempList.clear()
        for (selection: String in list) {
            if (searchText == null) {
                tempList.add(R.string.noResult.toString())
                return tempList
            }
            if (selection.lowercase().startsWith(searchText.lowercase()))
                tempList.add(selection)
        }
        if (tempList.size == 0) {
            tempList.add(R.string.noResult.toString())
        }
        return tempList
    }
// countriesToCities.json

    fun getAllCities(country: String ,context: Context): ArrayList<String> {
        var tempArray = ArrayList<String>()
        try {
            val inputStreem: InputStream = context.assets.open("countriesToCities.json")
            val size: Int = inputStreem.available()
            val bytesArray = ByteArray(size)
            inputStreem.read(bytesArray)
            val jsonFile = String(bytesArray)
            val JsonObject = JSONObject(jsonFile)
            val cityNames = JsonObject.getJSONArray(country)
                for (n in 0 until cityNames.length()) {
                    tempArray.add(cityNames.getString(n))
                }

        } catch (e: IOException) {

        }
        return tempArray
    }

}