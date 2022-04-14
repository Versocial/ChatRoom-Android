package vsper.app.debug

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vsper.app.R

/**
 * Created by.
 */
class TalkAdapter(talkList: List<Talk>) :
    RecyclerView.Adapter<TalkAdapter.ViewHolder>() {
    private val mTalkList: List<Talk>

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var leftLayout: LinearLayout
        var rightLayout: LinearLayout
        var leftMsg: TextView
        var rightMsg: TextView

        init {
            leftLayout = view.findViewById(R.id.left_layout) as LinearLayout
            rightLayout = view.findViewById<View>(R.id.right_layout) as LinearLayout
            leftMsg = view.findViewById<View>(R.id.left_msg) as TextView
            rightMsg = view.findViewById<View>(R.id.right_msg) as TextView
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_msg, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val talk: Talk = mTalkList[position]
        if (talk.type == Talk.TYPE_RECEIVED) {
            //如果是收到的消息则显示左边的布局,将右边的布局隐藏掉
            holder.leftLayout.visibility = View.VISIBLE
            holder.rightLayout.visibility = View.GONE
            holder.leftMsg.text = talk.content
        } else if (talk.type == Talk.TYPE_SENT) {
            //如果是收到的消息则显示右边的布局，将左边的布局隐藏掉
            holder.rightLayout.visibility = View.VISIBLE
            holder.leftLayout.visibility = View.GONE
            holder.rightMsg.text = talk.content
        }
    }

    override fun getItemCount(): Int {
        return mTalkList.size
    }

    init {
        mTalkList = talkList
    }


}