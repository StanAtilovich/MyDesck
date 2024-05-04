package ru.stan.mydesck

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import ru.stan.mydesck.accountHelper.AccountHelper
import ru.stan.mydesck.act.DescriptionActivity
import ru.stan.mydesck.act.EditAdsActivity
import ru.stan.mydesck.act.FilterActivity
import ru.stan.mydesck.act.showToast
import ru.stan.mydesck.adapters.AdsRcAdapter
import ru.stan.mydesck.databinding.ActivityMainBinding
import ru.stan.mydesck.dialogHelper.DialogConst
import ru.stan.mydesck.dialogHelper.DialogHelper
import ru.stan.mydesck.model.Ad
import ru.stan.mydesck.utils.AppMainState
import ru.stan.mydesck.utils.BillingManager
import ru.stan.mydesck.utils.FilterManager
import ru.stan.mydesck.viewModel.FirebaseViewModel


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    AdsRcAdapter.Listener {
    private lateinit var tvAccount: TextView
    private lateinit var imAccount: ImageView
    private var curentCategory: String? = null
    private var filter: String = "empty"
    private var clearUpdate: Boolean = true
    private lateinit var binding: ActivityMainBinding
    private val dialogHelper = DialogHelper(this)
    val mAuth = Firebase.auth
    lateinit var googleSingInLauncher: ActivityResultLauncher<Intent>
    private lateinit var filterLauncher: ActivityResultLauncher<Intent>
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    val adapter = AdsRcAdapter(this)
    private var filterDb: String = ""
    private var pref: SharedPreferences? = null
    private var isPremium = false
    private var bManager: BillingManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pref = getSharedPreferences(BillingManager.MAIN_PREF, MODE_PRIVATE)
        isPremium = pref?.getBoolean(BillingManager.REMOVE_ADS_PREF, false)!!
        isPremium = true
        if (isPremium) {
            (application as AppMainState).showAdIfAvailable(this) {
            }
            initAds()
        } else {
            binding.mainContent.adView2.visibility = View.GONE
        }

        init()
        initRecyclerView()
        initViewModel()
        bottomMenuClick()
        scrollListener()
        onActivityResultFilter()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mine_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.id_filter) {
            val i = Intent(this@MainActivity, FilterActivity::class.java).apply {
                putExtra(FilterActivity.FILTER_KEY, filter)
            }
            filterLauncher.launch(i)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onActivityResult() {
        googleSingInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        dialogHelper.accHelper.singInFirebaseWithGoogle(account.idToken!!)
                    }
                } catch (e: ApiException) {
                    Log.d("MyLog", "Api error : ${e.message}")
                }
            }
    }

    private fun onActivityResultFilter() {
        filterLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    filter = it.data?.getStringExtra(FilterActivity.FILTER_KEY)!!
                    filterDb = FilterManager.getFilter(filter)
                } else if (it.resultCode == RESULT_CANCELED) {
                    filterDb = ""
                    filter = "empty"
                }
            }
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(mAuth.currentUser)
    }

    override fun onResume() {
        super.onResume()
        binding.mainContent.bNavView.selectedItemId = R.id.id_home
        binding.mainContent.adView2.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.mainContent.adView2.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mainContent.adView2.destroy()
        bManager?.closeConnection()
    }

    private fun initAds() {
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        binding.mainContent.adView2.loadAd(adRequest)
    }


    private fun bottomMenuClick() = with(binding) {
        mainContent.bNavView.setOnItemSelectedListener { item ->
            clearUpdate = true
            when (item.itemId) {
                R.id.id_new_ad -> {
                    if (mAuth.currentUser != null) {
                        if (!mAuth.currentUser?.isAnonymous!!) {
                            val i = Intent(this@MainActivity, EditAdsActivity::class.java)
                            startActivity(i)
                        } else {
                            showToast("Гость не может публиковать обьявления!")
                        }
                    } else {
                        showToast("Ошибка регистрации")
                    }
                }

                R.id.id_my_ads -> {
                    firebaseViewModel.loadMyAds()
                    mainContent.toolbar.title = getString(R.string.ad_me_ads)
                }

                R.id.id_fav -> {
                    firebaseViewModel.loadMyFavs()
                }

                R.id.id_home -> {
                    curentCategory = getString(R.string.fev)
                    firebaseViewModel.loadAllAdsFirstPage(filterDb)
                    mainContent.toolbar.title = getString(R.string.fev)
                }
            }
            true
        }

    }

    private fun initViewModel() {
        firebaseViewModel.liveAdsData.observe(this) {
            val list = it?.let { it1 -> getAdsByCategory(it1) }
            if (!clearUpdate) {
                if (list != null) {
                    adapter.updateAdapter(list)
                }
            } else {
                if (list != null) {
                    adapter.updateAdapterWithClear(list)
                }
            }
            binding.mainContent.tvEmpty.visibility =
                if (adapter.itemCount == 0) View.VISIBLE else View.GONE
        }
    }

    private fun getAdsByCategory(list: ArrayList<Ad>): ArrayList<Ad> {
        val tempList = ArrayList<Ad>()
        tempList.addAll(list)
        if (curentCategory != getString(R.string.fev)) {
            tempList.clear()
            list.forEach {
                if (curentCategory == it.category) tempList.add(it)
            }
        }
        tempList.reverse()
        return tempList
    }

    private fun init() {
        curentCategory = getString(R.string.fev)
        setSupportActionBar(binding.mainContent.toolbar)
        onActivityResult()
        navViewSettings()
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.mainContent.toolbar, R.string.open, R.string.close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.tvAccountEmail)
        imAccount = binding.navView.getHeaderView(0).findViewById(R.id.ivAccount)
    }

    private fun initRecyclerView() {
        binding.apply {
            mainContent.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            mainContent.rcView.adapter = adapter
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        clearUpdate = true
        when (item.itemId) {
            R.id.id_my_ads -> {
                Toast.makeText(this, "id_my_ads", Toast.LENGTH_LONG).show()
            }

            R.id.id_car -> {
                getAdsFromCat(getString(R.string.ad_car))
            }

            R.id.id_pc -> {
                getAdsFromCat(getString(R.string.ad_pc))
            }

            R.id.id_smartphones -> {
                getAdsFromCat(getString(R.string.ad_smartphones))
            }

            R.id.id_remove_ads -> {
                bManager = BillingManager(this)
                bManager?.startConnection()
            }

            R.id.id_dm -> {
                getAdsFromCat(getString(R.string.ad_dm))
            }

            R.id.id_sing_up -> {
                dialogHelper.createSingDialog(DialogConst.SING_UP_STATE)
            }

            R.id.id_sing_in -> {
                dialogHelper.createSingDialog(DialogConst.SING_IN_STATE)
            }

            R.id.is_sing_out -> {
                if (mAuth.currentUser?.isAnonymous == true) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return true
                }
                uiUpdate(null)
                mAuth.signOut()
                dialogHelper.accHelper.singOutG()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun getAdsFromCat(cat: String) {
        curentCategory = cat
        firebaseViewModel.loadAllAdsFromCat(cat, filterDb)
    }

    fun uiUpdate(user: FirebaseUser?) {

        if (user == null) {
            dialogHelper.accHelper.singInAnonymysle(object : AccountHelper.Listener {
                override fun onComplete() {
                    tvAccount.setText(R.string.guest)
                    imAccount.setImageResource(R.drawable.ic_account)
                }
            })
        } else if (user.isAnonymous) {
            tvAccount.setText(R.string.guest)
            imAccount.setImageResource(R.drawable.ic_account)
        } else if (!user.isAnonymous) {
            tvAccount.text = user.email
            Picasso.get().load(user.photoUrl).into(imAccount)
        }
    }


    override fun onDeleteItem(ad: Ad) {
        firebaseViewModel.deleteItem(ad)
    }

    override fun onAdView(ad: Ad) {
        firebaseViewModel.adViewed(ad)
        val i = Intent(binding.root.context, DescriptionActivity::class.java)
        i.putExtra(DescriptionActivity.AD_NODE, ad)
        startActivity(i)
    }

    override fun onFavClicked(ad: Ad) {
        firebaseViewModel.onFavClick(ad)
    }

    private fun navViewSettings() = with(binding) {
        val menu = navView.menu
        val adsCat = menu.findItem(R.id.adsCat)
        val spanAdsCat = SpannableString(adsCat.title)
        spanAdsCat.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    this@MainActivity,
                    R.color.red
                )
            ), 0, adsCat.title!!.length, 0
        )
        adsCat.title = spanAdsCat

        val acCat = menu.findItem(R.id.acCat)
        val spanAcCat = SpannableString(acCat.title)
        spanAcCat.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    this@MainActivity,
                    R.color.red
                )
            ), 0, acCat.title!!.length, 0
        )
        acCat.title = spanAcCat
    }

    private fun scrollListener() = with(binding.mainContent) {
        rcView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rcView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(rcView, newState)
                if (!rcView.canScrollVertically(SCROOL_DONE)
                    && newState == RecyclerView.SCROLL_STATE_IDLE
                ) {
                    clearUpdate = false
                    val adsList = firebaseViewModel.liveAdsData.value!!
                    if (adsList.isNotEmpty()) {
                        getAdsFromCat(adsList)
                    }
                }
            }
        })
    }

    private fun getAdsFromCat(adsList: ArrayList<Ad>) {
        adsList[0].let {
            if (curentCategory == getString(R.string.fev)) {
                firebaseViewModel.loadAllAdsNextPage(it.time, filterDb)
            } else {
                firebaseViewModel.loadAllAdsFromCatNextPage(it.category!!, it.time, filterDb)
            }
        }
    }

    companion object {
        const val EDIT_STATE = "EDIT_STATE"
        const val ADS_DATA = "ADS_DATA"
        const val SCROOL_DONE = 1

    }
}