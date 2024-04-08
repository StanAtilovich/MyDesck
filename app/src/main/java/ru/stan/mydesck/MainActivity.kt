package ru.stan.mydesck

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import ru.stan.mydesck.act.EditAdsActivity
import ru.stan.mydesck.databinding.ActivityMainBinding
import ru.stan.mydesck.dialogHelper.DialogConst
import ru.stan.mydesck.dialogHelper.DialogHelper
import ru.stan.mydesck.dialogHelper.GoogleAcCounst


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var tvAccount: TextView
    private lateinit var binding: ActivityMainBinding
    private val dialogHelper = DialogHelper(this)
    val mAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mine_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GoogleAcCounst.GOOGLE_SING_IN_REQUEST_CODE) {

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    dialogHelper.accHelper.singInFirebaseWithGoogle(account.idToken!!)
                }
            } catch (e: ApiException) {
                Log.d("MyLog", "Api error : ${e.message}")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(mAuth.currentUser)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.id_new_add) {
            val i = Intent(this, EditAdsActivity::class.java)
            startActivity(i)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun init() {
        setSupportActionBar(binding.mainContent.toolbar)
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.mainContent.toolbar, R.string.open, R.string.close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.tvAccountEmail)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.id_my_ads -> {
                Toast.makeText(this, "id_my_ads", Toast.LENGTH_LONG).show()
            }

            R.id.id_car -> {
                Toast.makeText(this, "id_car", Toast.LENGTH_LONG).show()
            }

            R.id.id_pc -> {
                Toast.makeText(this, "id_pc", Toast.LENGTH_LONG).show()
            }

            R.id.id_smartphones -> {
                Toast.makeText(this, "id_smartphones", Toast.LENGTH_LONG).show()
            }

            R.id.id_dm -> {
                Toast.makeText(this, "id_dm ", Toast.LENGTH_LONG).show()
            }

            R.id.id_sing_up -> {
                dialogHelper.createSingDialog(DialogConst.SING_UP_STATE)
            }

            R.id.id_sing_in -> {
                dialogHelper.createSingDialog(DialogConst.SING_IN_STATE)
            }

            R.id.is_sing_out -> {
                uiUpdate(null)
                mAuth.signOut()
                dialogHelper.accHelper.singOutG()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun uiUpdate(user: FirebaseUser?) {
        tvAccount.text = if (user == null) {
            resources.getString(R.string.not_reg)
        } else {
            user.email
        }
    }

}