package abbosbek.mobiler.mytaxiclone.ui

import abbosbek.mobiler.mytaxiclone.model.UserAddress
import abbosbek.mobiler.mytaxiclone.repository.MyRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel
@Inject constructor(private val repository: MyRepository) : ViewModel(){

    fun getUserAddress() = repository.getUserAddress()

    fun addUserAddress(userList: List<UserAddress>) = viewModelScope.launch(Dispatchers.IO){
        repository.addToUserAddress(userList)
    }

    fun deleteUserAddress(userAddress: UserAddress) = viewModelScope.launch(Dispatchers.IO){
        repository.deleteToUserAddress(userAddress)
    }

}