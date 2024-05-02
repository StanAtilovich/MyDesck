package ru.stan.mydesck.model


import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import ru.stan.mydesck.utils.FilterManager


class DbManager {
    val db = Firebase.database.getReference(MAIN_NODE)
    var auth = Firebase.auth
    val dbStorage = Firebase.storage.getReference(MAIN_NODE)

    fun publishAdd(ad: Ad, finishWorkListener: FinishWorkListener) {
        if (auth.uid != null) db.child(ad.key ?: "empty")
            .child(auth.uid!!).child(AD_NODE)
            .setValue(ad)
            .addOnCompleteListener {

                val adFilter = FilterManager.createFilter(ad)
                db.child(ad.key ?: "empty").child(FILTER_NODE)
                    .setValue(adFilter)
                    .addOnCompleteListener {
                        finishWorkListener.onFinish()
                    }
            }

    }


    fun onFavClick(ad: Ad, listener: FinishWorkListener) {
        if (ad.isFav) {
            removeToFavs(ad, listener)
        } else {
            addToFavs(ad, listener)
        }
    }

    private fun addToFavs(ad: Ad, listener: FinishWorkListener) {
        ad.key?.let {
            auth.uid?.let { uid ->
                db.child(it)
                    .child(FAVS_NODE).child(uid).setValue(uid).addOnCompleteListener {
                        if (it.isSuccessful) listener.onFinish()
                    }
            }
        }
    }

    private fun removeToFavs(ad: Ad, listener: FinishWorkListener) {
        ad.key?.let {
            auth.uid?.let { uid ->
                db.child(it)
                    .child(FAVS_NODE).child(uid).removeValue().addOnCompleteListener {
                        if (it.isSuccessful) listener.onFinish()
                    }
            }
        }
    }

    fun getMyAds(readDataCallBack: ReadDataCallBack?) {
        val query = db.orderByChild(auth.uid + "/ad/uid").equalTo(auth.uid)
        readDataFromDb(query, readDataCallBack)
    }

    fun getAllAdsFirstPage(filter: String, readDataCallBack: ReadDataCallBack?) {
        val query = if (filter.isEmpty()) {
            db.orderByChild(ALL_TIME_NODE)
                .limitToLast(
                    ADS_LIMIT
                )
        } else {
            getAllAdsByFilterFirstPage(filter)
        }
        readDataFromDb(query, readDataCallBack)
    }

    private fun getAllAdsByFilterFirstPage(tempFilter: String): Query {
        val orderBy = tempFilter.split("|")[0]
        val filter = tempFilter.split("|")[1]
        return db.orderByChild("/adFilter/$orderBy")
            .startAt(filter).endAt(filter + "\uf8ff").limitToLast(
                ADS_LIMIT
            )
    }

    private fun getAllAdsByFilterNextPage(
        tempFilter: String,
        time: String,
        readDataCallBack: ReadDataCallBack?
    ) {
        val orderBy = tempFilter.split("|")[0]
        val filter = tempFilter.split("|")[1]
        val query = db.orderByChild("/adFilter/$orderBy")
            .endBefore(filter + "_$time").limitToLast(
                ADS_LIMIT
            )
        readNextPageFromDb(query, filter, orderBy, readDataCallBack)
    }

    fun getAllAdsNextPage(time: String, filter: String, readDataCallBack: ReadDataCallBack?) {
        if (filter.isEmpty()) {
            val query = db.orderByChild(ALL_TIME_NODE).endBefore(time)
                .limitToLast(
                    ADS_LIMIT
                )
            readDataFromDb(query, readDataCallBack)
        } else {
            getAllAdsByFilterNextPage(filter, time, readDataCallBack)
        }

    }

    fun getAllAdsFromCatFirstPage(
        cat: String,
        filter: String,
        readDataCallBack: ReadDataCallBack?
    ) {
        val query = if (filter.isEmpty()) {
            db.orderByChild(CAL_TIME_NODE)
                .startAt(cat).endAt(cat + "_\uf8ff").limitToLast(
                    ADS_LIMIT
                )
        } else {
            getAllAdsFromCatByFilterFirstPage(cat, filter)
        }
        readDataFromDb(query, readDataCallBack)
    }

    private fun getAllAdsFromCatByFilterFirstPage(cat: String, tempFilter: String): Query {
        val orderBy = "cat_" + tempFilter.split("|")[0]
        val filter = cat + "_" + tempFilter.split("|")[1]
        return db.orderByChild("/adFilter/$orderBy")
            .startAt(filter).endAt(filter + "\uf8ff").limitToLast(
                ADS_LIMIT
            )
    }

    fun getAllAdsFromCatNextPage(
        cat: String,
        time: String,
        filter: String,
        readDataCallBack: ReadDataCallBack?
    ) {
        if (filter.isEmpty()) {
            val query = db.orderByChild(CAL_TIME_NODE)
                .endBefore(cat + "_" + time).limitToLast(
                    ADS_LIMIT
                )
            readDataFromDb(query, readDataCallBack)
        } else {
            getAllAdsFromCatByFilterNextPage(cat, time, filter, readDataCallBack)
        }

    }

    private fun getAllAdsFromCatByFilterNextPage(
        cat: String,
        time: String,
        tempFilter: String,
        readDataCallBack: ReadDataCallBack?
    ) {
        val orderBy = "cat_" + tempFilter.split("|")[0]
        val filter = cat + "_" + tempFilter.split("|")[1]
        val query = db.orderByChild("/adFilter/$orderBy")
            .endBefore(filter + "_" + time).limitToLast(
                ADS_LIMIT
            )
        readNextPageFromDb(query, filter, orderBy, readDataCallBack)
    }


    fun getFavs(readDataCallBack: ReadDataCallBack?) {
        val query = db.orderByChild("/favs/${auth.uid}").equalTo(auth.uid)
        readDataFromDb(query, readDataCallBack)
    }

    private fun readDataFromDb(query: Query, readDataCallBack: ReadDataCallBack?) {
        query.addListenerForSingleValueEvent(

            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val adArray = ArrayList<Ad>()
                    for (item in snapshot.children) {
                        var ad: Ad? = null
                        item.children.forEach {
                            if (ad == null) ad = it.child(AD_NODE).getValue(Ad::class.java)
                        }
                        val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java)

                        val favCounter = item.child(FAVS_NODE).childrenCount
                        val isFav = auth.uid?.let {
                            item.child(FAVS_NODE).child(it).getValue(String::class.java)
                        }
                        ad?.isFav = isFav != null
                        ad?.favCounter = favCounter.toString()

                        ad?.viewCounter = infoItem?.viewCounter ?: "0"
                        ad?.emailCounter = infoItem?.emailsCounter ?: "0"
                        ad?.callsCounter = infoItem?.callsCounter ?: "0"
                        if (ad != null) adArray.add(ad!!)

                    }
                    readDataCallBack?.readData(adArray)
                }

                override fun onCancelled(error: DatabaseError) {
                }

            }
        )
    }

    private fun readNextPageFromDb(
        query: Query,
        filter: String,
        orderBy: String,
        readDataCallBack: ReadDataCallBack?
    ) {
        query.addListenerForSingleValueEvent(

            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val adArray = ArrayList<Ad>()
                    for (item in snapshot.children) {
                        var ad: Ad? = null
                        item.children.forEach {
                            if (ad == null) ad = it.child(AD_NODE).getValue(Ad::class.java)
                        }
                        val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java)
                        val filterNodeValue = item.child(INFO_NODE).child(orderBy).value.toString()
                        Log.d("MyLog", "Filter value:$filterNodeValue ")
                        val favCounter = item.child(FAVS_NODE).childrenCount
                        val isFav = auth.uid?.let {
                            item.child(FAVS_NODE).child(it).getValue(String::class.java)
                        }
                        ad?.isFav = isFav != null
                        ad?.favCounter = favCounter.toString()

                        ad?.viewCounter = infoItem?.viewCounter ?: "0"
                        ad?.emailCounter = infoItem?.emailsCounter ?: "0"
                        ad?.callsCounter = infoItem?.callsCounter ?: "0"
                        if (ad != null && filterNodeValue.startsWith(filter)) adArray.add(ad!!)

                    }
                    readDataCallBack?.readData(adArray)
                }

                override fun onCancelled(error: DatabaseError) {
                }

            }
        )
    }

    fun adViewed(ad: Ad) {
        var counter = ad.viewCounter.toInt()
        counter++
        if (auth.uid != null) db.child(ad.key ?: "empty")
            .child(INFO_NODE)
            .setValue(InfoItem(counter.toString(), ad.emailCounter, ad.callsCounter))

    }

    fun deleteAd(ad: Ad, listener: FinishWorkListener) {
        if (ad.key == null || ad.uid == null) return
        db.child(ad.key).child(ad.uid).removeValue().addOnCompleteListener {
            if (it.isSuccessful) listener.onFinish()
        }
    }

    interface ReadDataCallBack {
        fun readData(list: ArrayList<Ad>)
    }

    interface FinishWorkListener {
        fun onFinish()
    }

    companion object {
        const val AD_NODE = "ad"
        const val FILTER_NODE = "adFilter"
        const val INFO_NODE = "info"
        const val MAIN_NODE = "main"
        const val FAVS_NODE = "favs"
        const val ALL_TIME_NODE = "/adFilter/time"
        const val CAL_TIME_NODE = "/adFilter/cat_time"
        const val ADS_LIMIT = 2
    }

}
