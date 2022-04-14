package vsper.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import vsper.app.R
import vsper.app.debug.TalkFragment
import vsper.app.global.GlobalRegistry


class DebugFragment : Fragment() {
    lateinit var textView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_debug, container, false)
        val switch: Switch = view.findViewById(R.id.debug_switch)
        switch.textOff = "调试模式已关闭"
        switch.textOn = "调试模式已打开"
        switch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            GlobalRegistry.isDebugOn = isChecked
        })
        replaceFragment(TalkFragment())
        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = parentFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.main_fragment, fragment)
        transaction.commit()
    }
}