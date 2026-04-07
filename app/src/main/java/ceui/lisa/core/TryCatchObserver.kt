package ceui.lisa.core

import io.reactivex.Observer
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable

abstract class TryCatchObserver<T : Any> : Observer<T> {
    override fun onSubscribe(@NonNull d: Disposable) {
        try {
            subscribe(d)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNext(@NonNull t: T) {
        try {
            next(t)
            must()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onError(@NonNull e: Throwable) {
        try {
            error(e)
            must()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onComplete() {
        try {
            complete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    abstract fun subscribe(d: Disposable)

    abstract fun next(t: T)

    abstract fun error(e: Throwable)

    abstract fun complete()

    open fun must() {
    }
}
