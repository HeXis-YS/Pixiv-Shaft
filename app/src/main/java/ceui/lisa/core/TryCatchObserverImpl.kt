package ceui.lisa.core

import io.reactivex.disposables.Disposable

open class TryCatchObserverImpl<T : Any> : TryCatchObserver<T>() {
    override fun subscribe(d: Disposable) {
    }

    override fun next(t: T) {
    }

    override fun error(e: Throwable) {
    }

    override fun complete() {
    }
}
