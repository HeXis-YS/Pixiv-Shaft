package ceui.lisa.utils

object Dev {

    //是否是开发状态
    @JvmField
    var isDev = true

    @JvmField
    var refreshUser = false

    @JvmField
    var hideMainActivityStatus = true

    /**
     * 测试账号：哪位大佬，希望自己去注册账号，别改我密码，别改我密码，别改我密码
     *
     * 申请了N个测试账号了，好不容易造的数据，会被改密码，被改邮箱，然后登不上
     */
    const val USER_ACCOUNT = "user_shaft2"
    const val USER_PWD = "Mercis09bv"

    @JvmField
    var show_url_detail = false
}
