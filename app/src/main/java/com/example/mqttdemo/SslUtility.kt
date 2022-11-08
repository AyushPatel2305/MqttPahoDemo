package com.example.mqttdemo

import android.content.Context
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

class SslUtility(val context: Context) {

    companion object {

        private var mInstance: SslUtility? = null
        private val mContext: Context? = null
        private val mSocketFactoryMap: HashMap<Int, SSLSocketFactory> = HashMap()

        fun getInstance(context: Context): SslUtility {
            if (mInstance == null) {
                mInstance = SslUtility(context);
            }
            return mInstance!!
        }

    }

    fun getSocketFactory(certificateId: Int, certificatePassword: String): SSLSocketFactory? {
        var result = mSocketFactoryMap[certificateId]
        if (null == result && null != mContext) {
            try {
                val keystoreTrust: KeyStore = KeyStore.getInstance("BKS")
                keystoreTrust.load(mContext.resources.openRawResource(certificateId),
                    certificatePassword.toCharArray())
                val trustManagerFactory: TrustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory
                        .getDefaultAlgorithm())
                trustManagerFactory.init(keystoreTrust)
                val sslContext: SSLContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustManagerFactory.getTrustManagers(),
                    SecureRandom())
                result = sslContext.socketFactory
                mSocketFactoryMap[certificateId] = result
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return result
    }

}