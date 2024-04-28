package ru.stan.mydesck.accountHelper

import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import ru.stan.mydesck.MainActivity
import ru.stan.mydesck.R
import ru.stan.mydesck.constants.FireAuthConstance
import ru.stan.mydesck.dialogHelper.GoogleAcCounst
import java.lang.Exception


class AccountHelper(act: MainActivity) {
    private val act = act
    private lateinit var singInClient: GoogleSignInClient
    fun singUpWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    act.mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                singUpWithEmailSuccessful(task.result.user!!)
                            } else {
                                singUpWithEmailException(task.exception!!, email, password)
                            }
                        }
                }
            }

        }
    }

    private fun singUpWithEmailSuccessful(user: FirebaseUser) {
        sendEmailVerification(user)
        act.uiUpdate(user)
    }

    private fun singUpWithEmailException(exception: Exception, email: String, password: String) {
        Toast.makeText(
            act,
            act.resources.getString(R.string.sing_in_error),
            Toast.LENGTH_LONG
        ).show()

        if (exception is FirebaseAuthUserCollisionException) {
            if (exception.errorCode == FireAuthConstance.ERROR_EMAIL_ALREADY_IN_USE) {
                linkEmailToG(email, password)
            }
        } else if (exception is FirebaseAuthInvalidCredentialsException) {

            if (exception.errorCode == FireAuthConstance.ERROR_INVALID_EMAIL) {
            }
            if (exception is FirebaseAuthWeakPasswordException) {
                val exception =
                    exception as FirebaseAuthInvalidCredentialsException

                if (exception.errorCode == FireAuthConstance.ERROR_WEAK_PASSWORD) {
                }
            }
        }
    }

    private fun linkEmailToG(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        if (act.mAuth.currentUser != null) {
            act.mAuth.currentUser?.linkWithCredential(credential)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        act,
                        act.resources.getString(R.string.link_done),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        } else {
            Toast.makeText(act, act.resources.getString(R.string.not), Toast.LENGTH_LONG).show()
        }
    }

    fun singInWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful)
                    act.mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                act.uiUpdate(task.result.user)
                            } else {
                                singInWithEmailException(task.exception!!, email, password)
                            }
                        }
            }
        }
    }

    private fun singInWithEmailException(exception: Exception, email: String, password: String) {
        if (exception is FirebaseAuthInvalidCredentialsException) {
            if (exception.errorCode == FireAuthConstance.ERROR_INVALID_EMAIL) {
                Toast.makeText(
                    act,
                    FireAuthConstance.ERROR_INVALID_EMAIL,
                    Toast.LENGTH_LONG
                ).show()
            } else if (exception.errorCode == FireAuthConstance.ERROR_WRONG_PASSWORD) {
                Toast.makeText(
                    act,
                    FireAuthConstance.ERROR_WRONG_PASSWORD,
                    Toast.LENGTH_LONG
                ).show()
            }
        } else if (exception is FirebaseAuthInvalidUserException) {
            if (exception.errorCode == FireAuthConstance.ERROR_USER_NOT_FOUND) {
                Toast.makeText(
                    act,
                    FireAuthConstance.ERROR_USER_NOT_FOUND,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun getSingInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(act.getString(R.string.default_web_client_id)).requestEmail().build()
        return GoogleSignIn.getClient(act, gso)
    }

    fun singInWithGoogle() {
        singInClient = getSingInClient()
        val intent = singInClient.signInIntent
        act.googleSingInLauncher.launch(intent)
    }

    fun singOutG() {
        getSingInClient().signOut()
    }

    fun singInFirebaseWithGoogle(token: String) {
        val credential = GoogleAuthProvider.getCredential(token, null)
        act.mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                act.mAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(act, "sing in done", Toast.LENGTH_LONG).show()
                        act.uiUpdate(task.result.user)
                    } else {
                        Log.d("MyLog", "singInFirebaseWithGoogle EXCEPTION: ${task.exception} ")
                    }
                }
            }
        }

    }

    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    act,
                    act.resources.getString(R.string.send_verification_done),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    act,
                    act.resources.getString(R.string.send_verification_email_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun singInAnonymysle(listener: Listener) {
        act.mAuth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                listener.onComplete()
                Toast.makeText(act, "Вы вошли как гость", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(act, "Вам не удалось войти как Гость", Toast.LENGTH_LONG).show()
            }
        }
    }

    interface Listener {
        fun onComplete()
    }
}