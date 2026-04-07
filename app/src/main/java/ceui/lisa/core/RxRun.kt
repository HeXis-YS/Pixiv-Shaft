package ceui.lisa.core

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers

object RxRun {

    @JvmStatic
    fun <T : Any, R : Any> runOn(runnable: RxRunnable<T>?, observer: Observer<R>, mapper: Function<T, R>) {
        if (runnable == null) {
            return
        }

        runnable.beforeExecute()
        val observable = Observable.create<T> { emitter ->
            try {
                val result = runnable.execute()
                emitter.onNext(result)
                emitter.onComplete()
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
        observable.subscribeOn(Schedulers.newThread())
            .map(mapper)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
    }

    @JvmStatic
    fun <T : Any> runOn(runnable: RxRunnable<T>?, observer: Observer<T>) {
        runOn(runnable, observer, Function { value -> value })
    }
}
