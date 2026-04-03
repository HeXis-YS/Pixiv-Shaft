package ceui.lisa.core

abstract class RxRunnable<T> {

    /**
     * 新线程执行
     *
     * @return 异步操作的结果
     * @throws Exception ex
     */
    @Throws(Exception::class)
    abstract fun execute(): T

    /**
     * 主线程执行
     */
    open fun beforeExecute() {}
}
