package vsper.app.holders.contact

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.stfalcon.chatkit.utils.ShapeImageView
import de.hdodenhof.circleimageview.CircleImageView
import vsper.app.R
import vsper.app.chat.user.User
import vsper.app.utils.AppUtils

/*what*/class ContactViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(user: User){
        Picasso.get().load(user.avatar).into(itemView.findViewById(R.id.userAvatar) as ShapeImageView)
        itemView.findViewById<TextView>(R.id.userName).text=user.name
        itemView.findViewById<TextView>(R.id.isUserOnline).text=if(user.isOnline){"在线"}else{"已下线"}
        itemView.findViewById<TextView>(R.id.isUserOnline).setTextColor(
            if(user.isOnline){AppUtils.getColor(colorId = R.color.green)}
            else{AppUtils.getColor(colorId = R.color.yellow)}
        )
    }
}