package com.appdrone

import android.graphics.Color
import android.os.Bundle
import android.os.StrictMode
import androidx.appcompat.app.AppCompatActivity
import com.appdrone.databinding.ActivityVue1Binding
import com.appdrone.entities.Drone
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.lang.Math.round
import java.security.Policy
import java.util.*


class Vue1 : AppCompatActivity(), OnMapReadyCallback, CoroutineScope by MainScope() {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityVue1Binding

    // Création d'un timer pour mettre à jour la position du drone
    private val timer : Timer = Timer()

    // Création d'un drone
    val drone : Drone = Drone("drone")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVue1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        // Autorisation de l'application à accéder au réseau
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        // Création d'un thread pour mettre à jour la position du drone
        val polylineOptions = PolylineOptions().color(Color.RED).width(5f)

        // Création d'un marqueur pour le drone et zoom sur la position du drone
        val markerBateau = mMap.addMarker(
            MarkerOptions()
                .position(LatLng(drone.position!!.latitude, drone.position!!.longitude))
                .title(drone.name)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon))
                .snippet("")
        )
        if (markerBateau != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerBateau.position, 8f))
        }

        // Création d'une tache pour mettre à jour la position du drone
        val task = object : TimerTask() {
            override fun run() {

                // Mise à jour des valeurs du drone
                drone.update()

               /* println("Latitude : " + drone.position!!.latitude)
                println("Longitude : " + drone.position!!.longitude)
                println("Orientation : " + drone.orientation)
                println("Vitesse : " + drone.vitesse)*/

                // Mise à jour de la position du drone
                this@Vue1.runOnUiThread {
                    if (markerBateau != null) {
                        // Mise à jour de la position du drone
                        markerBateau.position = LatLng(drone.position!!.longitude, drone.position!!.latitude)

                        // Mise à jour de la direction du drone
                        markerBateau.rotation = drone.orientation!!.toFloat()

                        // Affichage de la vitesse du drone
                        markerBateau.snippet =
                            "Vitesse : " + drone.vitesse + " knots" + " -> " + round(drone.vitesse!! * 1.852) + " km/h"
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(markerBateau.position))

                        // Création du tracé du drone
                        polylineOptions.add(markerBateau.position)
                        mMap.addPolyline(polylineOptions)
                    }
                }
            }
        }
        // Lancement du timer
        timer.schedule(task, 0, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Arrêt du timer et la réception des données du drone
        drone.stop()
        timer.cancel()
        finish()
    }
}