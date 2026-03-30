package ceui.lisa.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.ArrayList;
import java.util.List;

import ceui.lisa.R;
import ceui.lisa.databinding.ActivityFragmentBinding;
import ceui.lisa.fragments.FragmentAboutApp;
import ceui.lisa.fragments.FragmentBookedTag;
import ceui.lisa.fragments.FragmentCollection;
import ceui.lisa.fragments.FragmentColors;
import ceui.lisa.fragments.FragmentComment;
import ceui.lisa.fragments.FragmentDownload;
import ceui.lisa.fragments.FragmentEditAccount;
import ceui.lisa.fragments.FragmentFeature;
import ceui.lisa.fragments.FragmentFileName;
import ceui.lisa.fragments.FragmentFollowUser;
import ceui.lisa.fragments.FragmentHistory;
import ceui.lisa.fragments.FragmentImageDetail;
import ceui.lisa.fragments.FragmentLikeIllust;
import ceui.lisa.fragments.FragmentLikeNovel;
import ceui.lisa.fragments.FragmentListSimpleUser;
import ceui.lisa.fragments.FragmentLocalUsers;
import ceui.lisa.fragments.FragmentLogin;
import ceui.lisa.fragments.FragmentMangaSeries;
import ceui.lisa.fragments.FragmentMangaSeriesDetail;
import ceui.lisa.fragments.FragmentMultiDownload;
import ceui.lisa.fragments.FragmentNew;
import ceui.lisa.fragments.FragmentNewNovel;
import ceui.lisa.fragments.FragmentNewNovels;
import ceui.lisa.fragments.FragmentNiceFriend;
import ceui.lisa.fragments.FragmentNovelHolder;
import ceui.lisa.fragments.FragmentNovelMarkers;
import ceui.lisa.fragments.FragmentNovelSeries;
import ceui.lisa.fragments.FragmentNovelSeriesDetail;
import ceui.lisa.fragments.FragmentPv;
import ceui.lisa.fragments.FragmentRecmdIllust;
import ceui.lisa.fragments.FragmentRecmdUser;
import ceui.lisa.fragments.FragmentRelatedIllust;
import ceui.lisa.fragments.FragmentRelatedUser;
import ceui.lisa.fragments.FragmentSB;
import ceui.lisa.fragments.FragmentSearch;
import ceui.lisa.fragments.FragmentSettings;
import ceui.lisa.fragments.FragmentUserIllust;
import ceui.lisa.fragments.FragmentUserInfo;
import ceui.lisa.fragments.FragmentUserManga;
import ceui.lisa.fragments.FragmentUserNovel;
import ceui.lisa.fragments.FragmentViewPager;
import ceui.lisa.fragments.FragmentWebView;
import ceui.lisa.fragments.FragmentWhoFollowThisUser;
import ceui.lisa.helper.BackHandlerHelper;
import ceui.lisa.models.IllustsBean;
import ceui.lisa.models.NovelBean;
import ceui.lisa.models.UserPreviewsBean;
import ceui.lisa.utils.Local;
import ceui.lisa.utils.Params;
import ceui.lisa.utils.ReverseResult;
import ceui.loxia.flag.FlagDescFragment;
import ceui.loxia.flag.FlagReasonFragment;

public class TemplateActivity extends BaseActivity<ActivityFragmentBinding> implements ColorPickerDialogListener {

    public static final String EXTRA_FRAGMENT = "dataType";
    public static final String EXTRA_KEYWORD = "keyword";
    public static final String FRAGMENT_LOGIN = "登录注册";
    private static final String FRAGMENT_RELATED_ILLUST = "相关作品";
    private static final String FRAGMENT_HISTORY = "浏览记录";
    public static final String FRAGMENT_WEB = "网页链接";
    public static final String FRAGMENT_SETTINGS = "设置";
    private static final String FRAGMENT_RECMD_USER = "推荐用户";
    private static final String FRAGMENT_PV = "特辑";
    private static final String FRAGMENT_REVERSE_SEARCH = "以图搜图";
    public static final String FRAGMENT_COMMENT = "相关评论";
    private static final String FRAGMENT_TAG_FILTER = "按标签筛选";
    public static final String FRAGMENT_TAG_STAR = "按标签收藏";
    private static final String FRAGMENT_ABOUT = "关于软件";
    private static final String FRAGMENT_MULTI_DOWNLOAD = "批量下载";
    public static final String FRAGMENT_SEARCH = "搜索";
    private static final String FRAGMENT_LATEST = "最新作品";
    public static final String FRAGMENT_DOWNLOAD = "下载管理";
    private static final String FRAGMENT_BIND_EMAIL = "绑定邮箱";
    private static final String FRAGMENT_MUTED_TAGS = "标签屏蔽记录";
    private static final String FRAGMENT_FILENAME = "修改命名方式";
    private static final String FRAGMENT_THEME = "主题颜色";
    private static final String FRAGMENT_FLAG_ILLUST = "举报插画";
    private static final String FRAGMENT_FLAG_DETAIL = "填写举报详细信息";
    private static final String FRAGMENT_LOCAL_USERS = "账号管理";
    private static final String FRAGMENT_FOLLOWING = "正在关注";
    private static final String FRAGMENT_NICE_FRIEND = "好P友";
    private static final String FRAGMENT_USER_INFO = "详细信息";
    private static final String FRAGMENT_FANS = "粉丝";
    private static final String FRAGMENT_LIKE_USER_LIST = "喜欢这个作品的用户";
    private static final String FRAGMENT_RELATED_USER = "相关用户";
    private static final String FRAGMENT_ILLUST_WORKS = "插画作品";
    private static final String FRAGMENT_MANGA_WORKS = "漫画作品";
    private static final String FRAGMENT_PUBLIC_ILLUST_STAR = "插画/漫画收藏";
    private static final String FRAGMENT_PUBLIC_NOVEL_STAR = "小说收藏";
    private static final String FRAGMENT_NOVEL_WORKS = "小说作品";
    private static final String FRAGMENT_MY_ILLUST_STAR = "我的插画收藏";
    private static final String FRAGMENT_MY_NOVEL_STAR = "我的小说收藏";
    private static final String FRAGMENT_WATCHLIST = "追更列表";
    private static final String FRAGMENT_MY_FOLLOWING = "我的关注";
    private static final String FRAGMENT_NOVEL_MARKERS = "小说书签";
    private static final String FRAGMENT_RECMD_MANGA = "推荐漫画";
    private static final String FRAGMENT_RECMD_NOVEL = "推荐小说";
    private static final String FRAGMENT_NEW_NOVELS = "关注者的小说";
    public static final String FRAGMENT_NOVEL_DETAIL = "小说详情";
    private static final String FRAGMENT_IMAGE_DETAIL = "图片详情";
    public static final String FRAGMENT_NOVEL_SERIES_DETAIL = "小说系列详情";
    private static final String FRAGMENT_MANGA_SERIES = "漫画系列作品";
    private static final String FRAGMENT_MANGA_SERIES_DETAIL = "漫画系列详情";
    private static final String FRAGMENT_NOVEL_SERIES = "小说系列作品";
    private static final String FRAGMENT_FEATURE = "精华列";
    private static final int COLLECTION_ILLUST_BOOKMARKS = 0;
    private static final int COLLECTION_NOVEL_BOOKMARKS = 1;
    private static final int COLLECTION_FOLLOWING = 2;
    private static final int COLLECTION_WATCHLIST = 3;
    protected Fragment childFragment;
    private String dataType;

    @Override
    protected void initBundle(Bundle bundle) {
        dataType = bundle.getString(EXTRA_FRAGMENT);
    }

    protected Fragment createNewFragment() {
        Intent intent = getIntent();
        if (!TextUtils.isEmpty(dataType)) {
            Fragment webFragment = createWebFragment(intent);
            if (webFragment != null) {
                return webFragment;
            }
            Fragment userFragment = createUserFragment(intent);
            if (userFragment != null) {
                return userFragment;
            }
            Fragment collectionFragment = createCollectionFragment(intent);
            if (collectionFragment != null) {
                return collectionFragment;
            }
            Fragment detailFragment = createDetailFragment(intent);
            if (detailFragment != null) {
                return detailFragment;
            }
            Fragment settingsFragment = createSettingsFragment(intent);
            if (settingsFragment != null) {
                return settingsFragment;
            }
            Fragment reportFragment = createReportFragment(intent);
            if (reportFragment != null) {
                return reportFragment;
            }
            return new Fragment();
        }
        return null;
    }

    private Fragment createWebFragment(Intent intent) {
        switch (dataType) {
            case FRAGMENT_WEB: {
                String url = intent.getStringExtra(Params.URL);
                String title = intent.getStringExtra(Params.TITLE);
                boolean preferPreserve = intent.getBooleanExtra(Params.PREFER_PRESERVE, false);
                return FragmentWebView.newInstance(title, url, preferPreserve);
            }
            case FRAGMENT_REVERSE_SEARCH:
                ReverseResult result = intent.getParcelableExtra(Params.REVERSE_SEARCH_RESULT);
                Uri imageUri = intent.getParcelableExtra(Params.REVERSE_SEARCH_IMAGE_URI);
                return FragmentWebView.newInstance(result.getTitle(), result.getUrl(), result.getResponseBody(), result.getMime(), result.getEncoding(), result.getHistory_url(), imageUri);
            case FRAGMENT_IMAGE_DETAIL:
                return FragmentImageDetail.newInstance(intent.getStringExtra(Params.URL));
            default:
                return null;
        }
    }

    private Fragment createUserFragment(Intent intent) {
        switch (dataType) {
            case FRAGMENT_RECMD_USER:
                Bundle bundleExtra = intent.getBundleExtra(Params.USER_MODEL);
                if (bundleExtra == null) {
                    return new FragmentRecmdUser();
                }
                List<UserPreviewsBean> userPreviewsBeans = (ArrayList<UserPreviewsBean>) bundleExtra.getSerializable(Params.USER_MODEL);
                String nextUrl = intent.getStringExtra(Params.URL);
                return new FragmentRecmdUser(userPreviewsBeans, nextUrl);
            case FRAGMENT_LOCAL_USERS:
                return new FragmentLocalUsers();
            case FRAGMENT_FOLLOWING:
                return FragmentFollowUser.newInstance(
                        readUserId(intent),
                        Params.TYPE_PUBLIC, true);
            case FRAGMENT_NICE_FRIEND:
                return new FragmentNiceFriend();
            case FRAGMENT_USER_INFO:
                return new FragmentUserInfo();
            case FRAGMENT_FANS:
                return FragmentWhoFollowThisUser.newInstance(readUserId(intent));
            case FRAGMENT_LIKE_USER_LIST:
                return FragmentListSimpleUser.newInstance((IllustsBean) intent.getSerializableExtra(Params.CONTENT));
            case FRAGMENT_ILLUST_WORKS:
                return FragmentUserIllust.newInstance(readUserId(intent), true);
            case FRAGMENT_MANGA_WORKS:
                return FragmentUserManga.newInstance(readUserId(intent), true);
            case FRAGMENT_PUBLIC_ILLUST_STAR:
                return FragmentLikeIllust.newInstance(readUserId(intent), Params.TYPE_PUBLIC, true);
            case FRAGMENT_PUBLIC_NOVEL_STAR:
                return FragmentLikeNovel.newInstance(readUserId(intent), Params.TYPE_PUBLIC, true);
            case FRAGMENT_NOVEL_WORKS:
                return FragmentUserNovel.newInstance(readUserId(intent));
            case FRAGMENT_RELATED_USER:
                return FragmentRelatedUser.newInstance(readUserId(intent));
            default:
                return null;
        }
    }

    private Fragment createCollectionFragment(Intent intent) {
        switch (dataType) {
            case FRAGMENT_MY_ILLUST_STAR:
                return createCollectionFragment(COLLECTION_ILLUST_BOOKMARKS);
            case FRAGMENT_MY_NOVEL_STAR:
                return createCollectionFragment(COLLECTION_NOVEL_BOOKMARKS);
            case FRAGMENT_WATCHLIST:
                return createCollectionFragment(COLLECTION_WATCHLIST);
            case FRAGMENT_MY_FOLLOWING:
                return createCollectionFragment(COLLECTION_FOLLOWING);
            case FRAGMENT_RECMD_MANGA:
                return FragmentRecmdIllust.newInstance("漫画");
            case FRAGMENT_RECMD_NOVEL:
                return new FragmentNewNovel();
            case FRAGMENT_NEW_NOVELS:
                return new FragmentNewNovels();
            case FRAGMENT_FEATURE:
                return new FragmentFeature();
            case FRAGMENT_NOVEL_MARKERS:
                return new FragmentNovelMarkers();
            default:
                return null;
        }
    }

    private Fragment createDetailFragment(Intent intent) {
        switch (dataType) {
            case FRAGMENT_LOGIN:
                return new FragmentLogin();
            case FRAGMENT_RELATED_ILLUST: {
                int id = readIllustId(intent);
                String title = intent.getStringExtra(Params.ILLUST_TITLE);
                return FragmentRelatedIllust.newInstance(id, title);
            }
            case FRAGMENT_HISTORY:
                return new FragmentHistory();
            case FRAGMENT_PV:
                return new FragmentPv();
            case FRAGMENT_COMMENT: {
                String title = intent.getStringExtra(Params.ILLUST_TITLE);
                int workId = readIllustId(intent);
                if (workId == 0) {
                    workId = readNovelId(intent);
                    return FragmentComment.newNovelInstance(workId, title);
                }
                return FragmentComment.newIllustInstance(workId, title);
            }
            case FRAGMENT_ABOUT:
                return new FragmentAboutApp();
            case FRAGMENT_MULTI_DOWNLOAD:
                return new FragmentMultiDownload();
            case FRAGMENT_SEARCH:
                return new FragmentSearch();
            case FRAGMENT_LATEST:
                return new FragmentNew();
            case FRAGMENT_NOVEL_SERIES_DETAIL:
                return FragmentNovelSeriesDetail.newInstance(readId(intent));
            case FRAGMENT_DOWNLOAD:
                return new FragmentDownload();
            case FRAGMENT_NOVEL_DETAIL:
                return FragmentNovelHolder.newInstance((NovelBean) intent.getSerializableExtra(Params.CONTENT));
            case FRAGMENT_MANGA_SERIES:
                return FragmentMangaSeries.newInstance(readUserId(intent));
            case FRAGMENT_MANGA_SERIES_DETAIL:
                return FragmentMangaSeriesDetail.newInstance(readMangaSeriesId(intent));
            case FRAGMENT_NOVEL_SERIES:
                return new FragmentNovelSeries();
            default:
                return null;
        }
    }

    private Fragment createSettingsFragment(Intent intent) {
        switch (dataType) {
            case FRAGMENT_SETTINGS:
                return new FragmentSettings();
            case FRAGMENT_TAG_FILTER:
                return FragmentBookedTag.newInstance(readDataType(intent), intent.getStringExtra(EXTRA_KEYWORD));
            case FRAGMENT_TAG_STAR: {
                int id = readIllustId(intent);
                String type = intent.getStringExtra(Params.DATA_TYPE);
                String[] tagNames = intent.getStringArrayExtra(Params.TAG_NAMES);
                return FragmentSB.newInstance(id, type, tagNames);
            }
            case FRAGMENT_BIND_EMAIL:
                return new FragmentEditAccount();
            case FRAGMENT_MUTED_TAGS:
                return FragmentViewPager.newInstance(Params.VIEW_PAGER_MUTED);
            case FRAGMENT_FILENAME:
                return FragmentFileName.newInstance();
            case FRAGMENT_THEME:
                return new FragmentColors();
            default:
                return null;
        }
    }

    private Fragment createReportFragment(Intent intent) {
        switch (dataType) {
            case FRAGMENT_FLAG_ILLUST:
                return FlagReasonFragment.Companion.newInstance(
                        intent.getIntExtra(FlagDescFragment.FlagObjectIdKey, 0),
                        intent.getIntExtra(FlagDescFragment.FlagObjectTypeKey, 0)
                );
            case FRAGMENT_FLAG_DETAIL:
                return FlagDescFragment.Companion.newInstance(
                        intent.getIntExtra(FlagDescFragment.FlagReasonIdKey, 0),
                        intent.getIntExtra(FlagDescFragment.FlagObjectIdKey, 0),
                        intent.getIntExtra(FlagDescFragment.FlagObjectTypeKey, 0)
                );
            default:
                return null;
        }
    }

    private Fragment createCollectionFragment(int collectionType) {
        return FragmentCollection.newInstance(collectionType);
    }

    private int readUserId(Intent intent) {
        return intent.getIntExtra(Params.USER_ID, 0);
    }

    private int readIllustId(Intent intent) {
        return intent.getIntExtra(Params.ILLUST_ID, 0);
    }

    private int readNovelId(Intent intent) {
        return intent.getIntExtra(Params.NOVEL_ID, 0);
    }

    private int readId(Intent intent) {
        return intent.getIntExtra(Params.ID, 0);
    }

    private int readMangaSeriesId(Intent intent) {
        return intent.getIntExtra(Params.MANGA_SERIES_ID, 0);
    }

    private int readDataType(Intent intent) {
        return intent.getIntExtra(Params.DATA_TYPE, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (childFragment instanceof FragmentWebView) {
            return ((FragmentWebView) childFragment).getAgentWeb().handleKeyEvent(keyCode, event) ||
                    super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected int initLayout() {
        return R.layout.activity_fragment;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = createNewFragment();
            if (fragment != null) {
                fragmentManager.beginTransaction()
                        .add(R.id.fragment_container, fragment)
                        .commit();
                childFragment = fragment;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (childFragment != null) {
            childFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean hideStatusBar() {
        if (FRAGMENT_COMMENT.equals(dataType)) {
            return false;
        } else {
            return getIntent().getBooleanExtra("hideStatusBar", true);
        }
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        if (childFragment instanceof FragmentNovelHolder) {
            if (dialogId == Params.DIALOG_NOVEL_BG_COLOR) {
                Shaft.sSettings.setNovelHolderColor(color);
                ((FragmentNovelHolder) childFragment).setBackgroundColor(color);
            } else if (dialogId == Params.DIALOG_NOVEL_TEXT_COLOR) {
                Shaft.sSettings.setNovelHolderTextColor(color);
                ((FragmentNovelHolder) childFragment).setTextColor(color);
            }

            Local.setSettings(Shaft.sSettings);
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    @Override
    public void onBackPressed() {
        if (!BackHandlerHelper.handleBackPress(this)) {
            super.onBackPressed();
        }
    }
}
