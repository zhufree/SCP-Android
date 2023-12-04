package info.free.scp.view.home

import android.os.Bundle
import info.free.scp.R
import info.free.scp.databinding.ActivityPrivacyBinding
import info.free.scp.view.base.BaseActivity

class PrivacyActivity : BaseActivity() {
    private lateinit var binding: ActivityPrivacyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        baseToolbar = binding.privacyToolbar
    }
}