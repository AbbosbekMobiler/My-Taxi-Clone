package abbosbek.mobiler.mytaxiclone.adapter

import abbosbek.mobiler.mytaxiclone.databinding.TripsItemLayoutBinding
import abbosbek.mobiler.mytaxiclone.model.UserAddress
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class TripsAdapter : RecyclerView.Adapter<TripsAdapter.ItemHolder>() {
    inner class ItemHolder(val binding: TripsItemLayoutBinding) : RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                onItemClickListener?.invoke(differ.currentList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(
            TripsItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val userItem = differ.currentList[position]

        holder.binding.apply {
            tvUserLocation.text = userItem.addressName
            tvTaxiLocation.text = "${userItem.longitude} and ${userItem.latitude}"
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<UserAddress>(){
        override fun areItemsTheSame(oldItem: UserAddress, newItem: UserAddress): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserAddress, newItem: UserAddress): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this,diffUtil)

    private var onItemClickListener : ((UserAddress) -> Unit) ?= null

    fun setOnItemClickListener(listener : (UserAddress) -> Unit){
        onItemClickListener = listener
    }

}