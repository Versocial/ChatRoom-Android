package vsper.eventChannel

import java.util.EventListener
import kotlin.reflect.KClass

/**
 * 事件监听器的接口, 每个监听器必须实现一个针对 event 的处理函数
 */
interface EventListener : EventListener {
    fun tClass(): KClass<*>
    fun handle(event: Event): Boolean
}