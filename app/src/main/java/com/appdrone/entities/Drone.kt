package com.appdrone.entities

import com.appdrone.Utilitaire.Threadinit
import com.appdrone.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class Drone(val name: String) {

    // déclaration des variables
    var position: Waypoint? = null
    var vitesse: Double? = null
    var orientation: Double? = null

    // initialisation du thread
    val threadInit: Threadinit = Threadinit()

    init {

        // initialisation du thread
        threadInit.name = "Drone init"
        threadInit.start()

        // attente de la première réception de données
        Thread.sleep(1000)

        // initialisation de la position du drone
        position = Waypoint(threadInit.longitude.toDouble(), threadInit.latitude.toDouble())
        vitesse = threadInit.vitesse.toDouble()
        orientation = threadInit.orientation.toDouble()
    }

    // fonction de mise à jour des données du drone
    fun update() {
        position!!.longitude = threadInit.longitude.toDouble()
        position!!.latitude = threadInit.latitude.toDouble()
        vitesse = threadInit.vitesse.toDouble()
        orientation = threadInit.orientation.toDouble()
    }

    // fonction d'interruption du thread
    fun stop() {
        threadInit.interrupt()
    }
}