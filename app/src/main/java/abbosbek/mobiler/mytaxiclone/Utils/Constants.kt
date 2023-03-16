package abbosbek.mobiler.mytaxiclone.Utils

import abbosbek.mobiler.mytaxiclone.R
import abbosbek.mobiler.mytaxiclone.model.MapStyleModel

object Constants {


    fun getMapStyleList() : ArrayList<MapStyleModel>{
        val styleList : ArrayList<MapStyleModel> = ArrayList()
        styleList.add(MapStyleModel(styleImg = R.drawable.standart_map,"MAPBOX_STREETS"))
        styleList.add(MapStyleModel(styleImg = R.drawable.satellite,"Satellite"))
        styleList.add(MapStyleModel(styleImg = R.drawable.satellite_streets,"Satellite_Streets"))
        styleList.add(MapStyleModel(styleImg = R.drawable.mapbox_outdoors,"OutDoors"))
        styleList.add(MapStyleModel(styleImg = R.drawable.light,"Light"))
        styleList.add(MapStyleModel(styleImg = R.drawable.dark,"Dark"))

        return styleList

    }

}