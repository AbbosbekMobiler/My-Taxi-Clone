package abbosbek.mobiler.mytaxiclone.db

import abbosbek.mobiler.mytaxiclone.model.UserAddress
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDao {

    @Query("select * from user_address")
    fun getAllAddress() : LiveData<List<UserAddress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userList : List<UserAddress>)

    @Delete
    suspend fun delete(userAddress: UserAddress)

}