package ceui.lisa.model

import ceui.lisa.core.DownloadItem
import java.io.Serializable

class Holder : Serializable {
    var code: Int = 0
    var index: Int = 0
    var downloadItem: DownloadItem? = null
}
