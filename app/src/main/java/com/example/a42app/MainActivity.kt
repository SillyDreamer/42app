package com.example.a42app

import android.accounts.AccountManager
import android.accounts.AccountManagerCallback
import android.accounts.AccountManagerFuture
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.util.Log
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MyAsync().execute()

//        val am = AccountManager.get(this)
//        val options = Bundle()
//
//       var token = am.getAuthToken(
//            am.getAccountsByType("com.google")[0], // Account retrieved using getAccountsByType()
//            "Manage your tasks", // Auth scope
//            options, // Authenticator-specific options
//            this, // Your activity
//            OnTokenAcquired(), // Callback called when a token is successfully acquired
//            Handler()
//        )    // Callback called if an error occurs
        //options.putString(AccountManager.KEY_AUTHTOKEN, token.toString())
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
            //val url = URL("https://api.intra.42.fr/oauth/authorize")
            val url = URL("https://buy.dev.cart.is/api/product/search?productTag=mars-3")
            val conn = url.openConnection() as HttpsURLConnection
            conn.apply {
                sslSocketFactory = SslUtils.getSslContextForCertificateFile(this@MainActivity, "my_service_certifcate.pem").socketFactory
                requestMethod = "GET"
//                addRequestProperty("grant_type", "client_credentials")
//                addRequestProperty("client_id", "fa41c816827827633513cad1e7514aca2bf933c33a5e20babb279a161e1b1dc8")
//                addRequestProperty("client_secret", "3653e33a21b16ea374713dd16d3d58f064a148713e227edf866e57debd71c780")
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

object SslUtils {

    fun getSslContextForCertificateFile(context: Context, fileName: String): SSLContext {
        try {
            val keyStore = SslUtils.getKeyStore(context, fileName)
            val sslContext = SSLContext.getInstance("SSL")
            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(keyStore)
            sslContext.init(null, trustManagerFactory.trustManagers, SecureRandom())
            return sslContext
        } catch (e: Exception) {
            val msg = "Error during creating SslContext for certificate from assets"
            e.printStackTrace()
            throw RuntimeException(msg)
        }
    }

    private fun getKeyStore(context: Context, fileName: String): KeyStore? {
        var keyStore: KeyStore? = null
        try {
            val assetManager = context.assets
            val cf = CertificateFactory.getInstance("X.509")
            val caInput = assetManager.open(fileName)
            val ca: Certificate
            try {
                ca = cf.generateCertificate(caInput)
                Log.d("SslUtilsAndroid", "ca=" + (ca as X509Certificate).subjectDN)
            } finally {
                caInput.close()
            }

            val keyStoreType = KeyStore.getDefaultType()
            keyStore = KeyStore.getInstance(keyStoreType)
            keyStore!!.load(null, null)
            keyStore.setCertificateEntry("ca", ca)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return keyStore
    }

    fun getTrustAllHostsSSLSocketFactory(): SSLSocketFactory? {
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }

                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }
            })

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            // Create an ssl socket factory with our all-trusting manager

            return sslContext.socketFactory
        } catch (e: KeyManagementException) {
            e.run { printStackTrace() }
            return null
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            return null
        }
    }
}
