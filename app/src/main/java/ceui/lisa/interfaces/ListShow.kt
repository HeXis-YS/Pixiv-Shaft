package ceui.lisa.interfaces

interface ListShow<Item> {
    val list: List<Item>

    val nextUrl: String?
}
