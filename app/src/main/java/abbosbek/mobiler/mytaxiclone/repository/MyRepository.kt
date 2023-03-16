package abbosbek.mobiler.mytaxiclone.repository

import abbosbek.mobiler.mytaxiclone.db.UserDao
import abbosbek.mobiler.mytaxiclone.model.UserAddress
import javax.inject.Inject

class MyRepository
@Inject constructor(private val userDao: UserDao){

    fun getUserAddress() = userDao.getAllAddress()

    suspend fun addToUserAddress(userList: List<UserAddress>) = userDao.insert(userList)

    suspend fun deleteToUserAddress(userAddress: UserAddress) = userDao.delete(userAddress)

}