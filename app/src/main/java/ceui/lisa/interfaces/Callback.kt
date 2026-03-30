package ceui.lisa.interfaces

fun interface Callback<Target> {
    fun doSomething(t: Target)
}
