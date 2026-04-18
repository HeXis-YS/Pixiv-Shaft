package ceui.lisa.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import ceui.lisa.R
import ceui.lisa.activities.BaseActivity
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.databinding.FragmentSettingsBinding
import ceui.lisa.download.IllustDownload
import ceui.lisa.file.LegacyFile
import ceui.lisa.helper.NavigationLocationHelper
import ceui.lisa.helper.PageTransformerHelper
import ceui.lisa.helper.ThemeHelper
import ceui.lisa.interfaces.Callback
import ceui.lisa.utils.BackupUtils
import ceui.lisa.utils.Common
import ceui.lisa.utils.DownloadLimitTypeUtil
import ceui.lisa.utils.Local
import ceui.lisa.utils.Params
import ceui.lisa.utils.PixivSearchParamUtil
import ceui.lisa.utils.Settings
import ceui.lisa.utils.UserFolderNameUtil
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LanguageUtils
import com.blankj.utilcode.util.UriUtils
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.Arrays
import java.util.Locale

class FragmentSettings : BaseLazyFragment<FragmentSettingsBinding>() {
    companion object {
        private const val REQUEST_WRITE_STORAGE = 1002
    }

    override fun initLayout() {
        mLayoutID = R.layout.fragment_settings
    }

    override fun initData() {
        baseBind.toolbar.setNavigationOnClickListener { mActivity.finish() }
        Common.animate(baseBind.parentLinear)

        run {
            baseBind.userManage.setOnClickListener {
                TemplateActivity.startLocalUsers(mContext)
            }

            baseBind.editAccount.setOnClickListener {
                TemplateActivity.startBindEmail(mContext)
            }

            baseBind.r18Space.setOnClickListener {
                TemplateActivity.startWeb(mContext, null, Params.URL_R18_SETTING)
            }

            baseBind.premiumSpace.setOnClickListener {
                TemplateActivity.startWeb(mContext, null, Params.URL_PREMIUM_SETTING)
            }

            baseBind.loginOut.setOnClickListener {
                val builder = QMUIDialog.CheckBoxMessageDialogBuilder(activity)
                builder
                    .setTitle(getString(R.string.string_185))
                    .setMessage(getString(R.string.string_186))
                    .setChecked(true)
                    .setSkinManager(QMUISkinManager.defaultInstance(mContext))
                    .addAction(
                        getString(R.string.string_187),
                        object : QMUIDialogAction.ActionListener {
                            override fun onClick(dialog: QMUIDialog, index: Int) {
                                dialog.dismiss()
                            }
                        },
                    )
                    .addAction(
                        R.string.login_out,
                        object : QMUIDialogAction.ActionListener {
                            override fun onClick(dialog: QMUIDialog, index: Int) {
                                Common.logOut(mContext, builder.isChecked)
                                mActivity.finish()
                                dialog.dismiss()
                            }
                        },
                    )
                    .create()
                    .show()
            }
        }

        run {
            baseBind.showLargeThumbnailImage.isChecked = Shaft.sSettings.isShowLargeThumbnailImage()
            baseBind.showLargeThumbnailImage.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    Shaft.sSettings.setShowLargeThumbnailImage(isChecked)
                    Common.showToast(getString(R.string.string_428))
                    Local.setSettings(Shaft.sSettings)
                },
            )
            baseBind.showLargeThumbnailImageRela.setOnClickListener {
                baseBind.showLargeThumbnailImage.performClick()
            }

            baseBind.showOriginalPreviewImage.isChecked = Shaft.sSettings.isShowOriginalPreviewImage()
            baseBind.showOriginalPreviewImage.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    Shaft.sSettings.setShowOriginalPreviewImage(isChecked)
                    Common.showToast(getString(R.string.string_428))
                    Local.setSettings(Shaft.sSettings)
                },
            )
            baseBind.showOriginalPreviewImageRela.setOnClickListener {
                baseBind.showOriginalPreviewImage.performClick()
            }
        }

        run {
            baseBind.saveHistory.isChecked = Shaft.sSettings.isSaveViewHistory()
            baseBind.saveHistory.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    Shaft.sSettings.setSaveViewHistory(isChecked)
                    Common.showToast(getString(R.string.string_428), 2)
                    Local.setSettings(Shaft.sSettings)
                },
            )
            baseBind.saveHistoryRela.setOnClickListener {
                baseBind.saveHistory.performClick()
            }

            baseBind.deleteStarIllust.isChecked = Shaft.sSettings.isDeleteStarIllust
            baseBind.deleteStarIllust.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    Shaft.sSettings.isDeleteStarIllust = isChecked
                    Common.showToast(getString(R.string.string_428), 2)
                    Local.setSettings(Shaft.sSettings)
                },
            )
            baseBind.deleteStarIllustRela.setOnClickListener {
                baseBind.deleteStarIllust.performClick()
            }

            baseBind.toastDownloadResult.isChecked = Shaft.sSettings.isToastDownloadResult()
            baseBind.toastDownloadResult.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    Shaft.sSettings.setToastDownloadResult(isChecked)
                    Common.showToast(getString(R.string.string_428), 2)
                    Local.setSettings(Shaft.sSettings)
                },
            )
            baseBind.toastDownloadResultRela.setOnClickListener {
                baseBind.toastDownloadResult.performClick()
            }

            baseBind.searchFilter.text =
                PixivSearchParamUtil.getSizeName(Shaft.sSettings.getSearchFilter())
            baseBind.searchFilterRela.setOnClickListener {
                QMUIDialog.CheckableDialogBuilder(mContext)
                    .setCheckedIndex(
                        PixivSearchParamUtil.getSizeIndex(Shaft.sSettings.getSearchFilter()),
                    )
                    .setSkinManager(QMUISkinManager.defaultInstance(mContext))
                    .addItems(
                        PixivSearchParamUtil.ALL_SIZE_NAME,
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, which: Int) {
                                Shaft.sSettings.setSearchFilter(PixivSearchParamUtil.ALL_SIZE_VALUE[which])
                                Common.showToast(getString(R.string.string_428), 2)
                                Local.setSettings(Shaft.sSettings)
                                baseBind.searchFilter.text = PixivSearchParamUtil.ALL_SIZE_NAME[which]
                                dialog.dismiss()
                            }
                        },
                    )
                    .create()
                    .show()
            }

            baseBind.searchDefaultSortType.text =
                PixivSearchParamUtil.getSortTypeName(Shaft.sSettings.searchDefaultSortType)
            baseBind.searchDefaultSortTypeRela.setOnClickListener {
                QMUIDialog.CheckableDialogBuilder(mContext)
                    .setCheckedIndex(
                        PixivSearchParamUtil.getSortTypeIndex(Shaft.sSettings.searchDefaultSortType),
                    )
                    .setSkinManager(QMUISkinManager.defaultInstance(mContext))
                    .addItems(
                        PixivSearchParamUtil.SORT_TYPE_NAME,
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, which: Int) {
                                Shaft.sSettings.searchDefaultSortType =
                                    PixivSearchParamUtil.SORT_TYPE_VALUE[which]
                                Common.showToast(getString(R.string.string_428), 2)
                                Local.setSettings(Shaft.sSettings)
                                baseBind.searchDefaultSortType.text =
                                    PixivSearchParamUtil.SORT_TYPE_NAME[which]
                                dialog.dismiss()
                            }
                        },
                    )
                    .create()
                    .show()
            }

            baseBind.filterComment.isChecked = Shaft.sSettings.isFilterComment
            baseBind.filterComment.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    Shaft.sSettings.isFilterComment = isChecked
                    Common.showToast(getString(R.string.string_428), 2)
                    Local.setSettings(Shaft.sSettings)
                },
            )
            baseBind.filterCommentRela.setOnClickListener {
                baseBind.filterComment.performClick()
            }

            baseBind.r18FilterDefaultEnable.isChecked = Shaft.sSettings.isR18FilterDefaultEnable()
            baseBind.r18FilterDefaultEnable.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    Shaft.sSettings.setR18FilterDefaultEnable(isChecked)
                    Common.showToast(getString(R.string.string_428), 2)
                    Local.setSettings(Shaft.sSettings)
                },
            )
            baseBind.r18FilterDefaultEnableRela.setOnClickListener {
                baseBind.r18FilterDefaultEnable.performClick()
            }
        }

        run {
            baseBind.mainViewR18.isChecked = Shaft.sSettings.isMainViewR18()
            baseBind.mainViewR18.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    Shaft.sSettings.setMainViewR18(isChecked)
                    Common.showToast(getString(R.string.please_restart_app), 2)
                    Local.setSettings(Shaft.sSettings)
                },
            )
            baseBind.mainViewR18Rela.setOnClickListener {
                baseBind.mainViewR18.performClick()
            }

            val navigationInitPositionSettingValue = Shaft.sSettings.getNavigationInitPosition()
            val navigationInitPosition =
                if (!TextUtils.isEmpty(navigationInitPositionSettingValue)) {
                    navigationInitPositionSettingValue
                } else {
                    NavigationLocationHelper.TUIJIAN
                }
            baseBind.navigationInitPosition.text =
                NavigationLocationHelper.SETTING_NAME_MAP[navigationInitPosition]
            baseBind.navigationInitPositionRela.setOnClickListener {
                val optionValues = NavigationLocationHelper.SETTING_NAME_MAP.keys.toTypedArray()
                val optionNames = NavigationLocationHelper.SETTING_NAME_MAP.values.toTypedArray()
                val settingValue = Shaft.sSettings.getNavigationInitPosition()
                val currentNavigationInitPosition =
                    if (!TextUtils.isEmpty(settingValue)) settingValue else NavigationLocationHelper.TUIJIAN
                val index = Arrays.asList(*optionValues).indexOf(currentNavigationInitPosition)
                QMUIDialog.CheckableDialogBuilder(mActivity)
                    .setCheckedIndex(index)
                    .setSkinManager(QMUISkinManager.defaultInstance(mContext))
                    .addItems(
                        optionNames,
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, which: Int) {
                                if (which != index) {
                                    Shaft.sSettings.setNavigationInitPosition(optionValues[which])
                                    baseBind.navigationInitPosition.text = optionNames[which]
                                    Local.setSettings(Shaft.sSettings)
                                }
                                dialog.dismiss()
                            }
                        },
                    )
                    .show()
            }

            baseBind.themeMode.text = Shaft.sSettings.getThemeType().toDisplayString(mContext)
            baseBind.themeModeRela.setOnClickListener {
                val index = Shaft.sSettings.getThemeType().themeTypeIndex
                val themeModes =
                    arrayOf(
                        ThemeHelper.ThemeType.DEFAULT_MODE,
                        ThemeHelper.ThemeType.LIGHT_MODE,
                        ThemeHelper.ThemeType.DARK_MODE,
                    )
                val themeNames =
                    arrayOf(
                        themeModes[0].toDisplayString(mContext),
                        themeModes[1].toDisplayString(mContext),
                        themeModes[2].toDisplayString(mContext),
                    )
                QMUIDialog.CheckableDialogBuilder(mActivity)
                    .setCheckedIndex(index)
                    .setSkinManager(QMUISkinManager.defaultInstance(mContext))
                    .addItems(
                        themeNames,
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, which: Int) {
                                if (which != index) {
                                    Shaft.sSettings.setThemeType(mActivity as AppCompatActivity, themeModes[which])
                                    baseBind.themeMode.text = themeNames[which]
                                    Local.setSettings(Shaft.sSettings)
                                }
                                dialog.dismiss()
                            }
                        },
                    )
                    .show()
            }

            setThemeName()
            baseBind.colorSelectRela.setOnClickListener {
                TemplateActivity.startThemeColors(mContext)
            }

            baseBind.lineCount.text = getString(R.string.string_349, Shaft.sSettings.lineCount)
            baseBind.lineCountRela.setOnClickListener {
                var index = 0
                if (Shaft.sSettings.lineCount == 3) {
                    index = 1
                } else if (Shaft.sSettings.lineCount == 4) {
                    index = 2
                }
                val lineCountNames =
                    arrayOf(
                        getString(R.string.string_349, 2),
                        getString(R.string.string_349, 3),
                        getString(R.string.string_349, 4),
                    )
                val selectIndex = index
                QMUIDialog.CheckableDialogBuilder(mActivity)
                    .setCheckedIndex(selectIndex)
                    .setSkinManager(QMUISkinManager.defaultInstance(mContext))
                    .addItems(
                        lineCountNames,
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, which: Int) {
                                if (which != selectIndex) {
                                    val lineCount = which + 2
                                    Shaft.sSettings.lineCount = lineCount
                                    baseBind.lineCount.text = getString(R.string.string_349, lineCount)
                                    Local.setSettings(Shaft.sSettings)
                                    Common.showToast(getString(R.string.please_restart_app), 2)
                                }
                                dialog.dismiss()
                            }
                        },
                    )
                    .show()
            }

            setOrderName()
            baseBind.orderSelect.setOnClickListener {
                val index = Shaft.sSettings.getBottomBarOrder()
                val orderNames =
                    arrayOf(
                        getString(R.string.string_343),
                        getString(R.string.string_344),
                        getString(R.string.string_345),
                        getString(R.string.string_346),
                        getString(R.string.string_347),
                        getString(R.string.string_348),
                    )
                QMUIDialog.CheckableDialogBuilder(mActivity)
                    .setCheckedIndex(index)
                    .setSkinManager(QMUISkinManager.defaultInstance(mContext))
                    .addItems(
                        orderNames,
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, which: Int) {
                                if (which == index) {
                                    Common.showLog("什么也不做")
                                } else {
                                    Shaft.sSettings.setBottomBarOrder(which)
                                    baseBind.orderSelect.text = orderNames[which]
                                    Local.setSettings(Shaft.sSettings)
                                    Common.showToast(getString(R.string.please_restart_app))
                                }
                                dialog.dismiss()
                            }
                        },
                    )
                    .show()
            }
            baseBind.bottomBarOrderRela.setOnClickListener {
                baseBind.orderSelect.performClick()
            }

            baseBind.appLanguage.text = Shaft.sSettings.appLanguage
            baseBind.appLanguageRela.setOnClickListener {
                QMUIDialog.CheckableDialogBuilder(activity)
                    .addItems(
                        Settings.ALL_LANGUAGE,
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, which: Int) {
                                Shaft.sSettings.appLanguage = Settings.ALL_LANGUAGE[which]
                                baseBind.appLanguage.text = Settings.ALL_LANGUAGE[which]
                                Common.showToast(getString(R.string.string_428), 2)
                                Local.setSettings(Shaft.sSettings)
                                when (which) {
                                    0 -> LanguageUtils.applyLanguage(Locale.SIMPLIFIED_CHINESE, true)
                                    1 -> LanguageUtils.applyLanguage(Locale.JAPAN, true)
                                    2 -> LanguageUtils.applyLanguage(Locale.US, true)
                                    3 -> LanguageUtils.applyLanguage(Locale.TRADITIONAL_CHINESE, true)
                                    4 -> LanguageUtils.applyLanguage(Locale("RU", "ru", ""), true)
                                    5 -> LanguageUtils.applyLanguage(Locale.KOREA, true)
                                }
                                dialog.dismiss()
                            }
                        },
                    )
                    .show()
            }
        }

        run {
            baseBind.r18DivideSave.isChecked = Shaft.sSettings.isR18DivideSave
            baseBind.r18DivideSave.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    Shaft.sSettings.isR18DivideSave = isChecked
                    Common.showToast(getString(R.string.string_428))
                    Local.setSettings(Shaft.sSettings)
                },
            )
            baseBind.r18DivideSaveRela.setOnClickListener {
                baseBind.r18DivideSave.performClick()
            }

            baseBind.aiDivideSave.isChecked = Shaft.sSettings.isAIDivideSave
            baseBind.aiDivideSave.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    Shaft.sSettings.isAIDivideSave = isChecked
                    Common.showToast(getString(R.string.string_428))
                    Local.setSettings(Shaft.sSettings)
                },
            )
            baseBind.aiDivideSaveRela.setOnClickListener {
                baseBind.aiDivideSave.performClick()
            }

            baseBind.fileNameRela.setOnClickListener {
                TemplateActivity.startFileNameSettings(mContext)
            }

            baseBind.saveForSeparateAuthor.text = UserFolderNameUtil.getCurrentStatusName()
            baseBind.saveForSeparateAuthor.setOnClickListener {
                QMUIDialog.CheckableDialogBuilder(mActivity)
                    .setCheckedIndex(Shaft.sSettings.getSaveForSeparateAuthorStatus())
                    .setSkinManager(QMUISkinManager.defaultInstance(mContext))
                    .addItems(
                        UserFolderNameUtil.USER_FOLDER_NAME_NAMES,
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, which: Int) {
                                if (which == Shaft.sSettings.getSaveForSeparateAuthorStatus()) {
                                    Common.showLog("什么也不做")
                                } else {
                                    Shaft.sSettings.setSaveForSeparateAuthorStatus(which)
                                    baseBind.saveForSeparateAuthor.text = UserFolderNameUtil.getCurrentStatusName()
                                    Local.setSettings(Shaft.sSettings)
                                }
                                dialog.dismiss()
                            }
                        },
                    )
                    .show()
            }
            baseBind.saveForSeparateAuthorRela.setOnClickListener {
                baseBind.saveForSeparateAuthor.performClick()
            }

            baseBind.illustLongPressDownload.isChecked = Shaft.sSettings.isIllustLongPressDownload()
            baseBind.illustLongPressDownload.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    Shaft.sSettings.setIllustLongPressDownload(isChecked)
                    Common.showToast(getString(R.string.please_restart_app))
                    Local.setSettings(Shaft.sSettings)
                },
            )
            baseBind.illustLongPressDownloadRela.setOnClickListener {
                baseBind.illustLongPressDownload.performClick()
            }

            val downloadStartTypeNames =
                arrayOf(
                    getString(DownloadLimitTypeUtil.DOWNLOAD_START_TYPE_IDS[0]),
                    getString(DownloadLimitTypeUtil.DOWNLOAD_START_TYPE_IDS[1]),
                    getString(DownloadLimitTypeUtil.DOWNLOAD_START_TYPE_IDS[2]),
                )
            baseBind.downloadLimitType.text =
                downloadStartTypeNames[DownloadLimitTypeUtil.getCurrentStatusIndex()]
            baseBind.downloadLimitType.setOnClickListener {
                QMUIDialog.CheckableDialogBuilder(mActivity)
                    .setCheckedIndex(Shaft.sSettings.downloadLimitType)
                    .setSkinManager(QMUISkinManager.defaultInstance(mContext))
                    .addItems(
                        downloadStartTypeNames,
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, which: Int) {
                                if (which == Shaft.sSettings.downloadLimitType) {
                                    Common.showLog("什么也不做")
                                } else {
                                    Shaft.sSettings.downloadLimitType = which
                                    baseBind.downloadLimitType.text =
                                        downloadStartTypeNames[DownloadLimitTypeUtil.getCurrentStatusIndex()]
                                    Common.showToast(getString(R.string.string_428))
                                    Local.setSettings(Shaft.sSettings)
                                }
                                dialog.dismiss()
                            }
                        },
                    )
                    .show()
            }
            baseBind.downloadLimitTypeRela.setOnClickListener {
                baseBind.downloadLimitType.performClick()
            }

            val downloadWays =
                arrayOf(
                    getString(R.string.string_363),
                    getString(R.string.string_364),
                )
            baseBind.downloadWay.text = downloadWays[Shaft.sSettings.getDownloadWay()]
            baseBind.downloadWayRela.setOnClickListener {
                QMUIDialog.CheckableDialogBuilder(mActivity)
                    .setCheckedIndex(Shaft.sSettings.getDownloadWay())
                    .setSkinManager(QMUISkinManager.defaultInstance(mContext))
                    .addItems(
                        downloadWays,
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, which: Int) {
                                if (which == Shaft.sSettings.getDownloadWay()) {
                                    Common.showLog("什么也不做")
                                } else {
                                    Shaft.sSettings.setDownloadWay(which)
                                    baseBind.downloadWay.text = downloadWays[which]
                                    Local.setSettings(Shaft.sSettings)
                                    updateIllustPathUI()
                                }
                                dialog.dismiss()
                            }
                        },
                    )
                    .show()
            }

            updateIllustPathUI()
            if (mActivity is BaseActivity<*>) {
                (mActivity as BaseActivity<*>).setFeedBack {
                    updateIllustPathUI()
                }
            }
            baseBind.singleIllustPath.setOnClickListener {
                if (Shaft.sSettings.getDownloadWay() == 0) {
                    Common.showToast(getString(R.string.string_329), true)
                } else {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    if (!TextUtils.isEmpty(Shaft.sSettings.rootPathUri) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val start = Uri.parse(Shaft.sSettings.rootPathUri)
                        intent.putExtra(android.provider.DocumentsContract.EXTRA_INITIAL_URI, start)
                    }
                    mActivity.startActivityForResult(intent, BaseActivity.ASK_URI)
                }
            }

            baseBind.novelPath.text = Settings.FILE_PATH_NOVEL
            baseBind.novelPathRela.setOnClickListener {
                Common.showToast(getString(R.string.string_374), true)
            }
        }

        run {
            baseBind.showLikeButton.isChecked = Shaft.sSettings.isPrivateStar
            baseBind.showLikeButton.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    Shaft.sSettings.isPrivateStar = isChecked
                    Common.showToast(getString(R.string.string_428), 2)
                    Local.setSettings(Shaft.sSettings)
                },
            )
            baseBind.showLikeButtonRela.setOnClickListener {
                baseBind.showLikeButton.performClick()
            }

            baseBind.hideStarBar.isChecked = Shaft.sSettings.isHideStarButtonAtMyCollection
            baseBind.hideStarBar.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    Shaft.sSettings.isHideStarButtonAtMyCollection = isChecked
                    Common.showToast(getString(R.string.string_428))
                    Local.setSettings(Shaft.sSettings)
                },
            )
            baseBind.hideStarBarRela.setOnClickListener {
                baseBind.hideStarBar.performClick()
            }

            baseBind.selectAllTag.isChecked = Shaft.sSettings.isStarWithTagSelectAll()
            baseBind.selectAllTag.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    Shaft.sSettings.setStarWithTagSelectAll(isChecked)
                    Common.showToast(getString(R.string.string_428))
                    Local.setSettings(Shaft.sSettings)
                },
            )
            baseBind.selectAllTagRela.setOnClickListener {
                baseBind.selectAllTag.performClick()
            }

            val transformerNames = PageTransformerHelper.getTransformerNames()
            baseBind.transformType.text =
                transformerNames[PageTransformerHelper.getCurrentTransformerIndex()]
            baseBind.transformTypeRela.setOnClickListener {
                QMUIDialog.CheckableDialogBuilder(mActivity)
                    .setCheckedIndex(PageTransformerHelper.getCurrentTransformerIndex())
                    .setSkinManager(QMUISkinManager.defaultInstance(mContext))
                    .addItems(
                        transformerNames,
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, which: Int) {
                                if (which != PageTransformerHelper.getCurrentTransformerIndex()) {
                                    PageTransformerHelper.setCurrentTransformer(which)
                                    baseBind.transformType.text = transformerNames[which]
                                    Local.setSettings(Shaft.sSettings)
                                }
                                dialog.dismiss()
                            }
                        },
                    )
                    .show()
            }

            baseBind.showRelatedWhenStar.isChecked = Shaft.sSettings.isShowRelatedWhenStar()
            baseBind.showRelatedWhenStar.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    Shaft.sSettings.setShowRelatedWhenStar(isChecked)
                    Common.showToast(getString(R.string.please_restart_app))
                    Local.setSettings(Shaft.sSettings)
                },
            )
            baseBind.showRelatedWhenStarRela.setOnClickListener {
                baseBind.showRelatedWhenStar.performClick()
            }

            baseBind.globalSwipeBack.isChecked = Shaft.sSettings.isGlobalSwipeBack()
            baseBind.globalSwipeBack.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    Shaft.sSettings.setGlobalSwipeBack(isChecked)
                    Common.showToast(getString(R.string.please_restart_app))
                    Local.setSettings(Shaft.sSettings)
                },
            )
            baseBind.globalSwipeBackRela.setOnClickListener {
                baseBind.globalSwipeBack.performClick()
            }

            baseBind.downloadAutoPostLike.isChecked = Shaft.sSettings.isAutoPostLikeWhenDownload
            baseBind.downloadAutoPostLike.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    Shaft.sSettings.isAutoPostLikeWhenDownload = isChecked
                    Common.showToast(getString(R.string.string_428))
                    Local.setSettings(Shaft.sSettings)
                },
            )
            baseBind.downloadAutoPostLikeRela.setOnClickListener {
                baseBind.downloadAutoPostLike.performClick()
            }

            baseBind.illustDetailKeepScreenOn.isChecked = Shaft.sSettings.isIllustDetailKeepScreenOn
            baseBind.illustDetailKeepScreenOn.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    Shaft.sSettings.isIllustDetailKeepScreenOn = isChecked
                    Common.showToast(getString(R.string.string_428))
                    Local.setSettings(Shaft.sSettings)
                },
            )
            baseBind.illustDetailKeepScreenOnRela.setOnClickListener {
                baseBind.illustDetailKeepScreenOn.performClick()
            }
        }

        run {
            baseBind.imageCacheSize.text = FileUtils.getSize(LegacyFile.imageCacheFolder(mContext))
            baseBind.clearImageCache.setOnClickListener {
                FileUtils.deleteAllInDir(LegacyFile.imageCacheFolder(mContext))
                Common.showToast(getString(R.string.success_clearImageCache))
                baseBind.imageCacheSize.text = FileUtils.getSize(LegacyFile.imageCacheFolder(mContext))
            }

            baseBind.gifCacheSize.text = FileUtils.getSize(LegacyFile.gifCacheFolder(mContext))
            baseBind.clearGifCache.setOnClickListener {
                FileUtils.deleteAllInDir(LegacyFile.gifCacheFolder(mContext))
                Common.showToast(getString(R.string.success_clearGifCache), 2)
                baseBind.gifCacheSize.text = FileUtils.getSize(LegacyFile.gifCacheFolder(mContext))
            }
        }

        run {
            baseBind.backupRela.setOnClickListener {
                val builder = QMUIDialog.CheckBoxMessageDialogBuilder(activity)
                builder
                    .setTitle(getString(R.string.string_420))
                    .setMessage(getString(R.string.string_423))
                    .setSkinManager(QMUISkinManager.defaultInstance(mContext))
                    .addAction(
                        getString(R.string.string_187),
                        object : QMUIDialogAction.ActionListener {
                            override fun onClick(dialog: QMUIDialog, index: Int) {
                                dialog.dismiss()
                            }
                        },
                    )
                    .addAction(
                        R.string.sure,
                        object : QMUIDialogAction.ActionListener {
                            override fun onClick(dialog: QMUIDialog, index: Int) {
                                val backupString = BackupUtils.getBackupString(mContext, builder.isChecked)
                                IllustDownload.downloadBackupFile(
                                    mActivity as BaseActivity<*>,
                                    "Shaft-Backup.json",
                                    backupString,
                                    object : Callback<Uri> {
                                        override fun doSomething(t: Uri) {
                                            Common.showToast(getString(R.string.backup_success) + Settings.FILE_PATH_BACKUP)
                                        }
                                    },
                                )
                                dialog.dismiss()
                            }
                        },
                    )
                    .create()
                    .show()
            }

            baseBind.restoreRela.setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val backupFileUri =
                        Uri.parse("content://com.android.externalstorage.documents/document/primary:Download%2fShaftBackups%2fShaft-Backup.json")
                    intent.putExtra(android.provider.DocumentsContract.EXTRA_INITIAL_URI, backupFileUri)
                }
                startActivityForResult(intent, Params.REQUEST_CODE_CHOOSE)
            }
        }

        if (!Common.isAndroidQ()) {
            requestStoragePermission()
        }
    }

    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_STORAGE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != REQUEST_WRITE_STORAGE) {
            return
        }
        if (grantResults.isEmpty() || grantResults[0] != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Common.showToast(getString(R.string.access_denied))
            finish()
        }
    }

    private fun setOrderName() {
        val index = Shaft.sSettings.getBottomBarOrder()
        val orderNames =
            arrayOf(
                getString(R.string.string_343),
                getString(R.string.string_344),
                getString(R.string.string_345),
                getString(R.string.string_346),
                getString(R.string.string_347),
                getString(R.string.string_348),
            )
        baseBind.orderSelect.text = orderNames[index]
    }

    private fun setThemeName() {
        val index = Shaft.sSettings.themeIndex
        baseBind.colorSelect.text = getString(FragmentColors.COLOR_NAME_CODES[index])
    }

    private fun updateIllustPathUI() {
        if (Shaft.sSettings.getDownloadWay() == 1) {
            try {
                baseBind.illustPath.text = URLDecoder.decode(Shaft.sSettings.rootPathUri, "utf-8")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        } else {
            baseBind.illustPath.text = Shaft.sSettings.getIllustPath()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        @Nullable data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Params.REQUEST_CODE_CHOOSE && resultCode == RESULT_OK && data != null) {
            try {
                val uri = data.data
                val fileString = String(UriUtils.uri2Bytes(uri))
                val restoreResult = BackupUtils.restoreBackups(mContext, fileString)
                Common.showToast(
                    if (restoreResult) getString(R.string.restore_success) else getString(R.string.restore_failed),
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
