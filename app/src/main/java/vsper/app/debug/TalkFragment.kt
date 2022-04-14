package vsper.app.debug

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import vsper.app.R
import vsper.app.global.GlobalRegistry
import vsper.app.global.GlobalRegistry.TALK_LIST
import vsper.app.global.VsperApplication
import vsper.app.utils.AppUtils
import vsper.eventChannel.event.MsgEvent
import vsper.webConnect.WebConnect

class TalkFragment : Fragment() {
    private lateinit var inputText: EditText
    private lateinit var send: Button
    private lateinit var msgRecyclerView: RecyclerView
    private lateinit var adapter: TalkAdapter
    private lateinit var handler: Handler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_talk, container, false)
        initMsg()
        inputText = view.findViewById(R.id.input_text) as EditText
        send = view.findViewById(R.id.send) as Button
        msgRecyclerView = view.findViewById(R.id.msg_recycler_view) as RecyclerView
        val layoutManager = LinearLayoutManager(VsperApplication.context)
        msgRecyclerView.layoutManager = layoutManager
        adapter = TalkAdapter(TALK_LIST)
        msgRecyclerView.adapter = adapter

        msgRecyclerView.scrollToPosition(TALK_LIST.size - 1)
        //将ListView定位到最后一行

        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                adapter.notifyItemInserted(TALK_LIST.size - 1)
                //当有新消息时刷新ListView中显示的内容
                msgRecyclerView.scrollToPosition(TALK_LIST.size - 1)
                //将ListView定位到最后一行

            }

        }

        send.setOnClickListener {
            if (GlobalRegistry.logStatus() != GlobalRegistry.LogStatus.logined)
                Toast.makeText(VsperApplication.context, "请先登录", Toast.LENGTH_SHORT).show()
            else {
                if (GlobalRegistry.user().id != "verso") {
                    AppUtils.toast("你没有管理员权限！")
                    return@setOnClickListener
                }
                val content = inputText.text.toString()
                if ("" != content) {
                    WebConnect.wsConnect.send(content)
                    inputText.setText("") //清空输入框中的内容
                }
            }
        }
        return view
    }

    private fun initMsg() {
        GlobalRegistry.eventChannel.addListenerAlways("recvMsg", MsgEvent::class) {
            if (!GlobalRegistry.isDebugOn)
                return@addListenerAlways
            val e = it as MsgEvent
            val type =
                if (e.info.startsWith(GlobalRegistry.logAccount + " 说")) Talk.TYPE_SENT else Talk.TYPE_RECEIVED
            val msg = Talk(e.info, type)
            TALK_LIST.add(msg)
            handler.sendEmptyMessage(0)
        }
    }
}