package com.example.desi.babycare

import android.app.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat

class BabyCare : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.baby_care_main)
        val ip = intent.getStringExtra("ip")
        conf = Configuration(ip)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?

        client =
                mqttClient.getMqttClient(applicationContext, conf!!.MQTT_BROKER_URL, conf!!.CLIENT_ID) {
                    mqttClient.subscribe(client!!, conf!!.HEALTH_TOPIC, 0)
                    mqttClient.subscribe(client!!, conf!!.SEAT_TOPIC, 1)
                    try {
                        locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
                    } catch (ex: SecurityException) {
                        Log.d(TAG, "Security Exception, no location available")
                    }
                }

        client!!.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {}
            override fun connectionLost(throwable: Throwable) {}

            @Throws(Exception::class)
            override fun messageArrived(s: String, mqttMessage: MqttMessage) {
                val message = JSONObject(String(mqttMessage.payload))
                val temperature: TextView = findViewById(R.id.temperature)
                val beats: TextView = findViewById(R.id.beats)
                val healthWarning: TextView = findViewById(R.id.healthCondition)
                val warningText: TextView = findViewById(R.id.warning)
                when (s) {
                    conf!!.HEALTH_TOPIC -> {
                        temperature.text = message.getInt("temp").toString()
                        beats.text = message.getInt("heart").toString()
                        healthWarning.text =
                                if (message.getBoolean("warning")) "Critical condition detected" else "acceptable life parameters"
                    }
                    conf!!.SEAT_TOPIC -> {
                        if (message.getBoolean("warning")) {
                            warningText.text = "Child left in the car"
                            sendNotification("BabyCare", "Child left in the car")
                            try {
                                val position = JSONObject()
                                position.put("longitude", loc!!.longitude)
                                position.put("latitude", loc!!.latitude)
                                mqttClient.publishMessage(client!!, position.toString(), 0, conf!!.POSITION_TOPIC)
                            } catch (e: MqttException) {
                                e.printStackTrace()
                            } catch (e: UnsupportedEncodingException) {
                                e.printStackTrace()
                            }
                        } else
                            warningText.text = ""

                    }
                }
            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {}
        })
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            loc = location
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun sendNotification(title: String, msg: String) {
        val mBuilder = NotificationCompat.Builder(applicationContext, "notify_001")
        val ii = Intent(this.applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, ii, 0)
        val bigText = NotificationCompat.BigTextStyle()
        bigText.setSummaryText("Warning message")
        mBuilder.setContentIntent(pendingIntent)
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
        mBuilder.setContentTitle(title)
        mBuilder.setContentText(msg)
        mBuilder.priority = Notification.PRIORITY_MAX
        mBuilder.setStyle(bigText)
        mNotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "0"
            val channel = NotificationChannel(
                channelId,
                "BabyCare",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            mNotificationManager!!.createNotificationChannel(channel)
            mBuilder.setChannelId(channelId)
        }
        mNotificationManager!!.notify(0, mBuilder.build())
    }

    private var conf: Configuration? = null
    private val mqttClient = PahoMqttClient()
    private var locationManager: LocationManager? = null
    private var client: MqttAndroidClient? = null
    private var mNotificationManager: NotificationManager? = null
    private var loc: Location? = null
    companion object {

        private val TAG = "BabyCare"
    }
}