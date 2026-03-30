package ceui.lisa.interfaces

fun interface Display<Data> {
    fun invoke(data: Data)
}
