package ceui.loxia

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData


fun <T1, T2, S> combineLatest2(
    source1: LiveData<T1>,
    source2: LiveData<T2>,
    combine: (data1: T1?, data2: T2?) -> S,
) : LiveData<S> {
    val finalLiveData: MediatorLiveData<S> = MediatorLiveData()

    var data1: T1? = source1.value
    var data2: T2? = source2.value

    finalLiveData.addSource(source1) {
        data1 = it
        finalLiveData.value = combine(data1, data2)
    }
    finalLiveData.addSource(source2) {
        data2 = it
        finalLiveData.value = combine(data1, data2)
    }

    return finalLiveData
}

fun <T1, T2> combineLatest(
    source1: LiveData<T1>,
    source2: LiveData<T2>,
) : LiveData<Pair<T1?, T2?>> {
    return combineLatest2(source1, source2, ::Pair)
}
