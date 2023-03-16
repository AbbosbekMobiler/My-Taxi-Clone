package abbosbek.mobiler.mytaxiclone.adapter

import abbosbek.mobiler.mytaxiclone.databinding.MapStyleItemBinding
import abbosbek.mobiler.mytaxiclone.databinding.TripsItemLayoutBinding
import abbosbek.mobiler.mytaxiclone.model.MapStyleModel
import abbosbek.mobiler.mytaxiclone.model.UserAddress
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class MapStyleAdapter : RecyclerView.Adapter<MapStyleAdapter.ItemHolder>() {
    inner class ItemHolder(val binding: MapStyleItemBinding) : RecyclerView.ViewHolder(binding.root){
        init {
            binding.imgMapStyle.setOnClickListener {
                onItemClickListener?.invoke(differ.currentList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(
            MapStyleItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val styleItem = differ.currentList[position]

        holder.binding.apply {
            imgMapStyle.setImageResource(styleItem.styleImg)
            mapStyleName.text = styleItem.styleName
        }
    }

    private val diffUtil = object : DiffUtil.ItemCallback<MapStyleModel>(){
        override fun areItemsTheSame(oldItem: MapStyleModel, newItem: MapStyleModel): Boolean {
            return oldItem.styleName == newItem.styleName
        }

        override fun areContentsTheSame(oldItem: MapStyleModel, newItem: MapStyleModel): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this,diffUtil)

    private var onItemClickListener : ((MapStyleModel) -> Unit) ?= null

    fun setOnItemClickListener(listener : (MapStyleModel) -> Unit){
        onItemClickListener = listener
    }

}