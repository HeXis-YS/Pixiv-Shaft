package ceui.lisa.utils

import com.blankj.utilcode.util.NetworkUtils
import ceui.lisa.R
import ceui.lisa.activities.Shaft

class DownloadLimitTypeUtil private constructor() {
    companion object {
        @JvmField
        val DOWNLOAD_START_TYPE_IDS = intArrayOf(
            R.string.string_289,
            R.string.string_448,
            R.string.string_453
        )

        @JvmStatic
        fun getCurrentStatusIndex(): Int {
            var currentIndex = Shaft.sSettings.downloadLimitType
            if (currentIndex < 0 || currentIndex >= DOWNLOAD_START_TYPE_IDS.size) {
                currentIndex = 0
            }
            return currentIndex
        }

        /**
         * 创建任务时自动开始任务
         */
        @JvmStatic
        fun startTaskWhenCreate(): Boolean {
            return Shaft.sSettings.downloadLimitType == 0 ||
                (Shaft.sSettings.downloadLimitType == 1 && NetworkUtils.isWifiConnected())
        }

        /**
         * 目前是否可以下载
         */
        @JvmStatic
        fun canDownloadNow(): Boolean {
            return Shaft.sSettings.downloadLimitType == 0 ||
                (Shaft.sSettings.downloadLimitType != 0 && NetworkUtils.isWifiConnected())
        }
    }
}
