package com.example.desi.babycare

import java.io.Serializable

class Configuration(ip: String): Serializable {
        val MQTT_BROKER_URL = "tcp://"+ip+":1883"

        val POSITION_TOPIC = "babycare/position"

        val SEAT_TOPIC = "babycare/childseat"

        val CLIENT_ID = "guest"
}