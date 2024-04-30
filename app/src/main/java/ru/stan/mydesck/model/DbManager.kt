package ru.stan.mydesck.model


import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class DbManager {
    val db = Firebase.database.getReference(MAIN_NODE)
    var auth = Firebase.auth
    val dbStorage = Firebase.storage.getReference(MAIN_NODE)

    fun publishAdd(ad: Ad, finishWorkListener: FinishWorkListener) {
        if (auth.uid != null) db.child(ad.key ?: "empty")
            .child(auth.uid!!).child(AD_NODE)
            .setValue(ad)
            .addOnCompleteListener {

                val adFilter = AdFilter(ad.time, "${ad.category}_${ad.time}")
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

    fun getAllAdsFirstPage(readDataCallBack: ReadDataCallBack?) {
        val query = db.orderByChild(ALL_TIME_NODE)
            .limitToLast(
                ADS_LIMIT
            )
        readDataFromDb(query, readDataCallBack)
    }

    fun getAllAdsNextPage(time: String,readDataCallBack: ReadDataCallBack?) {
        val query = db.orderByChild(ALL_TIME_NODE).endBefore(time)
            .limitToLast(
                ADS_LIMIT
            )
        readDataFromDb(query, readDataCallBack)
    }

    fun getAllAdsFromCatFirstPage(cat: String, readDataCallBack: ReadDataCallBack?) {
        val query = db.orderByChild(CAL_TIME_NODE)
            .startAt(cat).endAt(cat + "_\uf8ff").limitToLast(
                ADS_LIMIT
            )
        readDataFromDb(query, readDataCallBack)
    }

    fun getAllAdsFromCatNextPage(catTime: String, readDataCallBack: ReadDataCallBack?) {
        val query = db.orderByChild(CAL_TIME_NODE)
            .endBefore(catTime).limitToLast(
                ADS_LIMIT
            )
        readDataFromDb(query, readDataCallBack)
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
        const val CAL_TIME_NODE = "/adFilter/catTime"
        const val ADS_LIMIT = 2
    }

}
