package vsper.app.chat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessagesList
import com.stfalcon.chatkit.messages.MessagesListAdapter
import com.stfalcon.chatkit.utils.DateFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import qiu.niorgai.StatusBarCompat
import vsper.app.R
import vsper.app.chat.dialog.Dialog
import vsper.app.chat.message.Message
import vsper.app.chat.message.MessageDbUtil
import vsper.app.global.Core
import vsper.app.global.GlobalRegistry
import vsper.app.holders.message.CustomIncomingTextMessageViewHolder
import vsper.app.holders.message.CustomOutcomingTextMessageViewHolder
import vsper.app.utils.AppUtils
import vsper.app.utils.AppUtils.copyToClipboard
import vsper.eventChannel.event.ToShowMsgEvent
import java.text.SimpleDateFormat
import java.util.*


/*what*/class DialogActivity :

    AppCompatActivity(),
    MessagesListAdapter.SelectionListener,
    MessagesListAdapter.OnLoadMoreListener,
    MessageInput.InputListener,
    MessageInput.AttachmentsListener,
    MessageInput.TypingListener,
    DateFormatter.Formatter {
    companion object {
        private const val TAG = "dialogActivity"
    }

    lateinit var dialog: Dialog
    lateinit var msgList: MessagesList
    lateinit var msgListAdapter: MessagesListAdapter<Message>
    lateinit var input: MessageInput
    private lateinit var menu: Menu
    var selectionCount: Int = 0
    var lastLoadedDate: Date = Date()
    lateinit var onMsgRecvHandler: Handler
    val messagesToHandle = HashMap<String, Message>()

    private val firstLoad = 300
    private val oldestMsgCreatedAt = Date()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog)
        //StatusBarCompat.translucentStatusBar(this, true);
        val statusColor = AppUtils.getColor(this, R.color.dialog_toolbar)
        StatusBarCompat.setStatusBarColor(this, statusColor, 0)

        val dialogId = intent.getStringExtra("dialog")!!
        dialog = Core.getDialog(dialogId)!!
        Log.d("$TAG:DialogActivity onCreate", "extra data is $dialogId")

        val actionBar: Toolbar? = findViewById(R.id.dialog_toolbar)
        setSupportActionBar(actionBar)

        val title: TextView = findViewById(R.id.dialog_title)
        title.text = dialog.dialogName

        input = findViewById(R.id.msgInput)
        msgList = findViewById(R.id.msgList)
        initAdapter()
        msgList.setAdapter(msgListAdapter)
        input.setInputListener(this)
        input.setTypingListener(this)
        input.setAttachmentsListener(this)
        dialog.unreadCount = 0
        //初始化时调用一次加载历史记录
        onLoadMore(0, 1);

        GlobalRegistry.eventChannel.addListenerAlways("message update", ToShowMsgEvent::class) {
            val e = it as ToShowMsgEvent
            this.lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    msgListAdapter.addToStart(e.message, true)
                    dialog.unreadCount--
                }
            }
        }

    }

    fun initAdapter() {
        val holdersConfig = MessageHolders()
            .setIncomingTextConfig(
                CustomIncomingTextMessageViewHolder::class.java,
                R.layout.item_custom_incoming_text_message
            )
            .setOutcomingTextConfig(
                CustomOutcomingTextMessageViewHolder::class.java,
                R.layout.item_custom_outcoming_text_message
            )

        val imageLoader = ImageLoader { imageView, url, payload ->
            Picasso.get().load(url).into(
                imageView
            )
        }
        msgListAdapter =
            MessagesListAdapter<Message>(GlobalRegistry.logAccount, holdersConfig, imageLoader)
        loadHistory(firstLoad)
        msgListAdapter.setLoadMoreListener(this)
        msgListAdapter.enableSelectionMode(this)
        msgListAdapter.registerViewClickListener(
            R.id.messageUserAvatar
        ) { view, message ->
            AppUtils.toast("别点了，还不能拍了拍某人")
        }

        if (GlobalRegistry.msgCopyFree())
            msgListAdapter.disableSelectionMode()
    }

    private fun loadHistory(num: Int): Boolean {
        val queryMsgs = MessageDbUtil.queryMsg(dialog.id, lastLoadedDate, num)
        if (queryMsgs.size > 0) {
            msgListAdapter.addToEnd(queryMsgs, false)
            lastLoadedDate = queryMsgs[0].createdAt
        }
        return queryMsgs.size > 0
    }

    //滚动到顶部加载历史记录
    override fun onLoadMore(page: Int, totalItemsCount: Int) {
        val handler = object : Handler(Looper.getMainLooper()) {}
//        handler.postDelayed({
//            val messages = MessagesData.getMessages(lastLoadedDate);
//            lastLoadedDate = messages[messages.size - 1].createdAt;
//            msgListAdapter.addToEnd(messages, false);
//        }, 1000);
    }

    override fun onSubmit(input: CharSequence?): Boolean {
        Log.d(TAG, "on submit")
        if (GlobalRegistry.logStatus() == GlobalRegistry.LogStatus.logined)
            dialog.sendTextMsg(input.toString())
        else
            AppUtils.toast("请先登录！！")
        return true
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.chat_actions_menu, menu)
        onSelectionChanged(0)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> msgListAdapter.deleteSelectedMessages()
            R.id.action_copy -> {

                copyToClipboard(
                    this,
                    msgListAdapter.getSelectedMessagesText(
                        { it -> it.text },
                        true
                    )
                )
            }
//            R.id.action_change->{
//                msgListAdapter.disableSelectionMode()
//            }
        }
        return true
    }

    override fun onBackPressed() {
        if (selectionCount == 0) {
            super.onBackPressed()
        } else {
            msgListAdapter.unselectAllItems()
        }
    }


    override fun onSelectionChanged(count: Int) {
        this.selectionCount = count
        menu.findItem(R.id.action_delete).setVisible(count > 0)
        menu.findItem(R.id.action_copy).setVisible(count > 0)
//        menu.findItem(R.id.action_change).setVisible(count>0)
    }


    private fun getMessageStringFormatter(): MessagesListAdapter.Formatter<Message>? {
        return MessagesListAdapter.Formatter { message: Message ->
            val createdAt =
                SimpleDateFormat("MMM d, EEE 'at' h:mm a", Locale.getDefault())
                    .format(message.createdAt)
            String.format(
                Locale.getDefault(), "%s: %s (%s)",
                message.user.name, message.text, createdAt
            )
        }
    }

    override fun onAddAttachments() {
        Log.d(TAG, "onAddAttachments")

    }

    override fun onStartTyping() {
        Log.d(TAG, "nStartTyping")

    }

    override fun onStopTyping() {
        Log.d(TAG, "onStopTyping")

    }

    private fun setListenerByHandler() {
        onMsgRecvHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: android.os.Message) {
                val message = messagesToHandle[msg.data.getString("id")]!!
                msgListAdapter.addToStart(message, true)
                messagesToHandle.remove(message.id)
                dialog.unreadCount--
            }

        }

        GlobalRegistry.eventChannel.addListenerAlways("message update", ToShowMsgEvent::class) {
            val e = it as ToShowMsgEvent
            messagesToHandle[e.message.id] = e.message
            onMsgRecvHandler.sendMessage(android.os.Message().apply {
                data = Bundle().apply { putString("id", e.message.id) }
            })
        }
    }

    override fun format(date: Date?): String {
        return when {
            DateFormatter.isToday(date) -> getString(R.string.chat_date_header_today)
            DateFormatter.isYesterday(date) -> getString(R.string.chat_date_header_yesterday)
            else -> DateFormatter.format(date, "EEEE, dd MMMM")
        }
    }
}