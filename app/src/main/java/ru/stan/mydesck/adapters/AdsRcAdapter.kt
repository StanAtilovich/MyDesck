package ru.stan.mydesck.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ru.stan.mydesck.MainActivity
import ru.stan.mydesck.R
import ru.stan.mydesck.act.EditAdsActivity
import ru.stan.mydesck.databinding.AdListItemBinding
import ru.stan.mydesck.model.Ad
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdsRcAdapter(val act: MainActivity) : RecyclerView.Adapter<AdsRcAdapter.AdHolder>() {
    private val arrayList = ArrayList<Ad>()
    private var timeFormater: SimpleDateFormat? = null

    init {
        timeFormater = SimpleDateFormat("dd/MM/yyyy - hh:mm:ss", Locale.getDefault())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdHolder {
        val binding = AdListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdHolder(binding, act, timeFormater!!)
    }

    override fun onBindViewHolder(holder: AdHolder, position: Int) {
        holder.setData(arrayList[position])
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: List<Ad>) {
        val tempArray = ArrayList<Ad>()
        tempArray.addAll(arrayList)
        tempArray.addAll(newList)
        val diffUtil = DiffUtil.calculateDiff(DiffUtilHelper(arrayList, tempArray))
        diffUtil.dispatchUpdatesTo(this)
        arrayList.clear()
        arrayList.addAll(tempArray)

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapterWithClear(newList: List<Ad>) {
        val diffUtil = DiffUtil.calculateDiff(DiffUtilHelper(arrayList, newList))
        diffUtil.dispatchUpdatesTo(this)
        arrayList.clear()
        arrayList.addAll(newList)

    }

    class AdHolder(
        private val binding: AdListItemBinding,
        val act: MainActivity,
        private val formater: SimpleDateFormat
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(ad: Ad) = with(binding) {
            tvDiscription.text = ad.description
            tvPrice.text = ad.price
            tvTitle.text = ad.title
            tvViewCounter.text = ad.viewCounter
            tvFavCounter.text = ad.favCounter
            val publishTime = "${act.getString( R.string.timePublish)} ${getTimeFromMills(ad.time)}"
            tvPublishTime.text = publishTime
            Picasso.get().load(ad.mainImage).into(myImageeView)

            isFav(ad)
            showEditPanel(isOwner(ad))
            mainOnClick(ad)

        }

        private fun getTimeFromMills(timeMills: String): String {
            val c = Calendar.getInstance()
            c.timeInMillis = timeMills.toLong()
            return formater.format(c.time)
        }

        private fun isFav(ad: Ad) = with(binding) {
            if (ad.isFav) {
                ibFav.setImageResource(R.drawable.fav_presed)
            } else {
                ibFav.setImageResource(R.drawable.fav_bormal)
            }
        }

        private fun mainOnClick(ad: Ad) = with(binding) {
            imDeleteAd.setOnClickListener {
                act.onDeleteItem(ad)
            }
            itemView.setOnClickListener {
                act.onAdView(ad)
            }
            ibFav.setOnClickListener {
                if (act.mAuth.currentUser?.isAnonymous == false) act.onFavClicked(ad)
            }
            ibEditAd.setOnClickListener(onClickEdit(ad))

        }

        private fun onClickEdit(ad: Ad): OnClickListener {
            return OnClickListener {
                val editIntent = Intent(act, EditAdsActivity::class.java).apply {
                    putExtra(MainActivity.EDIT_STATE, true)
                    putExtra(MainActivity.ADS_DATA, ad)
                }
                act.startActivity(editIntent)
            }

        }

        private fun isOwner(ad: Ad): Boolean {
            return ad.uid == act.mAuth.uid
        }

        private fun showEditPanel(isOwner: Boolean) {
            if (isOwner) {
                binding.editPanel.visibility = View.VISIBLE
            } else {
                binding.editPanel.visibility = View.GONE
            }
        }
    }

    interface Listener {
        fun onDeleteItem(ad: Ad)
        fun onAdView(ad: Ad)
        fun onFavClicked(ad: Ad)
    }

}