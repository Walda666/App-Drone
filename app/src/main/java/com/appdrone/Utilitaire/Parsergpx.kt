package com.appdrone.Utilitaire

import android.annotation.SuppressLint
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.util.Log
import com.appdrone.entities.Drone
import com.appdrone.entities.Waypoint
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import okio.IOException
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.time.LocalDateTime


class Parsergpx {

    @SuppressLint("SuspiciousIndentation")
    fun writeToFile(fos: FileOutputStream, name: String, desc: String, trajectoire : ArrayList<Waypoint>, drone : Drone) {

        // vérifcation de l'existence et de l'état d'ouverture du fichier
        try {
            fos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".toByteArray())
            fos.write("<gpx version=\"1.1\" creator=\"My Application\">\n".toByteArray())
            fos.write("\t<metadata>\n".toByteArray())
            fos.write("\t\t<name>$name</name>\n".toByteArray())
            fos.write("\t\t<desc>$desc</desc>\n".toByteArray())
            fos.write("\t</metadata>\n".toByteArray())
            fos.write("\t<trk>\n".toByteArray())
            fos.write("\t\t<name>$name</name>\n".toByteArray())
            fos.write("\t\t<trkseg>\n".toByteArray())

            // parcours de la liste des points
            for (point in trajectoire) {
                val dateaddition = LocalDateTime.now().plusHours(calculTrajet(LatLng(point.latitude,point.longitude),5.0,drone).toLong())

                fos.write("\t\t\t<trkpt lon=\"${point.longitude}\" lat=\"${point.latitude}\">\n".toByteArray())
                fos.write("\t\t\t\t<time>$dateaddition</time>\n".toByteArray())
                fos.write("\t\t\t</trkpt>\n".toByteArray())
            }

            fos.write("\t\t</trkseg>\n".toByteArray())
            fos.write("\t</trk>\n".toByteArray())
            fos.write("</gpx>\n".toByteArray())

        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }
    // calcul du temps de trajet
    fun calculTrajet(lnglat : LatLng, vitesse : Double, drone : Drone) : Double{

        // conversion de knots en m/s
        val metreSeconds = vitesse * 0.514444

        // calcul de la distance entre le drone et le point
        val distance : Double = SphericalUtil.computeDistanceBetween(LatLng(drone.position!!.longitude,drone.position!!.latitude), lnglat)

        // calcul du temps de trajet
        val temps : Double = distance / metreSeconds // temps en secondes
        val tempsHeure  = temps/3600 // temps en heure

            println("distance : $distance")
            println("temps : $temps")
            println("tempsHeure : $tempsHeure")

        return tempsHeure
    }
}
