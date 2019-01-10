package com.example.desi.babycare

class Configuration(ip: String) {
    val MQTT_BROKER_URL = "tcp://" + ip + ":1883"

    val POSITION_TOPIC = "babycare/position"

    val SEAT_TOPIC = "babycare/childseat"

    val HEALTH_TOPIC = "babycare/health"

    val CLIENT_ID = "guest"
}