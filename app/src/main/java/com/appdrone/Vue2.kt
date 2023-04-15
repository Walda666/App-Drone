package com.appdrone

import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.StrictMode
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.appdrone.databinding.ActivityVue2Binding
import com.appdrone.entities.Drone
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.SphericalUtil
import java.util.*
import kotlin.math.*

class Vue2 : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityVue2Binding
    val timer = Timer()
    val timer2 = Timer()

    // Création drone
    var drone: Drone = Drone("Drone")

    // On initialise certaines variables du drone ici pour pouvoir y accéder de partout
    // On crée également des booleans permettant de gérer plusieurs actions
    private var doFirst: Boolean = false
    private var timerBool = true
    private var angle = 0.0
    private var vitesseGlobale = 0.0
    private var isGauche: Int = 1
    private lateinit var sensorManager: SensorManager
    private var home: LatLng = LatLng(46.147149, -1.168021)
    private var positionGlobale: LatLng = home

    private var stopAccelerometre: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVue2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // boutons home & urgence ainsi que seekbar pour changer la vitesse
        val buttonHome = findViewById<Button>(R.id.buttonHome)
        val buttonUrgence = findViewById<Button>(R.id.buttonUrgence)

        val vitesseBar: SeekBar = findViewById(R.id.vitessebar)
        vitesseBar.min = 0
        vitesseBar.max = 70

            // Changement de la vitesse selon la valeur de la seekbar
        vitesseBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar?, progress: Int, fromuser: Boolean) {
                vitesseGlobale = progress.toDouble()
            }

            // fonctions obligatoires à implémenter pour l'interface
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })


        // Listener du boutton Home
        buttonHome.setOnClickListener {
            // On calcule l'angle entre la position actuelle et la position de départ
            val lat1 = Math.toRadians(positionGlobale.latitude)
            val lon1 = Math.toRadians(positionGlobale.longitude)
            val lat2 = Math.toRadians(home.latitude)
            val lon2 = Math.toRadians(home.longitude)

            val y = Math.sin(lon2 - lon1) * Math.cos(lat2)
            val x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)

            val bearing = Math.toDegrees(Math.atan2(y, x)).toFloat()
            val temp = (bearing+360)%360

            // Une fois l'angle trouvé, on tourne le bateau dans cette direction pour qu'il retourne à la position de départ
            angle = (temp).toDouble()
        }

        // Listener du boutton Urgence : on met la vitesse à 0, tout comme la position visuelle de la seekbar
        buttonUrgence.setOnClickListener {
            vitesseGlobale = 0.0
            vitesseBar.progress = 0
        }

        // Démarage et setup de l'accelerometre
        setUpSensorStuff()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val polylineOptions = PolylineOptions().color(Color.RED).width(5f)


        // Position de départ : dans le vieux port de La Rochelle
        drone.position!!.latitude = home.latitude
        drone.position!!.longitude = home.longitude
        drone.orientation = 0.0
        drone.vitesse = 0.0

        // On ajoute un marqueur sur la carte pour représenter le bateau
        val markerbateau = mMap.addMarker(
            MarkerOptions()
                .position(LatLng(0.00, 0.00))
                .title(drone.name)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon)).snippet("")
        )
        if (markerbateau != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerbateau.position, 8f))
        }
        // Première task : affiche le bateau sur la carte, en utilisant les variables globales
        val task = object : TimerTask() {
            override fun run() {
                drone.orientation = angle
                drone.vitesse = vitesseGlobale
                drone.position!!.latitude = positionGlobale.latitude
                drone.position!!.longitude = positionGlobale.longitude

                this@Vue2.runOnUiThread {
                    if (markerbateau != null) {
                        markerbateau.position =
                            LatLng(drone.position!!.latitude, drone.position!!.longitude)
                        markerbateau.rotation = drone.orientation!!.toFloat()

                        markerbateau.snippet =
                            "Vitesse : " + drone.vitesse + " knots" + " -> " + Math.round(
                                drone.vitesse!! * 1.852
                            ) + " km/h"
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(markerbateau.position))
                        polylineOptions.add(markerbateau.position)
                        mMap.addPolyline(polylineOptions)
                        /* println("Latitude : " + drone.position!!.latitude)
                           println("Longitude : " + drone.position!!.longitude)
                           println("Orientation : " + drone.orientation)
                            println("Vitesse : " + drone.vitesse)
                        */

                    }
                }
            }
        }

        // Deuxième task : permet de mettre à jour la position du bateau toutes les secondes, en modifiant les variables globales
        val task2 = object : TimerTask() {
            override fun run() {
                // knot -> m/s
                val metresecondes: Double = drone.vitesse!! / 1.94384
                val nvPos: LatLng = getDestinationLatLng(
                    LatLng(
                        drone.position!!.latitude,
                        drone.position!!.longitude
                    ), metresecondes, drone.orientation!!
                )
                positionGlobale = nvPos

                timerBool = true // remettre le boolean toutes les secondes pour l'acceleromètre
            }
        }
        timer2.schedule(task2, 0, 1000)
        timer.schedule(task, 0, 1000)
    }


    // Mise en place de l'accéléromètre
    private fun setUpSensorStuff() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    // Listener de l'accéléromètre
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            if (timerBool && !stopAccelerometre) { // Si une seconde est passée et que bouton Home pas appuyé
                if (!doFirst) { // A l'arrivée sur la Vue, on retient l'orientation du téléphone
                    if (event.values[0] < 0) isGauche = -1
                    println("isGauche set to " + isGauche)
                    doFirst = true
                } else {
                    // Toutes les fois suivantes : on calcule le nouvel angle en fonction  de la rotation du téléphone et de la vitesse
                    val accel: Float = event.values[1]
                    var diviseurVitesse: Int = (16 - (vitesseGlobale / 2.33)).toInt()
                    if (diviseurVitesse < 1) diviseurVitesse = 1
                    // Le drone ne tourne pas si il est à l'arrêt
                    if (vitesseGlobale == 0.0) diviseurVitesse = 9999999
                    angle += (accel * 9.17 * isGauche) / diviseurVitesse // tourne + ou - vite selon vitesse
                    if (angle > 360) angle -= 360 // Remettre à zero
                    if (angle < -360) angle += 360 // Remettre à zero
                    timerBool = false
                }
            }
        }
    }

    // Fonction nécessaire pour l'implémentation de l'accéléromètre
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }

    // A la fin de l'activité, on désenregistre le listener de l'accéléromètre et on arrête les timers task
    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
        timer.cancel()
        timer2.cancel()
        finish()
    }

    // Fonction retournant les coordonnées du point de destination, en fonction de la distance et de l'angle
    fun getDestinationLatLng(originLatLng: LatLng, distance: Double, heading: Double): LatLng {
        // Utilise la méthode computeOffset de SphericalUtil pour calculer les coordonnées du point de destination
        return SphericalUtil.computeOffset(originLatLng, distance, heading)
    }
}