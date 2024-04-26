package ru.stan.mydesck.dialogHelper

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import ru.stan.mydesck.MainActivity
import ru.stan.mydesck.R
import ru.stan.mydesck.accountHelper.AccountHelper
import ru.stan.mydesck.databinding.SingDialogBinding


class DialogHelper(val act: MainActivity) {
    val accHelper = AccountHelper(act)

    fun createSingDialog(index: Int) {
        val builder = AlertDialog.Builder(act)
        val binding = SingDialogBinding.inflate(act.layoutInflater)
        builder.setView(binding.root)
        setDialogState(index, binding)
        val dialog = builder.create()
        binding.btSingUpIn.setOnClickListener {
            setOnClickSingUpIn(index, binding, dialog)
        }
        binding.btForgetPasword.setOnClickListener {
            setOnClickResetPassword(binding, dialog)
        }

        binding.btGoogleSingIn.setOnClickListener {
            accHelper.singInWithGoogle()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setOnClickResetPassword(binding: SingDialogBinding, dialog: AlertDialog?) {
        if (binding.edSingInEmail.text.isNotEmpty()) {
            act.mAuth.sendPasswordResetEmail(binding.edSingInEmail.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            act, R.string.email_reset_password_was_sent, Toast.LENGTH_LONG
                        ).show()
                    }
                }
            dialog?.dismiss()
        } else {
            binding.tvDialogMessage.visibility = View.VISIBLE
        }
    }

    private fun setOnClickSingUpIn(index: Int, binding: SingDialogBinding, dialog: AlertDialog?) {
        dialog?.dismiss()
        if (index == DialogConst.SING_UP_STATE) {
            accHelper.singUpWithEmail(
                binding.edSingInEmail.text.toString(), binding.edSingInPasword.text.toString()
            )
        } else {
            accHelper.singInWithEmail(
                binding.edSingInEmail.text.toString(), binding.edSingInPasword.text.toString()
            )
        }
    }

    private fun setDialogState(index: Int, binding: SingDialogBinding) {
        if (index == DialogConst.SING_UP_STATE) {
            binding.btSingUpIn.text = act.resources.getString(R.string.ad_sing_in_action)
            binding.tvSingTitle.text = act.resources.getString(R.string.ad_sing_up)
        } else {
            binding.btSingUpIn.text = act.resources.getString(R.string.ad_sing_up_action)
            binding.tvSingTitle.text = act.resources.getString(R.string.ad_sing_in)
            binding.btForgetPasword.visibility = View.VISIBLE
        }
    }


}