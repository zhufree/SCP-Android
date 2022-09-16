package info.free.scp.view.game

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ktx.getValue
import info.free.scp.ScpApplication
import info.free.scp.bean.GameModel


class GameViewModel : ViewModel() {
    private var _gameList = MutableLiveData(listOf<GameModel>())
    var gameList: LiveData<List<GameModel>> = _gameList
        private set

    fun getGames() {
        ScpApplication.database.child("games").get().addOnSuccessListener { data ->
            Log.i("firebase", "Got value ${data.value}")
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
//            next(games)
        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
        }
    }
}