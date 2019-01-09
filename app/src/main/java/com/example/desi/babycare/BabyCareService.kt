package com.example.desi.babycare

import android.app.Service
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.os.IBinder
import android.util.Log
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.android.service.MqttAndroidClient

class BabyCareService(conf: Configuration) : Service() {
    private val TAG = "MqttMessageService"
    private var pahoMqttClient: PahoMqttClient? = null
    private var mqttAndroidClient: MqttAndroidClient? = null
    private val configuration = conf
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        pahoMqttClient = PahoMqttClient()
        mqttAndroidClient =
                pahoMqttClient!!.getMqttClient(applicationContext, configuration.MQTT_BROKER_URL, configuration.CLIENT_ID)

        mqttAndroidClient!!.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {

            }

            override fun connectionLost(throwable: Throwable) {

            }

            @Throws(Exception::class)
            override fun messageArrived(s: String, mqttMessage: MqttMessage) {
                setMessageNotification(s, String(mqttMessage.payload))
            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {

            }
        })
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    private fun setMessageNotification(topic: String, msg: String) {
        val mBuilder = NotificationCompat.Builder(this)
            .setContentTitle(topic)
            .setContentText(msg)
        val resultIntent = Intent(this, MainActivity::class.java)

        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.setContentIntent(resultPendingIntent)
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(100, mBuilder.build())
    }
}