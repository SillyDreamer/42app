package com.example.a42app

import android.accounts.AccountManager
import android.accounts.AccountManagerCallback
import android.accounts.AccountManagerFuture
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL






class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MyAsync().execute()

        val am = AccountManager.get(this)
        val options = Bundle()

//       var token = am.getAuthToken(
//            am.getAccountsByType(packageName)[0], // Account retrieved using getAccountsByType()
//            "Manage your tasks", // Auth scope
//            options, // Authenticator-specific options
//            this, // Your activity
//            OnTokenAcquired(), // Callback called when a token is successfully acquired
//            Handler()
//        )    // Callback called if an error occurs
//        options.putString(AccountManager.KEY_AUTHTOKEN, token.toString())
    }



    private inner class OnTokenAcquired : AccountManagerCallback<Bundle> {

        override fun run(result: AccountManagerFuture<Bundle>) {
            // Get the result of the operation from the AccountManagerFuture.
            val bundle = result.result

            // The token is a named value in the bundle. The name of the value
            // is stored in the constant AccountManager.KEY_AUTHTOKEN.
            var token = bundle.getString(AccountManager.KEY_AUTHTOKEN)
            val launch: Intent? = result.getResult().get(AccountManager.KEY_INTENT) as? Intent
            if (launch != null) {
                startActivityForResult(launch, 0)
            }
        }
    }

    inner class MyAsync : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params: String?): String {
            val url = URL("https://api.intra.42.fr/oauth/")
            val conn = url.openConnection() as HttpURLConnection
            conn.apply {
               // requestMethod = "POST"
                addRequestProperty("grant_type", "client_credentials")
                addRequestProperty("client_id", "38a3ee0913d81f6b1a77a6a293be7903f2e1ee38b030299915d968bf5edacd8c")
                addRequestProperty("client_secret", "7d87bbf68d1b627a7509ffca2be4394f6e0e2bbd27771bf2bdb07d95c495f6cc")
                //setRequestProperty("Authorization", "a2404e41014d8ab5648c3fcc6c8ad71e77d703a3d2fd47eeb3097ca730fe66b2")

                println("code == $responseCode")
                BufferedReader(InputStreamReader(inputStream)).use {
                    var content = ""
                    val response = StringBuffer()

                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }
                    it.close()
                    content = response.toString()
                    println("content = $content")
                }
            }
            return ""
        }
    }
}
