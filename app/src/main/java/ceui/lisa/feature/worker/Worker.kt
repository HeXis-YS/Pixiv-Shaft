package ceui.lisa.feature.worker

import android.os.Handler
import ceui.lisa.interfaces.FeedBack
import java.util.ArrayList

class Worker private constructor() {
    private var runningTasks = ArrayList<AbstractTask>()
    private var feedBack: FeedBack? = null
    private var finalFeedBack: FeedBack? = null
    private var isRunning = false

    fun getRunningTask(): ArrayList<AbstractTask> = runningTasks

    fun setRunningTask(runningTask: ArrayList<AbstractTask>) {
        runningTasks = runningTask
    }

    fun addTask(task: AbstractTask) {
        println("添加任务 ${task.getName()}")
        runningTasks.add(task)
    }

    fun removeTask(task: AbstractTask) {
        runningTasks.remove(task)
    }

    fun removeTask(index: Int) {
        if (index < runningTasks.size) {
            runningTasks.removeAt(index)
        }
    }

    fun start() {
        if (!isRunning) {
            isRunning = true
            Thread(this::execute).start()
        }
    }

    private fun execute() {
        if (runningTasks.isEmpty()) {
            isRunning = false
            println("已完成")
            return
        }

        val current = runningTasks[0]
        current.process(
            IEnd {
                removeTask(current)
                feedBack?.doSomething()
                if (runningTasks.isEmpty()) {
                    finalFeedBack?.doSomething()
                    finalFeedBack = null
                }
                execute()
            },
        )
    }

    fun getFeedBack(): FeedBack? = feedBack

    fun setFeedBack(feedBack: FeedBack?) {
        this.feedBack = feedBack
    }

    fun getFinalFeedBack(): FeedBack? = finalFeedBack

    fun setFinalFeedBack(mFinalFeedBack: FeedBack?) {
        finalFeedBack = mFinalFeedBack
    }

    companion object {
        private val handler = Handler()
        private var worker: Worker? = null

        @JvmStatic
        fun get(): Worker {
            if (worker == null) {
                worker = Worker()
            }
            return worker!!
        }

        @JvmStatic
        fun getHandler(): Handler = handler
    }
}
