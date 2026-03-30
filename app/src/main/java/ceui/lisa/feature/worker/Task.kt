package ceui.lisa.feature.worker

class Task : AbstractTask() {

    override fun run(end: IEnd) {
        try {
            println("$name 开始工作 ")
            Thread.sleep(1000L)
            end.next()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
