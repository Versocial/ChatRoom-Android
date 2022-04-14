package vsper.app.main.contacts

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import vsper.app.R
import vsper.app.chat.user.User
import vsper.app.chat.user.UserList
import vsper.app.holders.contact.ContactViewHolder
import vsper.app.utils.AppUtils

class ContactsAdapter(val usersList: UserList) :
    RecyclerView.Adapter<ContactViewHolder>() {
    companion object{private val TAG="contactsAdapter"}

    private val usersID: ArrayList<String>  =ArrayList<String>(usersList.keys())
    private val holdersID:HashMap<String,ContactViewHolder> = HashMap()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val user=usersList.getUser(usersID[position])
        if (user != null) {
            if(holdersID.containsKey(user.id)) {
                holdersID[user.id]?.bind(user)
            }
            else {
                holdersID[user.id]=holder
                holder.bind(user)
            }
        }else{
            Log.e(TAG,"unknown user ${usersID[position]}")
        }
    }

    fun refreshUser(userId:String){
        val user=usersList.getUser(userId)
        if(holdersID.contains(userId)){
            if (user != null) {//change
                holdersID[userId]?.bind(user)
            }
            else{//remove
                val position=usersID.indexOf(userId)
                usersID.removeAt(position)
                holdersID.remove(userId)
                notifyItemRemoved(position)
            }
        }
        else{
            if(user!=null){//create
                usersID.add(userId)
                notifyItemInserted(usersID.indexOf(userId))
            }
        }
    }

    fun refreshAllUsers(){
        val keys=usersList.keys()
        for( key in keys){
            refreshUser(key)
        }
    }


    override fun getItemCount() = usersID.size
}