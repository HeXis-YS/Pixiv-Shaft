package ceui.lisa.feature.worker

import ceui.lisa.utils.Common

abstract class AbstractTask : IExecutable {
    protected var taskName: String? = null
    protected var taskDelay = 2000L
    protected var shouldDelay = true

    fun getName(): String? = taskName

    fun setName(name: String?) {
        taskName = name
    }

    fun process(end: IEnd) {
        Common.showLog("正在处理：$taskName")
        onStart()
        run(
            IEnd {
                onEnd()
                if (shouldDelay) {
                    Worker.getHandler().postDelayed({ end.next() }, taskDelay)
                } else {
                    end.next()
                }
            },
        )
    }

    override fun onStart() {
        Common.showLog("开始执行 $taskName")
    }

    override fun onEnd() {
        Common.showLog("执行结束 $taskName")
    }

    fun getDelay(): Long = taskDelay

    fun setDelay(delay: Long) {
        taskDelay = delay
    }

    fun isShouldDelay(): Boolean = shouldDelay

    fun setShouldDelay(shouldDelay: Boolean): AbstractTask {
        this.shouldDelay = shouldDelay
        return this
    }
}
