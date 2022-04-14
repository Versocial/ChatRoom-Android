package vsper.app.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import com.stfalcon.chatkit.dialogs.DialogsList
import com.stfalcon.chatkit.dialogs.DialogsListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vsper.app.R
import vsper.app.chat.DialogActivity
import vsper.app.chat.dialog.Dialog
import vsper.app.global.Core
import vsper.app.global.GlobalRegistry
import vsper.app.global.VsperApplication
import vsper.app.utils.AppUtils
import vsper.eventChannel.event.ToShowMsgEvent


/*what*/class DialogListFragment() : Fragment() {
    private lateinit var dialogsListAdapter: DialogsListAdapter<Dialog>
    private lateinit var dialogsList: DialogsList
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dialog_list, container, false)
        dialogsList = view.findViewById(R.id.dialogsList) as DialogsList
        dialogsListAdapter = DialogsListAdapter<Dialog> { imageView, url, payload ->
            Picasso.get().load(url).into(imageView)
        }


        dialogsListAdapter.setOnDialogClickListener {
            val intent = Intent(context, DialogActivity::class.java)
            intent.putExtra("dialog", it!!.id)
            startActivity(intent)
        }
        dialogsListAdapter.setOnDialogLongClickListener {
            Toast.makeText(VsperApplication.context, it.dialogName, Toast.LENGTH_SHORT).show()
        }
        dialogsList.setAdapter(dialogsListAdapter)
        initDialogs()
        return view
    }

    private fun initDialogs() {
        dialogsListAdapter.addItems(ArrayList<Dialog>(Core.getDialogs()))
        GlobalRegistry.eventChannel.addListenerAlways("dialog update", ToShowMsgEvent::class) {
            val e = it as ToShowMsgEvent
            if (Core.getDialog(e.dialogId) == null) {
                AppUtils.toast("oh never such a dialog ${e.dialogId}!!")
                return@addListenerAlways
            } else
                refresh()
        }
    }

    private fun refresh() {
        this.lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                dialogsListAdapter.sortByLastMessageDate()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

}
