package com.appdrone.Utilitaire

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Math.floor
import java.net.Socket


class Threadinit : Thread() {
    // Variables de connexion
    val simulateur = "10.0.2.2"
    val telephone = "192.168.242.107"
    val port = 1234

    // Variables de la trame
    var longitude = ""
    var latitude = ""
    var vitesse = ""
    var orientation = ""
    var orientationLat = ""
    var orientationLong = ""

    override fun run() {
        // test de connexion
        try {
            // Connection au serveur
            val client = Socket(simulateur, port)

            // Reception de la trame
            val input = BufferedReader(InputStreamReader(client.inputStream))

            // Traitement de la trame tant que le thread n'est pas interrompu
            while (!interrupted()) {

                // Parcours de la trame
                input.useLines { lines ->
                    lines.forEach {

                        // Séparation des champs de la trame
                        val fields = it.split(",")

                        // Vérification de la trame qu'on souhaite traiter
                        if (fields[0] == "\$GPRMC") {
                            longitude = fields[3]
                            orientationLong = fields[4]
                            latitude = fields[5]
                            orientationLat = fields[6]
                            vitesse = fields[7].toDouble().toString()
                            orientation = fields[8]

                            // Vérification de la position par rapport à l'équateur et au méridien de Greenwich
                            if (orientationLong.equals("S")) {
                                longitude = (-1 * ddm_to_dd(longitude.toDouble())).toString()
                            } else {
                                longitude = ddm_to_dd(longitude.toDouble()).toString()
                            }
                            if (orientationLat.equals("W")) {
                                latitude = (-1 * ddm_to_dd(latitude.toDouble())).toString()
                            } else {
                                latitude = ddm_to_dd(latitude.toDouble()).toString()
                            }
                        }
                    }
                }
            }
            client.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Fonction de conversion de degrés décimaux minutes en degrés décimaux
    fun ddm_to_dd(ddm: Double): Double {
        val degrees: Double = floor(ddm / 100.0)
        val minutes = ddm - degrees * 100.0
        return degrees + minutes / 60.0
    }
}
