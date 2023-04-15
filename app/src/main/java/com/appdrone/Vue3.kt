package com.appdrone

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.widget.Button
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.appdrone.databinding.ActivityVue3Binding
import com.appdrone.entities.Drone
import com.appdrone.entities.Waypoint
import com.appdrone.Utilitaire.Parsergpx
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList

class Vue3 : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityVue3Binding

    // Création d'un timer et d'une liste
    private val tab : ArrayList<Waypoint> = ArrayList<Waypoint>()
    private val timer : Timer= Timer()

    // Création du drone
    val drone = Drone("drone")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVue3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Autorisation de l'application à accéder au réseau
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val buttonFichier = findViewById<Button>(R.id.buttonSuivi)

        buttonFichier.setOnClickListener {
            val parser = Parsergpx()

            val fos : FileOutputStream = openFileOutput("trajet.gpx", Context.MODE_PRIVATE)
            parser.writeToFile(fos, "trajet 1 ", "Démonstration du trajet", tab, drone)
            fos.close()
        }

        // Ajout d'un listener sur la carte
        mMap.setOnMapClickListener(this)

        // Création d'un marqueur pour le drone et zoom sur la position du drone
        val markerBateau = mMap
            .addMarker(MarkerOptions()
                .position(LatLng(drone.position!!.longitude, drone.position!!.latitude))
                .title(drone.name)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon))
                .snippet(""))
        if (markerBateau != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(markerBateau.position))
        }

        // Création d'une tache pour mettre à jour la position du drone
        val task = object : TimerTask() {
            override fun run() {

                // Mise à jour des valeurs du drone
                drone.update()

                // Mise à jour de la position du drone
                this@Vue3.runOnUiThread {
                    if (markerBateau != null) {
                        // Mise à jour de la position du drone
                        markerBateau.position = LatLng(drone.position!!.longitude, drone.position!!.latitude)

                        // Mise à jour de la direction du drone
                        markerBateau.rotation = drone.orientation!!.toFloat()

                        // Affichage de la vitesse du drone
                        markerBateau.snippet =
                            "Vitesse : " + drone.vitesse + " knots" + " -> " + Math.round(drone.vitesse!! * 1.852) + " km/h"

                        /* println("Latitude : " + drone.position!!.latitude)
                            println("Longitude : " + drone.position!!.longitude)
                            println("Orientation : " + drone.orientation)
                            println("Vitesse : " + drone.vitesse)
                        */
                    }
                }
            }
        }
        // lancement du timer
        timer.schedule(task, 0, 1000)
    }

    // Ajout d'un waypoint sur la carte
    override fun onMapClick(latLng: LatLng) {
        // Ajout d'un waypoint sur la carte et ajout dans la liste
        mMap.addMarker(MarkerOptions().position(latLng))
        tab.add(Waypoint(latLng.longitude, latLng.latitude))
    }

    override fun onDestroy() {
        super.onDestroy()
        // Arrêt du timer et de la récupération des données
        drone.stop()
        timer.cancel()
        finish()
    }
}