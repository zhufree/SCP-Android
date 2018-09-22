package info.free.scp.util

import android.annotation.SuppressLint
import android.app.Fragment
import android.util.Log

import java.util.LinkedList

/**
 * Fragment的mUserVisibleHint属性控制器，用于准确的监听Fragment是否对用户可见
 * <br></br>
 * <br></br>mUserVisibleHint属性有什么用？
 * <br></br>* 使用ViewPager时我们可以通过Fragment的getUserVisibleHint()&&isResume()方法来判断用户是否能够看见某个Fragment
 * <br></br>* 利用这个特性我们可以更精确的统计页面的显示事件和标准化页面初始化流程（真正对用户可见的时候才去请求数据）
 * <br></br>
 * <br></br>解决BUG
 * <br></br>* FragmentUserVisibleController还专门解决了在Fragment或ViewPager嵌套ViewPager时子Fragment的
 * mUserVisibleHint属性与父Fragment的mUserVisibleHint属性不同步的问题
 * <br></br>* 例如外面的Fragment的mUserVisibleHint属性变化时，其包含的ViewPager中的Fragment的mUserVisibleHint属性并不会随着改变，这是ViewPager的BUG
 * <br></br>
 * <br></br>使用方式（假设你的基类Fragment是MyFragment）：
 * <br></br>1. 在你的MyFragment的构造函数中New一个FragmentUserVisibleController（一定要在构造函数中new）
 * <br></br>2. 重写Fragment的onActivityCreated()、onResume()、onPause()、setUserVisibleHint(boolean)方法，
 * 分别调用FragmentUserVisibleController的activityCreated()、resume()、pause()、setUserVisibleHint(boolean)方法
 * <br></br>3. 实现FragmentUserVisibleController.UserVisibleCallback接口并实现以下方法
 * <br></br>&nbsp&nbsp&nbsp&nbsp* void setWaitingShowToUser(boolean)：直接调用FragmentUserVisibleController的setWaitingShowToUser(boolean)即可
 * <br></br>&nbsp&nbsp&nbsp&nbsp* void isWaitingShowToUser()：直接调用FragmentUserVisibleController的isWaitingShowToUser()即可
 * <br></br>&nbsp&nbsp&nbsp&nbsp* void callSuperSetUserVisibleHint(boolean)：调用父Fragment的setUserVisibleHint(boolean)方法即可
 * <br></br>&nbsp&nbsp&nbsp&nbsp* void onVisibleToUserChanged(boolean, boolean)：当Fragment对用户可见或不可见的就会回调此方法，
 * 你可以在这个方法里记录页面显示日志或初始化页面
 * <br></br>&nbsp&nbsp&nbsp&nbsp* boolean isVisibleToUser()：判断当前Fragment是否对用户可见，直接调用FragmentUserVisibleController
 * 的isVisibleToUser()即可
 */
@SuppressLint("LongLogTag")
class FragmentUserVisibleController(private val fragment: Fragment, private val userVisibleCallback: UserVisibleCallback) {
    private val fragmentName: String?
    var isWaitingShowToUser: Boolean = false
    private var userVisibleListenerList: MutableList<OnUserVisibleListener>? = null

    /**
     * 当前Fragment是否对用户可见
     */
    val isVisibleToUser: Boolean
        get() = fragment.isResumed && fragment.userVisibleHint

    init {

        this.fragmentName = if (DEBUG) fragment.javaClass.simpleName else null
    }

    fun activityCreated() {
        if (DEBUG) {
            Log.d(TAG, fragmentName + ": activityCreated, userVisibleHint=" + fragment.userVisibleHint)
        }
        if (fragment.userVisibleHint) {
            val parentFragment = fragment.parentFragment
            if (parentFragment != null && !parentFragment.userVisibleHint) {
                if (DEBUG) {
                    Log.d(TAG, fragmentName + ": activityCreated, parent " + parentFragment.javaClass.simpleName + " is hidden, therefore hidden self")
                }
                userVisibleCallback.setWaitingShowToUser(true)
                userVisibleCallback.callSuperSetUserVisibleHint(false)
            }
        }
    }

    fun resume() {
        if (DEBUG) {
            Log.d(TAG, fragmentName + ": resume, userVisibleHint=" + fragment.userVisibleHint)
        }
        if (fragment.userVisibleHint) {
            userVisibleCallback.onVisibleToUserChanged(true, true)
            callbackListener(true, true)
            if (DEBUG) {
                Log.i(TAG, fragmentName!! + ": visibleToUser on resume")
            }
        }
    }

    fun pause() {
        if (DEBUG) {
            Log.d(TAG, fragmentName + ": pause, userVisibleHint=" + fragment.userVisibleHint)
        }
        if (fragment.userVisibleHint) {
            userVisibleCallback.onVisibleToUserChanged(false, true)
            callbackListener(false, true)
            if (DEBUG) {
                Log.w(TAG, fragmentName!! + ": hiddenToUser on pause")
            }
        }
    }

    fun setUserVisibleHint(isVisibleToUser: Boolean) {
        val parentFragment = fragment.parentFragment
        if (DEBUG) {
            val parent: String
            if (parentFragment != null) {
                parent = "parent " + parentFragment.javaClass.simpleName + " userVisibleHint=" + parentFragment.userVisibleHint
            } else {
                parent = "parent is null"
            }
            Log.d(TAG, fragmentName + ": setUserVisibleHint, userVisibleHint=" + isVisibleToUser
                    + ", " + (if (fragment.isResumed) "resume" else "pause") + ", " + parent)
        }

        // 父Fragment还没显示，你着什么急
        if (isVisibleToUser) {
            if (parentFragment != null && !parentFragment.userVisibleHint) {
                if (DEBUG) {
                    Log.d(TAG, fragmentName + ": setUserVisibleHint, parent " +
                            parentFragment.javaClass.simpleName + " is hidden, therefore hidden self")
                }
                userVisibleCallback.setWaitingShowToUser(true)
                userVisibleCallback.callSuperSetUserVisibleHint(false)
                return
            }
        }

        if (fragment.isResumed) {
            userVisibleCallback.onVisibleToUserChanged(isVisibleToUser, false)
            callbackListener(isVisibleToUser, false)
            if (DEBUG) {
                if (isVisibleToUser) {
                    Log.i(TAG, fragmentName!! + ": visibleToUser on setUserVisibleHint")
                } else {
                    Log.w(TAG, fragmentName!! + ": hiddenToUser on setUserVisibleHint")
                }
            }
        }

        if (fragment.activity != null) {
            val childFragmentList = fragment.childFragmentManager.fragments
            if (isVisibleToUser) {
                // 显示待显示的子Fragment
                if (childFragmentList != null && childFragmentList.size > 0) {
                    for (childFragment in childFragmentList) {
                        if (childFragment is UserVisibleCallback) {
                            val userVisibleCallback = childFragment as UserVisibleCallback
                            if (userVisibleCallback.isWaitingShowToUser()) {
                                if (DEBUG) {
                                    Log.d(TAG, fragmentName + ": setUserVisibleHint, show child "
                                            + childFragment.javaClass.simpleName)
                                }
                                userVisibleCallback.setWaitingShowToUser(false)
                                childFragment.userVisibleHint = true
                            }
                        }
                    }
                }
            } else {
                // 隐藏正在显示的子Fragment
                if (childFragmentList != null && childFragmentList.size > 0) {
                    for (childFragment in childFragmentList) {
                        if (childFragment is UserVisibleCallback) {
                            val userVisibleCallback = childFragment as UserVisibleCallback
                            if (childFragment.userVisibleHint) {
                                if (DEBUG) {
                                    Log.d(TAG, fragmentName + ": setUserVisibleHint, hidden child "
                                            + childFragment.javaClass.simpleName)
                                }
                                userVisibleCallback.setWaitingShowToUser(true)
                                childFragment.userVisibleHint = false
                            }
                        }
                    }
                }
            }
        }
    }

    private fun callbackListener(isVisibleToUser: Boolean, invokeInResumeOrPause: Boolean) {
        if (userVisibleListenerList != null && userVisibleListenerList!!.size > 0) {
            for (listener in userVisibleListenerList!!) {
                listener.onVisibleToUserChanged(isVisibleToUser, invokeInResumeOrPause)
            }
        }
    }

    fun addOnUserVisibleListener(listener: OnUserVisibleListener?) {
        if (listener != null) {
            if (userVisibleListenerList == null) {
                userVisibleListenerList = LinkedList()
            }
            userVisibleListenerList!!.add(listener)
        }
    }

    fun removeOnUserVisibleListener(listener: OnUserVisibleListener?) {
        if (listener != null && userVisibleListenerList != null) {
            userVisibleListenerList!!.remove(listener)
        }
    }

    interface UserVisibleCallback {
        fun isWaitingShowToUser(): Boolean

        fun setWaitingShowToUser(waitingShowToUser: Boolean)

        fun isVisibleToUser(): Boolean

        fun callSuperSetUserVisibleHint(isVisibleToUser: Boolean)

        fun onVisibleToUserChanged(isVisibleToUser: Boolean, invokeInResumeOrPause: Boolean)
    }

    interface OnUserVisibleListener {
        fun onVisibleToUserChanged(isVisibleToUser: Boolean, invokeInResumeOrPause: Boolean)
    }

    companion object {
        private val TAG = "FragmentUserVisibleController"
        var DEBUG = true
    }
}
