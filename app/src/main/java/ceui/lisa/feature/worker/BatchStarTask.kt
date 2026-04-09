package ceui.lisa.feature.worker

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ceui.lisa.activities.Shaft
import ceui.lisa.http.ErrorCtrl
import ceui.lisa.http.Retro
import ceui.lisa.models.NullResponse
import ceui.lisa.utils.Params
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class BatchStarTask(
    name: String?,
    private val illustID: Int,
    private val starType: Int,
) : AbstractTask() {
    init {
        taskName =
            if (starType == 0) {
                "添加收藏 $name"
            } else {
                "取消收藏 $name"
            }
    }

    override fun run(end: IEnd) {
        if (starType == 0) {
            Retro
                .getAppApi()
                .postLikeIllust(Shaft.sUserModel.access_token, illustID, Params.TYPE_PUBLIC)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    object : ErrorCtrl<NullResponse>() {
                        override fun next(nullResponse: NullResponse) {
                            val intent = Intent(Params.LIKED_ILLUST)
                            intent.putExtra(Params.ID, illustID)
                            intent.putExtra(Params.IS_LIKED, true)
                            LocalBroadcastManager.getInstance(Shaft.getContext()).sendBroadcast(intent)
                        }

                        override fun must() {
                            end.next()
                        }
                    },
                )
        } else {
            Retro
                .getAppApi()
                .postDislikeIllust(Shaft.sUserModel.access_token, illustID)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    object : ErrorCtrl<NullResponse>() {
                        override fun next(nullResponse: NullResponse) {
                            val intent = Intent(Params.LIKED_ILLUST)
                            intent.putExtra(Params.ID, illustID)
                            intent.putExtra(Params.IS_LIKED, false)
                            LocalBroadcastManager.getInstance(Shaft.getContext()).sendBroadcast(intent)
                        }

                        override fun must() {
                            end.next()
                        }
                    },
                )
        }
    }
}
