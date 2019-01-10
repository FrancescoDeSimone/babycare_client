package com.example.desi.babycare

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
import java.io.UnsupportedEncodingException;
import java.util.Timer
import kotlin.concurrent.schedule
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.support.annotation.NonNull




class BabyCare : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.baby_care_main)

        val ip = intent.getStringExtra("ip")
        conf = Configuration(ip)
        client = mqttClient.getMqttClient(getApplicationContext(), conf!!.MQTT_BROKER_URL, conf!!.CLIENT_ID)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?

        try {
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
        } catch (ex: SecurityException) {
            Log.d(TAG, "Security Exception, no location available")
        }

        //find bug in library?????
        Timer("SettingUp", false).schedule(1000) {
            mqttClient.subscribe(client!!, conf!!.HEALTH_TOPIC, 0)
            mqttClient.subscribe(client!!, conf!!.SEAT_TOPIC, 1)
        }
        client!!.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {

            }

            override fun connectionLost(throwable: Throwable) {

            }

            @Throws(Exception::class)
            override fun messageArrived(s: String, mqttMessage: MqttMessage) {
                var message = JSONObject(String(mqttMessage.payload))
                val temperature: TextView = findViewById(R.id.temperature) as TextView
                val beats: TextView = findViewById(R.id.beats) as TextView
                val healthWarning: TextView = findViewById(R.id.healthCondition) as TextView
                val warningText: TextView = findViewById(R.id.warning) as TextView
                when (s) {
                    conf!!.HEALTH_TOPIC -> {
                        temperature.text = message.getInt("temp").toString()
                        beats.text = message.getInt("hearth").toString()
                        healthWarning.text =
                                if (message.getBoolean("warning")) "Critical condition detected" else "acceptable life parameters"
                    }
                    conf!!.SEAT_TOPIC -> {
                        warningText.text = if (message.getBoolean("warning")) "Child left in the car" else ""
                        sendNotification("BabyCare","Child left in the car")
                    }
                }
            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {

            }
        })
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            try {
                val position = JSONObject()
                position.put("longitude", location.longitude)
                position.put("latitude", location.latitude)
                mqttClient.publishMessage(client!!, position.toString(), 0, conf!!.POSITION_TOPIC)
            } catch (e: MqttException) {
                e.printStackTrace()
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun sendNotification(title: String, msg: String) {
        val mBuilder = NotificationCompat.Builder(this)
            .setContentTitle(title)
            .setContentText(msg)
        val resultIntent = Intent(this, BabyCare::class.java)

        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(BabyCare::class.java)
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.setContentIntent(resultPendingIntent)
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(100, mBuilder.build())
    }

    private var conf: Configuration? = null
    private val mqttClient = PahoMqttClient()
    private var locationManager: LocationManager? = null
    private var client: MqttAndroidClient? = null


    companion object {

        private val TAG = "BabyCare"
    }
}