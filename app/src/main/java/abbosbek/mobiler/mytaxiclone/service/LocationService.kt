package abbosbek.mobiler.mytaxiclone.service

import abbosbek.mobiler.mytaxiclone.model.TaxiLocation
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import org.greenrobot.eventbus.EventBus

class LocationService : Service() {

    companion object{
        const val CHANNEL_ID = "12345"
        const val NOTIFICATION_ID = 12345
    }

    private var fusedLocationProviderClient : FusedLocationProviderClient?= null
    private var locationCallback : LocationCallback?= null
    private var locationRequest : LocationRequest?= null

    private var notificationManager : NotificationManager ?= null

    private var location : Location ?= null

    override fun onCreate() {
        super.onCreate()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,2000).setIntervalMillis(1000)
            .build()

        locationCallback = object : LocationCallback(){
            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
            }

            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult)
            }
        }

        notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(CHANNEL_ID,"locations",NotificationManager.IMPORTANCE_NONE)

            notificationManager?.createNotificationChannel(notificationChannel)
        }

    }

    @SuppressLint("MissingPermission")
    fun createLocationRequest(){
        try {
            fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest!!,locationCallback!!,null
            )
        }catch (e : Exception){
            e.printStackTrace()
        }

    }

    private fun removeLocationUpdates(){
        locationCallback?.let {
            fusedLocationProviderClient?.removeLocationUpdates(it)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        }
        stopSelf()
    }

    private fun onNewLocation(locationResult: LocationResult) {

        location = locationResult.lastLocation
        EventBus.getDefault().post(TaxiLocation(
            latitude = location?.latitude,
            longitude = location?.longitude
        ))
        startForeground(NOTIFICATION_ID,getNotification())

    }

    fun getNotification() : Notification{
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Updates")
            .setContentText(
                "Latitude --> ${location?.latitude}\nLongitude -->${location?.longitude}"
            )
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notification.setChannelId(CHANNEL_ID)
        }
        return notification.build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createLocationRequest()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder ?= null

    override fun onDestroy() {
        super.onDestroy()
        removeLocationUpdates()
    }
}