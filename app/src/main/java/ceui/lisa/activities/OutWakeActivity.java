package ceui.lisa.activities;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import java.util.List;

import ceui.lisa.R;
import ceui.lisa.database.UserEntity;
import ceui.lisa.databinding.ActivityOutWakeBinding;
import ceui.lisa.feature.PkceUtil;
import ceui.lisa.fragments.FragmentLogin;
import ceui.lisa.http.NullCtrl;
import ceui.lisa.http.Retro;
import ceui.lisa.interfaces.Callback;
import ceui.lisa.models.UserModel;
import ceui.lisa.utils.Common;
import ceui.lisa.utils.Local;
import ceui.lisa.utils.Params;
import ceui.lisa.utils.PixivOperate;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static ceui.lisa.activities.Shaft.sUserModel;

public class OutWakeActivity extends BaseActivity<ActivityOutWakeBinding> {

    public static final String HOST_ME = "pixiv.me";
    public static final String HOST_PIXIVISION = "pixivision.net";
    private static final String PIXIV_IMAGE_HOST = "i.pximg.net";
    public static boolean isNetWorking = false;

    @Override
    protected int initLayout() {
        return R.layout.activity_out_wake;
    }

    @Override
    public boolean hideStatusBar() {
        return true;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            Uri uri = intent.getData();
            if (uri != null) {

                String scheme = uri.getScheme();
                if (!TextUtils.isEmpty(scheme)) {

                    if (uri.getPath() != null) {
                        if (uri.getPathSegments().contains("artworks") || uri.getPathSegments().contains("i")) {
                            if (isNetWorking) {
                                return;
                            }
                            isNetWorking = true;
                            List<String> pathArray = uri.getPathSegments();
                            String illustID = pathArray.get(pathArray.size() - 1);
                            if (!TextUtils.isEmpty(illustID)) {
                                PixivOperate.getIllustByID(Shaft.sUserModel, tryParseId(illustID), getMContext(), new Callback<Void>() {
                                    @Override
                                    public void doSomething(Void t) {
                                        finish();
                                    }
                                },null);
                                // finish(); // wait for callback
                                return;
                            }
                        }

                        if (uri.getPathSegments().contains("novel") && !TextUtils.isEmpty(uri.getQueryParameter("id"))
                                || uri.getPathSegments().contains("n")) {
                            if (isNetWorking) {
                                return;
                            }
                            isNetWorking = true;
                            String novelId;
                            if (uri.getPathSegments().contains("novel") && !TextUtils.isEmpty(uri.getQueryParameter("id"))) {
                                novelId = uri.getQueryParameter("id");
                            } else {
                                List<String> pathArray = uri.getPathSegments();
                                novelId = pathArray.get(pathArray.size() - 1);
                            }
                            PixivOperate.getNovelByID(sUserModel, tryParseId(novelId), getMContext(), new Callback<Void>() {
                                @Override
                                public void doSomething(Void t) {
                                    finish();
                                }
                            });
                            return;
                        }

                        if (uri.getPathSegments().contains("users") || uri.getPathSegments().contains("u")) {
                            List<String> pathArray = uri.getPathSegments();
                            String userID = pathArray.get(pathArray.size() - 1);
                            if (!TextUtils.isEmpty(userID)) {
                                Intent userIntent = new Intent(getMContext(), UActivity.class);
                                userIntent.putExtra(Params.USER_ID, Integer.valueOf(userID));
                                startActivity(userIntent);
                                finish();
                                return;
                            }
                        }
                    }


                    //http网页跳转到这里
                    if (scheme.contains("http")) {
                        try {
                            String uriString = uri.toString();
                            if (uriString.toLowerCase().contains(PIXIV_IMAGE_HOST)) {
                                int index = uriString.lastIndexOf("/");
                                String end = uriString.substring(index + 1);
                                String idString = end.split("_")[0];

                                Common.showLog("end " + end + " idString " + idString);
                                PixivOperate.getIllustByID(Shaft.sUserModel, tryParseId(idString), getMContext(), new Callback<Void>() {
                                    @Override
                                    public void doSomething(Void t) {
                                        finish();
                                    }
                                },null);
                                return;
                            } else if (uriString.toLowerCase().contains(HOST_ME)) {
                                startActivity(TemplateActivity.newWebIntent(getMContext(), HOST_ME, uriString));
                                finish();
                                return;
                            } else if (uriString.toLowerCase().contains(HOST_PIXIVISION)) {
                                startActivity(TemplateActivity.newWebIntent(
                                        getMContext(),
                                        getString(R.string.pixiv_special),
                                        uriString,
                                        true
                                ));
                                finish();
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        String illustID = uri.getQueryParameter("illust_id");
                        if (!TextUtils.isEmpty(illustID)) {
                            PixivOperate.getIllustByID(Shaft.sUserModel, tryParseId(illustID), getMContext(), new Callback<Void>() {
                                @Override
                                public void doSomething(Void t) {
                                    finish();
                                }
                            },null);
                            return;
                        }

                        String userID = uri.getQueryParameter("id");
                        if (!TextUtils.isEmpty(userID)) {
                            Intent userIntent = new Intent(getMContext(), UActivity.class);
                            userIntent.putExtra(Params.USER_ID, Integer.valueOf(userID));
                            startActivity(userIntent);
                            finish();
                            return;
                        }

                    }

                    //pixiv内部链接，如
                    //pixiv://illusts/73190863
                    //pixiv://account/login?code=BsQND5vc6uIWKIwLiDsh0S3h1yno6eVHDVMrX3fONgM&via=login
                    if (scheme.contains("pixiv") || scheme.contains("shaftintent")) {
                        String host = uri.getHost();


                        if (!TextUtils.isEmpty(host)) {

                            if (host.equals("account")) {
                                Common.showToast(getString(R.string.trying_login));
                                String code = uri.getQueryParameter("code");
                                Retro.getAccountApi().newLogin(
                                        FragmentLogin.CLIENT_ID,
                                        FragmentLogin.CLIENT_SECRET,
                                        FragmentLogin.AUTH_CODE,
                                        code,
                                        PkceUtil.getPkceItem().getVerify(),
                                        FragmentLogin.CALL_BACK,
                                        true
                                ).subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new NullCtrl<UserModel>() {
                                    @Override
                                    public void success(UserModel userModel) {

                                        Common.showLog(userModel.toString());
                                        Common.showToast("登录成功");

                                        userModel.getUser().setIs_login(true);
                                        Local.saveUser(userModel);

                                        UserEntity userEntity = new UserEntity();
                                        userEntity.setLoginTime(System.currentTimeMillis());
                                        userEntity.setUserID(userModel.getUser().getId());
                                        userEntity.setUserGson(Shaft.sGson.toJson(Local.getUser()));

                                        PixivOperate.insertUser(userEntity);

                                        // 检测是否打开R18并提示开启，新注册未验证邮箱用户不提示，严格来说只有设置过密码(has_password)才能进设置页，考虑到网页注册只能使用邮箱，故如此限制
                                        if (userModel.getUser().isR18Enabled() || !userModel.getUser().isIs_mail_authorized()) {
                                            getMActivity().finish();
                                            Common.restart();
                                        } else {
                                            new QMUIDialog.MessageDialogBuilder(getMActivity())
                                                    .setTitle(R.string.string_216)
                                                    .setMessage(R.string.string_400)
                                                    .setSkinManager(QMUISkinManager.defaultInstance(getMContext()))
                                                    .addAction(R.string.string_401, new QMUIDialogAction.ActionListener() {
                                                        @Override
                                                        public void onClick(QMUIDialog dialog, int index) {
                                                            dialog.dismiss();
                                                            getMActivity().finish();
                                                            Common.restart();
                                                        }
                                                    })
                                                    .addAction(R.string.string_402, new QMUIDialogAction.ActionListener() {
                                                        @Override
                                                        public void onClick(QMUIDialog dialog, int index) {
                                                            TemplateActivity.startWeb(getMContext(), null, Params.URL_R18_SETTING);
                                                        }
                                                    })
                                                    .create()
                                                    .show();
                                        }
                                    }
                                });
                                return;
                            }

                            if (host.contains("users")) {
                                String path = uri.getPath();
                                Intent userIntent = new Intent(getMContext(), UActivity.class);
                                userIntent.putExtra(Params.USER_ID, Integer.valueOf(path.substring(1)));
                                startActivity(userIntent);
                                finish();
                                return;
                            }

                            if (host.contains("illusts")) {
                                String path = uri.getPath();
                                PixivOperate.getIllustByID(Shaft.sUserModel, tryParseId(path.substring(1)),
                                        getMContext(), t -> finish(),null);
                                return;
                            }

                            if (host.contains("novels")) {
                                String path = uri.getPath();
                                PixivOperate.getNovelByID(Shaft.sUserModel, tryParseId(path.substring(1)),
                                        getMContext(), t -> finish());
                                return;
                            }
                        }
                    }
                }
            }
        }

        if (sUserModel != null && sUserModel.getUser().isIs_login()) {
            Intent i = new Intent(getMContext(), MainActivity.class);
            getMActivity().startActivity(i);
            getMActivity().finish();
        } else {
            TemplateActivity.startLogin(getMContext());
            finish();
        }
    }
}
