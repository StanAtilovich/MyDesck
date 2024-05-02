package ru.stan.mydesck.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.stan.mydesck.model.Ad
import ru.stan.mydesck.model.DbManager

class FirebaseViewModel : ViewModel() {
    private val dbManager = DbManager()
    val liveAdsData = MutableLiveData<ArrayList<Ad>?>()
    fun loadAllAdsFirstPage(filter: String) {
        dbManager.getAllAdsFirstPage(filter, object : DbManager.ReadDataCallBack {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadAllAdsNextPage(time: String, filter: String) {
        dbManager.getAllAdsNextPage(time,filter, object : DbManager.ReadDataCallBack {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }
    fun loadAllAdsFromCat(cat: String, filter: String) {
        dbManager.getAllAdsFromCatFirstPage(cat,filter,object : DbManager.ReadDataCallBack {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadAllAdsFromCatNextPage(cat: String,time: String, filter: String) {
        dbManager.getAllAdsFromCatNextPage(cat,time,filter,object : DbManager.ReadDataCallBack {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun adViewed(ad: Ad) {
        dbManager.adViewed(ad)
    }

    fun onFavClick(ad: Ad) {
        dbManager.onFavClick(ad, object : DbManager.FinishWorkListener {
            override fun onFinish(isDone: Boolean) {
                val updateList = liveAdsData.value
                val pos = updateList?.indexOf(ad)
                if (pos != -1) {
                    pos?.let {
                        val favCounter =
                            if (ad.isFav) ad.favCounter.toInt() - 1 else ad.favCounter.toInt() + 1
                        updateList[pos] = updateList[pos].copy(isFav = !ad.isFav, favCounter = favCounter.toString())
                    }
                }
                liveAdsData.postValue(updateList)
            }

        })
    }

    fun loadMyAds() {
        dbManager.getMyAds(object : DbManager.ReadDataCallBack {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }

        })
    }

    fun loadMyFavs() {
        dbManager.getFavs(object : DbManager.ReadDataCallBack {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }

        })
    }

    fun deleteItem(ad: Ad) {
        dbManager.deleteAd(ad, object : DbManager.FinishWorkListener {
            override fun onFinish(isDone: Boolean) {
                val updatedList = liveAdsData.value
                updatedList?.remove(ad)
                liveAdsData.postValue(updatedList)
            }

        })
    }
}