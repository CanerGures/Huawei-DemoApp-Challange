package com.example.huaweichallange.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.huaweichallange.R
import com.example.huaweichallange.util.IOnLoadLocationListener
import com.example.huaweichallange.util.MyLatlng
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import java.util.*
import kotlin.collections.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, IOnLoadLocationListener,
    GeoQueryEventListener {

    private var mMap: GoogleMap? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var currentMarker: Marker? = null
    private lateinit var myLocationRef: DatabaseReference
    private lateinit var dangerousArea: MutableList<LatLng>
    private lateinit var listener: IOnLoadLocationListener

    private lateinit var myCity: DatabaseReference
    private lateinit var lastLocation: Location
    private var geoQuery: GeoQuery? = null
    private lateinit var geoFire: GeoFire


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)




        Dexter.withActivity(this)
            .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    buildLocationRequest()
                    buildLocationCallBack()
                    fusedLocationProviderClient =
                        LocationServices.getFusedLocationProviderClient(this@MapsActivity)
                    initArea()
                    settingGeoFire()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: com.karumi.dexter.listener.PermissionRequest?,
                    token: PermissionToken?
                ) {

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(
                        this@MapsActivity,
                        "You must enable this permission",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }).check()


    }

    private fun settingGeoFire() {
        myLocationRef = FirebaseDatabase.getInstance().getReference("MyLocation")
        geoFire = GeoFire(myLocationRef)
    }

    private fun initArea() {
        myCity = FirebaseDatabase.getInstance()
            .getReference("DangerousArea").child("MyCity")

        listener = this

        myCity.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val latLngList = ArrayList<MyLatlng>()
                for (locationSnapShot in dataSnapshot.children) {
                    val latLng = locationSnapShot.getValue(MyLatlng::class.java)
                    latLngList.add(latLng!!)
                }
                listener.onLocationLoadSuccess(latLngList)
            }

        })
    }

    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                if (mMap != null) {
                    lastLocation = locationResult!!.lastLocation
                    addUserMarker()
                }
            }
        }
    }

    private fun addUserMarker() {
        geoFire.setLocation(
            "You",
            GeoLocation(lastLocation.latitude, lastLocation.longitude)
        ) { _, _ ->
            if (currentMarker != null) currentMarker!!.remove()
            currentMarker = mMap!!.addMarker(
                MarkerOptions().position(
                    LatLng(
                        lastLocation.latitude,
                        lastLocation.longitude
                    )
                ).title("You").icon(BitmapDescriptorFactory.fromResource(R.mipmap.huawei))
            )
            mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(currentMarker!!.position, 12.0f))

        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap!!.uiSettings.isZoomControlsEnabled = true

        if (fusedLocationProviderClient != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    return
            }
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )

            addCircleArea()
        }


    }

    override fun onLocationLoadSuccess(latLng: List<MyLatlng>) {
        dangerousArea = ArrayList()
        for (myLatLng in latLng) {
            val convert = LatLng(myLatLng.latitude, myLatLng.longitude)
            dangerousArea.add(convert)
        }
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        if (mMap != null) {
            mMap!!.clear()

            addUserMarker()
            addCircleArea()
        }
    }

    private fun addCircleArea() {
        if (geoQuery != null) {
            geoQuery!!.removeGeoQueryEventListener(this@MapsActivity)
            geoQuery!!.removeAllListeners()
        }
        for (latLng in dangerousArea) {
            mMap!!.addCircle(
                CircleOptions().center(latLng).radius(500.0)
                    .radius(500.0).strokeColor(Color.BLUE)
                    .fillColor(0x220000FF)
                    .strokeWidth(5.0f)
            )

            geoQuery =
                geoFire.queryAtLocation(GeoLocation(latLng.latitude, latLng.longitude), 0.5)
            geoQuery!!.addGeoQueryEventListener(this@MapsActivity)
        }
    }

    override fun onLocationLoadFailed(message: String) {
        Toast.makeText(this, "" + message, Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    override fun onGeoQueryReady() {

    }

    override fun onKeyEntered(key: String?, location: GeoLocation?) {
        sendNotification("caner", String.format("%s entered the dangerous area", key))
    }

    private fun sendNotification(title: String, content: String) {
        Toast.makeText(this, "" + content, Toast.LENGTH_SHORT).show()

        val NOTIFICATION_CHANNEL_ID = "caner_multiple_location"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "myNotification",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationChannel.description = "Channel Description"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)

            val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            builder.setContentTitle(title).setContentText(content).setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))

            val notification = builder.build()
            notificationManager.notify(Random().nextInt(), notification)


        }
    }

    override fun onKeyMoved(key: String?, location: GeoLocation?) {
        sendNotification("caner", String.format("%s move within the dangerous area", key))
    }

    override fun onKeyExited(key: String?) {
        sendNotification("caner", String.format("%s leave the dangerous area", key))
    }

    override fun onGeoQueryError(error: DatabaseError?) {
        Toast.makeText(this, "" + error!!.message, Toast.LENGTH_SHORT).show()
    }
}
