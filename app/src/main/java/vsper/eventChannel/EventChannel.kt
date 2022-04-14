package vsper.eventChannel

import android.util.Log
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import kotlin.reflect.KClass

/**
 * 事件通道
 */
class EventChannel : Thread() {
    val TAG = "EventChannel"

    /**
     * 处理事件用的listener
     */
    var ids = HashMap<KClass<*>, Vector<String>>()

    var listeners = HashMap<String, EventListener>()

    /**
     * 事件队列
     */
    private var eventQueue = LinkedBlockingQueue<Event>()

    /**
     * 添加监听器 listener, 每个监听器都有一个事件处理函数
     *
     * @param tClass   监听器监听事件的类型
     * @param listener 要添加的监听器
     */
    @Synchronized
    fun addListener(listenerId: String, tClass: KClass<*>, handle: (Event) -> Boolean) {

        if (listeners.containsKey(listenerId)) {
            Log.d(TAG, "listenerId $listenerId already has, so replace it.")
            val listenerToRemove = listeners[listenerId]
            ids[listenerToRemove!!.tClass()]?.remove(listenerId)
            listeners.remove(listenerId, listenerToRemove)
        }

        val listener = object : EventListener {
            override fun tClass(): KClass<*> {
                return tClass
            }

            @Override
            override fun handle(event: Event): Boolean {
                return handle(event)
            }
        }
        if (!ids.containsKey(tClass))
            ids[tClass] = Vector()
        ids[tClass]?.add(listenerId)
        listeners[listenerId] = listener
    }

    @Synchronized
    fun addListenerAlways(listenerId: String, tClass: KClass<*>, handle: (Event) -> Unit) =
        addListener(listenerId, tClass) {
            handle(it)
            return@addListener false
        }

    @Synchronized
    fun addListenerOnce(listenerId: String, tClass: KClass<*>, handle: (Event) -> Unit) =
        addListener(listenerId, tClass) {
            handle(it)
            return@addListener true
        }

    /**
     * 删除监听器 listener
     *
     * @param listener 要删除的监听器名
     */
    @Synchronized
    private fun removeListener(listenerId: String) {
        if (!listeners.containsKey(listenerId)) {
            Log.d(TAG, "listenerId $listenerId not exists")
        } else {
            val listener = listeners[listenerId]
            ids[listener?.tClass()]?.remove(listenerId)
            listeners.remove(listenerId, listener)
        }
    }

    /**
     * 向事件队列中添加事件
     *
     * @param event 要添加的事件
     */
    fun addEvent(event: Event) {
        eventQueue.add(event)
    }

    /**
     * 启动线程, 监听事件通道, 一旦有新的事件到达, 立刻调用所有的 listener 处理该事件
     * 对该事件不感兴趣的 listener 会自动跳过, 对该该事件感兴趣的 listener 会作出相应的反应
     */
    override fun run() {
        while (true) {
            // 从事件队列取出一个事件
            val e: Event =
                try {
                    // 阻塞队列, 如果队列中没有事件, 则会阻塞在这里
                    eventQueue.take()
                } catch (ex: InterruptedException) {
                    ex.printStackTrace()
                    break
                }
            Log.d(TAG, "处理事件${e.javaClass.name}")

            val toDeleteQueue = LinkedBlockingQueue<String>()
            // 遍历 listener 处理该事件
            if (ids.containsKey(e.javaClass.kotlin)) {
                val tmp = ids[e.javaClass.kotlin]
                for (listenerId in ids[e.javaClass.kotlin]!!) {
                    val toDelete = listeners[listenerId]?.handle(e)
                    if (toDelete == true) {
                        toDeleteQueue.add(listenerId)
                    }
                    Log.d(TAG, "1个监听器正在处理：${listenerId}")
                }
            }
            for (listenerId in toDeleteQueue)
                removeListener(listenerId)
        }
    }
}