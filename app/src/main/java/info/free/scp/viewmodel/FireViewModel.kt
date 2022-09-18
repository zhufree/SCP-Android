package info.free.scp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ktx.getValue
import info.free.scp.ScpApplication
import info.free.scp.bean.GameModel
import info.free.scp.bean.PortalModel


class FireViewModel : ViewModel() {
    private var _gameList = MutableLiveData(listOf<GameModel>())
    var gameList: LiveData<List<GameModel>> = _gameList
        private set

    private var _portalList = MutableLiveData(listOf<PortalModel>())
    var portalList: LiveData<List<PortalModel>> = _portalList
        private set

    fun getGames() {
        ScpApplication.database.child("games").get().addOnSuccessListener { data ->
            val games = emptyList<GameModel>().toMutableList()
            data.children.forEach { g ->
                val name = g.child("name").getValue<String>() ?: ""
                val desc = g.child("desc").getValue<String>() ?: ""
                val type = g.child("type").getValue<String>() ?: ""
                val platform = g.child("platform").getValue<String>() ?: ""
                val link = g.child("link").getValue<String>() ?: ""
                val cover = g.child("cover").getValue<String>() ?: ""
                val newGame = GameModel(name, desc, type, platform, link, cover)
                games.add(newGame)
            }
            _gameList.value = games
        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
        }
    }

    fun getPortals() {
        ScpApplication.database.child("portals").get().addOnSuccessListener { data ->
            Log.i("firebase", "Got value ${data.value}")
            val portals = emptyList<PortalModel>().toMutableList()
            data.children.forEach { g ->
                val title = g.child("title").getValue<String>() ?: ""
                val url = g.child("url").getValue<String>() ?: ""
                val logoUrl = g.child("logo_url").getValue<String>() ?: ""
                val newPortal = PortalModel(title, url, logoUrl)
                portals.add(newPortal)
            }
            _portalList.value = portals
        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
        }
    }
}