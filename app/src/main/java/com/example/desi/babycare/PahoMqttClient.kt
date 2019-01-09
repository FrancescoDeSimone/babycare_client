package com.example.desi.babycare

import android.content.Context
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import android.util.Log;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.io.UnsupportedEncodingException;


class PahoMqttClient {
    private val TAG = "PahoMqttClient"
    private val mqttAndroidClient: MqttAndroidClient? = null

    fun getMqttClient(context: Context, brokerUrl: String, clientId: String): MqttAndroidClient? {
        try {
            val token = mqttAndroidClient?.connect(getMqttConnectionOption())
            token?.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    mqttAndroidClient?.setBufferOpts(getDisconnectedBufferOptions())
                    Log.d(TAG, "Success")
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.d(TAG, "Failure " + exception.toString())
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }

        return mqttAndroidClient
    }

    @Throws(MqttException::class)
    fun disconnect(client: MqttAndroidClient) {
        val mqttToken = client.disconnect()
        mqttToken.actionCallback = object : IMqttActionListener {
            override fun onSuccess(iMqttToken: IMqttToken) {
                Log.d(TAG, "Successfully disconnected")
            }

            override fun onFailure(iMqttToken: IMqttToken, throwable: Throwable) {
                Log.d(TAG, "Failed to disconnected " + throwable.toString())
            }
        }
    }

    private fun getDisconnectedBufferOptions(): DisconnectedBufferOptions {
        val disconnectedBufferOptions = DisconnectedBufferOptions()
        disconnectedBufferOptions.isBufferEnabled = true
        disconnectedBufferOptions.bufferSize = 100
        disconnectedBufferOptions.isPersistBuffer = false
        disconnectedBufferOptions.isDeleteOldestMessages = false
        return disconnectedBufferOptions
    }

    private fun getMqttConnectionOption(): MqttConnectOptions {
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isCleanSession = false
        mqttConnectOptions.isAutomaticReconnect = true
        return mqttConnectOptions
    }


    @Throws(MqttException::class, UnsupportedEncodingException::class)
    fun publishMessage(client: MqttAndroidClient, msg: String, qos: Int, topic: String) {
        var encodedPayload = ByteArray(0) //check this
        encodedPayload = msg.toByteArray(charset("UTF-8"))
        val message = MqttMessage(encodedPayload)
        message.id = 320
        message.isRetained = true
        message.qos = qos
        client.publish(topic, message)
    }

    @Throws(MqttException::class)
    fun subscribe(client: MqttAndroidClient, topic: String, qos: Int) {
        val token = client.subscribe(topic, qos)
        token.actionCallback = object : IMqttActionListener {
            override fun onSuccess(iMqttToken: IMqttToken) {
                Log.d(TAG, "Subscribe Successfully $topic")
            }

            override fun onFailure(iMqttToken: IMqttToken, throwable: Throwable) {
                Log.e(TAG, "Subscribe Failed $topic")

            }
        }
    }
}
