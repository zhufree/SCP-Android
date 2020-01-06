package info.free.scp.view.eatroom

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import android.view.View
import androidx.lifecycle.ViewModelProvider
import info.free.scp.R
import info.free.scp.bean.MealModel
import info.free.scp.bean.PortalModel
import info.free.scp.databinding.ActivityMealListBinding
import info.free.scp.view.base.BaseActivity

class MealListActivity : BaseActivity() {
    private val model by lazy {
        ViewModelProvider(this)
                .get(MealListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bd: ActivityMealListBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_meal_list)


        baseToolbar = bd.mealToolbar

        val adapter = MealAdapter()
        bd.rlMealList.adapter = adapter

        model.getMealList()
                ?.observe(this, Observer<List<MealModel>> { portals ->
                    // update UI
                    if (portals.isNotEmpty()) {
                        bd.tvEmptyMeal.visibility = View.GONE
                        adapter.submitList(portals)
                    } else {
                        bd.tvEmptyMeal.visibility = View.VISIBLE
                        bd.tvEmptyMeal.text = "加载失败"
                    }
                })
        model.loadMealModels()
    }
}
