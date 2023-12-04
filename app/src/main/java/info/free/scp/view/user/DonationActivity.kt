package info.free.scp.view.user

import android.graphics.Color
import android.os.Bundle
import info.free.scp.R
import info.free.scp.databinding.ActivityDonationBinding
import info.free.scp.util.ThemeUtil
import info.free.scp.util.Utils
import info.free.scp.view.base.BaseActivity
import org.jetbrains.anko.dip

class DonationActivity :BaseActivity() {
    private lateinit var binding: ActivityDonationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        baseToolbar = binding.donationToolbar

        binding.btnAfdian.background =
            ThemeUtil.customShape(Color.parseColor("#8A62DC"), 0, 0, dip(32))
        binding.btnMianbaoduo.background =
            ThemeUtil.customShape(Color.parseColor("#FED04D"), 0, 0, dip(32))

        binding.btnAfdian.setOnClickListener {
            startActivity(Utils.getUrlIntent("https://afdian.net/@zhufree"))
        }
        binding.btnMianbaoduo.setOnClickListener {
            startActivity(Utils.getUrlIntent("https://mbd.pub/o/author-b2iUl3Bo"))
        }
    }
}
