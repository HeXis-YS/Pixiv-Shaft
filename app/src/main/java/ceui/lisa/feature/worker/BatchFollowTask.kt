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

class BatchFollowTask(
    name: String?,
    private val userID: Int,
    private val starType: Int,
) : AbstractTask() {
    init {
        taskName =
            if (starType == 0) {
                "添加关注 $name"
            } else {
                "取消关注 $name"
            }
    }

    override fun run(end: IEnd) {
        if (starType == 0) {
            Retro
                .getAppApi()
                .postFollow(Shaft.sUserModel.access_token, userID, Params.TYPE_PUBLIC)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    object : ErrorCtrl<NullResponse>() {
                        override fun next(nullResponse: NullResponse) {
                            val intent = Intent(Params.LIKED_USER)
                            intent.putExtra(Params.ID, userID)
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
                .postUnFollow(Shaft.sUserModel.access_token, userID)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    object : ErrorCtrl<NullResponse>() {
                        override fun next(nullResponse: NullResponse) {
                            val intent = Intent(Params.LIKED_USER)
                            intent.putExtra(Params.ID, userID)
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
