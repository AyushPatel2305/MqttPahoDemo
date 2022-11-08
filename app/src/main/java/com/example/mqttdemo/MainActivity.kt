package com.example.mqttdemo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mqttdemo.databinding.ActivityMainBinding
import info.mqtt.android.service.Ack
import info.mqtt.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*
import java.sql.Timestamp


class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    lateinit var client: MqttAndroidClient

    private val TOPIC = "event"
    private val QOS = 0
    private val SERVER_URL = "tcp://broker.mqttdashboard.com:1883"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        mBinding.lifecycleOwner = this
        setContentView(mBinding.root)

        val clientId = MqttClient.generateClientId()
        client = MqttAndroidClient(this.applicationContext,
            SERVER_URL,
            clientId, Ack.AUTO_ACK)
        conn(mBinding.root)
        checkConnectionStatus()
        setMessageCallback()
    }

    private fun setMessageCallback() {
        client.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable) {
                Log.e("MqttCallback", "Connection Lost")
                checkConnectionStatus()
            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, message: MqttMessage) {
                Log.e("MqttCallback", "Message Arrived")
                val time: String = Timestamp(System.currentTimeMillis()).toString()
                mBinding.tvLastMessage.text =
                    "\nTime:\t$time\nTopic:\t$topic\nMessage:\t${String(message.payload)}\nQoS:\t${message.qos}"
                checkConnectionStatus()
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                Log.e("MqttCallback", "Devivery Complete")
                checkConnectionStatus()
            }
        })

    }

    fun published(v: View?) {
        val topic = TOPIC
        if (mBinding.etMessage.text.isNullOrBlank()) {
            Toast.makeText(this.applicationContext, "No message found", Toast.LENGTH_SHORT).show()
            return
        }
        val message = mBinding.etMessage.text.toString()
        try {
            client.publish(topic, message.toByteArray(), QOS, false)
            Toast.makeText(this, "Published Message", Toast.LENGTH_SHORT).show()
            mBinding.etMessage.setText("")
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun setSubscription() {
        try {
            client.subscribe(TOPIC, QOS)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun conn(v: View?) {
        try {
            val token: IMqttToken = client.connect()
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Toast.makeText(this@MainActivity, "connected!!", Toast.LENGTH_LONG).show()
                    Log.e("MQTT Connection conn", "connected!!")
                    checkConnectionStatus()
                    setSubscription()
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Toast.makeText(this@MainActivity, "connection failed!!", Toast.LENGTH_LONG)
                        .show()
                    Log.e("MQTT Connection conn", "connection failed")
                    checkConnectionStatus()
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconn(v: View?) {
        if (!client.isConnected) return
        try {
            client.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Toast.makeText(this@MainActivity, "Disconnected!!", Toast.LENGTH_LONG).show()
                    Log.e("MQTT Connection", "Disconnected!!")
                    checkConnectionStatus()
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Toast.makeText(this@MainActivity, "Could not diconnect!!", Toast.LENGTH_LONG)
                        .show()
                    Log.e("MQTT Connection", "Couldnot disconnect")
                    checkConnectionStatus()
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun checkConnectionStatus() {
        if (this::client.isInitialized) {
            if (client.isConnected) {
                mBinding.tvStatus.text = "Connected"
            } else {
                mBinding.tvStatus.text = "Disconnected"
            }
        } else {
            mBinding.tvStatus.text = "No Client Found"
        }
    }

}