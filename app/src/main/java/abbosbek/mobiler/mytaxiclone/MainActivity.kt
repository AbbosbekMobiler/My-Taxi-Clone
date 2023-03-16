package abbosbek.mobiler.mytaxiclone

import abbosbek.mobiler.mytaxiclone.databinding.ActivityMainBinding
import abbosbek.mobiler.mytaxiclone.model.UserAddress
import abbosbek.mobiler.mytaxiclone.service.LocationService
import abbosbek.mobiler.mytaxiclone.ui.MainViewModel
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    private val viewModel by viewModels<MainViewModel>()

    private val userList = ArrayList<UserAddress>()

    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var editor : SharedPreferences.Editor

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){permissions->

            permissions.entries.forEach{
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted){
                    startService(service)
                }else{
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri : Uri = Uri.fromParts("package",packageName,null)
                    intent.data = uri
                    startActivity(intent)
                }
            }

        }

    private var service : Intent?= null

    private lateinit var locationManager : LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        service = Intent(this, LocationService::class.java)
        checkPermissionLauncher()
        addUserToList()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        sharedPreferences = getSharedPreferences("AddressData", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        if (sharedPreferences.getString("address","") == ""){
            viewModel.addUserAddress(userList)
            editor.putString("address","have_data")
            editor.commit()
        }
        checkGps()

    }

    fun checkPermissionLauncher(){
        when{
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ->{
                startService(service)
            }
            else->{
                requestPermissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                )
            }
        }
    }

    private fun checkGps(){
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            startService(service)
        }
        else{
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Diqqat!!")
            dialog.setMessage("Davom ettirish uchun qurilmangizga joylashuvingizni aniqlashga ruxsat bering")
            dialog.setCancelable(false)
            dialog.setPositiveButton("Ok") { _, i ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            dialog.setNegativeButton("Kerak emas"){p0,i->
                p0.dismiss()
            }
            dialog.show()

        }
    }

    fun addUserToList(){
        userList.add(UserAddress(addressName = "Ташкентский Университет Информационных Технологий", latitude = 41.3410323764, longitude = 69.286482781))
        userList.add(UserAddress(addressName = "Мечеть Минор", latitude = 41.3356474279, longitude = 69.2753182741))
        userList.add(UserAddress(addressName = "Сквер Темура Амира", latitude = 41.3107445, longitude = 69.2786403))
        userList.add(UserAddress(addressName = "Ташкент сити", latitude = 41.3120748, longitude = 69.2487945))
        userList.add(UserAddress(addressName = "Milliy stadioni", latitude = 41.2797672, longitude = 69.2103894))
        userList.add(UserAddress(addressName = "ICE CITY", latitude = 41.2964106203, longitude = 69.2473593625))
        userList.add(UserAddress(addressName = "Парк Дружбы (Парк Бабура)", latitude = 41.289724, longitude = 69.254180))
        userList.add(UserAddress(addressName = "IT HOLDING", latitude = 41.2937721, longitude = 69.243982))

    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(service)
    }
}