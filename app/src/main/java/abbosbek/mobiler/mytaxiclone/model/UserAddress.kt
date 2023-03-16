package abbosbek.mobiler.mytaxiclone.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_address")
data class UserAddress(
    @PrimaryKey(autoGenerate = true)
    val id : Int?=null,
    val addressName : String,
    val longitude : Double,
    val latitude : Double
)
