package vsper.app.main.contacts

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vsper.app.R
import vsper.app.global.Core
import vsper.app.global.GlobalRegistry
import vsper.eventChannel.event.ToShowContactsEvent

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ContactsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContactsFragment : Fragment() {
    lateinit var adapter:ContactsAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_contacts, container, false)
        val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        val recyclerView=view.findViewById<RecyclerView>(R.id.allUsersList)

        // Inflate the layout for this fragment
        recyclerView.layoutManager = layoutManager
        adapter = ContactsAdapter(Core.usersList())
        recyclerView.adapter = adapter

        GlobalRegistry.eventChannel.addListenerAlways("show contact",ToShowContactsEvent::class){
            val e=it as ToShowContactsEvent
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    adapter.refreshUser(e.userId)
                }
            }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
//        adapter.refreshAllUsers()
    }

}