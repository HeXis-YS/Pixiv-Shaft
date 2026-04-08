package ceui.lisa.http

abstract class NullCtrl<T : Any> : ErrorCtrl<T>() {

    abstract fun success(t: T)

    open fun nullSuccess() {
    }

    override fun next(t: T) {
        success(t)
        must(true)
    }

    override fun error(e: Throwable) {
        super.error(e)
        must(false)
    }

    open fun must(isSuccess: Boolean) {
    }
}
