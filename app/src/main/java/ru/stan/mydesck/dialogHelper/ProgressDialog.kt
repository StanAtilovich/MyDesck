package ru.stan.mydesck.dialogHelper

import android.app.Activity
import android.app.AlertDialog
import ru.stan.mydesck.databinding.ProgressDialogBinding

object ProgressDialog {
    fun createProgressDialog(act: Activity): AlertDialog {
        val builder = AlertDialog.Builder(act)
        val binding = ProgressDialogBinding.inflate(act.layoutInflater)
        builder.setView(binding.root)

        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }
}