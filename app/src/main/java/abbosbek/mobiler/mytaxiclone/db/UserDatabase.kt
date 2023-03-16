package abbosbek.mobiler.mytaxiclone.db

import abbosbek.mobiler.mytaxiclone.model.UserAddress
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserAddress::class],
    version = 1,
    exportSchema = true
)
abstract class UserDatabase : RoomDatabase(){

    abstract fun getUserDao() : UserDao

}