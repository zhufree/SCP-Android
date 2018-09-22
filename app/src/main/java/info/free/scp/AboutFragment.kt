package info.free.scp


import android.app.Fragment
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.free.scp.util.Toaster
import info.free.scp.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_about.*


/**
 * A simple [Fragment] subclass.
 * Use the [AboutFragment.newInstance] factory method to
 * create an instance of this fragment.
 * 其他，包括写作相关，新人资讯，标签云和关于
 */
class AboutFragment : BaseFragment() {
//    private var listener: CategoryListener? = null

    // TODO: Rename and change types of parameters
//    private var mParam1: String? = null
//    private var mParam2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        if (arguments != null) {
//            mParam1 = arguments!!.getString(ARG_PARAM1)
//            mParam2 = arguments!!.getString(ARG_PARAM2)
//        }
    }

//    override fun onAttach(context: Context?) {
//        super.onAttach(context)
//        if (context is CategoryListener) {
//            listener = context
//        } else {
//            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
//        }
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_qq_group?.setOnLongClickListener {
            val clipboardManager = mContext?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clipData = ClipData.newPlainText("qqGroup", "805194504")
            clipboardManager?.primaryClip = clipData
            Toaster.show("已复制到剪贴板")
            true
        }
    }

    override fun onDetach() {
        super.onDetach()
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//        private val LISTENER = "listener"
//        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
//        fun newInstance(param1: String, param2: String): HomeFragment {
        fun newInstance(): AboutFragment {
            val fragment = AboutFragment()
//            val args = Bundle()
//            args.putString(ARG_PARAM1, param1)
//            args.putString(ARG_PARAM2, param2)
//            fragment.arguments = args
            return fragment
        }
    }

    interface CategoryListener {
        fun onCategoryClick(type: Int)
    }

} // Required empty public constructor
