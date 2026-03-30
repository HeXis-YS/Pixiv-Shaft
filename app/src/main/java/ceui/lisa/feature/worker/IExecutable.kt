package ceui.lisa.feature.worker

interface IExecutable {

    fun onStart()

    fun run(end: IEnd)

    fun onEnd()
}
