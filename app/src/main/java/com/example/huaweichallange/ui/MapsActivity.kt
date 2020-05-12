package com.example.huaweichallange.ui

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.huaweichallange.model.UserInfoModel
import com.example.huaweichallange.util.IOnLoadLocationListener
import com.example.huaweichallange.util.MyLatlng
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
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
import com.squareup.picasso.Picasso
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, IOnLoadLocationListener,
     GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    com.google.android.gms.location.LocationListener{

    private var mMap: GoogleMap? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var currentMarker: Marker? = null
    private lateinit var myLocationRef: DatabaseReference
    private lateinit var dangerousArea: MutableList<LatLng>
    private lateinit var listener: IOnLoadLocationListener
    private var photoUrl: String? = null
    private lateinit var myCity: DatabaseReference
    private lateinit var lastLocation: Location
    private var geoQuery: GeoQuery? = null
    private lateinit var geoFire: GeoFire
    private lateinit var locationManager: LocationManager
    private lateinit var latLng: LatLng
    private lateinit var location: Location
    private lateinit var locationListener: LocationListener
    private lateinit var mLocation : Location
    private lateinit var mGoogleApiClient : GoogleApiClient
    private var UPDATE_INTERVAL: Long = 2000
    private var FASTEST_INTERVAL: Long = 5000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.huaweichallange.R.layout.activity_maps)

        val personInfo = intent.extras!!.get("extra") as UserInfoModel
        photoUrl = personInfo.personPhoto


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
                val msg : String = "Latitude:${lastLocation!!.latitude}, Longitude:${lastLocation!!.longitude}"
                Toast.makeText(this@MapsActivity, msg, Toast.LENGTH_SHORT).show()


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
                ).title("You").icon(BitmapDescriptorFactory.fromBitmap(getBitmap(photoUrl)))
            )
            mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(currentMarker!!.position, 12.0f))

        }
    }
    fun getBitmap(url : String?) : Bitmap? {
        var bmp : Bitmap ? = null
        Picasso.get().load(url).into(object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                bmp =  bitmap
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
        })
        return bmp
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 500
        locationRequest.fastestInterval = 300
        locationRequest.smallestDisplacement = 5f

    }

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
        }
        val pref= this@MapsActivity.getSharedPreferences("darkMode", Context.MODE_PRIVATE)
        val darkMode = pref.getString("darkMode","dark")
        //val darkMode = getIntent().getExtras()!!.getString("darkMode");
        if(darkMode == "darkModeOn"){
            useDarkMode(googleMap)
        }
        else{

        }


    }

    private fun useDarkMode(googleMap: GoogleMap?) {
        try {
            val success : Boolean = googleMap!!.setMapStyle(
                MapStyleOptions.
            loadRawResourceStyle(this, com.example.huaweichallange.R.raw.map_style))
        }
        catch (e: Resources.NotFoundException){
            Log.e("MapActivity","Map Style Cannot found")
        }
    }


    override fun onLocationLoadSuccess(latLng: List<MyLatlng>) {
        dangerousArea = ArrayList()
        for (myLatLng in latLng) {
            val convert = LatLng(myLatLng.latitude, myLatLng.longitude)

            dangerousArea.add(convert)
        }
        val mapFragment = supportFragmentManager
            .findFragmentById(com.example.huaweichallange.R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        if (mMap != null) {
            mMap!!.clear()

            addUserMarker()

        }

    }


    override fun onLocationLoadFailed(message: String) {
        Toast.makeText(this, "" + message, Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }


    override fun onConnected(p0: Bundle?) {

    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    override fun onLocationChanged(location: Location?) {


    }

}
