package ceui.lisa.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;

import ceui.lisa.R;
import ceui.lisa.core.Manager;
import ceui.lisa.databinding.ActivityCoverBinding;
import ceui.lisa.fragments.FragmentCenter;
import ceui.lisa.fragments.FragmentLeft;
import ceui.lisa.fragments.FragmentRight;
import ceui.lisa.fragments.FragmentViewPager;
import ceui.lisa.helper.DrawerLayoutHelper;
import ceui.lisa.helper.NavigationLocationHelper;
import ceui.lisa.utils.Common;
import ceui.lisa.utils.GlideUtil;
import ceui.lisa.utils.Params;
import ceui.lisa.view.DrawerLayoutViewPager;

import static ceui.lisa.R.id.nav_gallery;
import static ceui.lisa.R.id.nav_slideshow;
import static ceui.lisa.activities.Shaft.sUserModel;

/**
 * 主页
 */
public class MainActivity extends BaseActivity<ActivityCoverBinding>
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_WRITE_STORAGE = 1001;
    private static final String EXTRA_REFRESH_DRAWER_HEADER = "refreshDrawerHeader";
    private static final int PAGE_LEFT = 0;
    private static final int PAGE_CENTER = 1;
    private static final int PAGE_RIGHT = 2;
    private static final int PAGE_R18 = 3;

    private ImageView userHead;
    private TextView username;
    private TextView user_email;
    private long mExitTime;
    private Fragment[] baseFragments = null;

    @Override
    protected int initLayout() {
        return R.layout.activity_cover;
    }

    @Override
    public boolean hideStatusBar() {
        return true;
    }

    @Override
    protected void initView() {
        baseBind.drawerLayout.setScrimColor(Color.TRANSPARENT);
        baseBind.navView.setNavigationItemSelectedListener(this);
        userHead = baseBind.navView.getHeaderView(0).findViewById(R.id.user_head);
        username = baseBind.navView.getHeaderView(0).findViewById(R.id.user_name);
        user_email = baseBind.navView.getHeaderView(0).findViewById(R.id.user_email);
        initDrawerHeader();
        userHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.showUser(mContext, sUserModel);
                baseBind.drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
        userHead.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                boolean filterEnable = Shaft.sSettings.isR18FilterTempEnable();
                Shaft.sSettings.setR18FilterTempEnable(!filterEnable);
                Common.showToast(filterEnable ? "ԅ(♡﹃♡ԅ)" : "X﹏X");
                return true;
            }
        });
        baseBind.navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return navigateToPage(item.getItemId());
            }
        });
        baseBind.navigationView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                forceRefreshCurrentPage(item.getItemId());
            }
        });
        baseBind.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                syncBottomNavigation(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        baseBind.viewPager.setTouchEventForwarder(new DrawerLayoutViewPager.IForwardTouchEvent() {
            @Override
            public void forwardTouchEvent(MotionEvent ev) {
                getDrawer().onTouchEvent(ev);
            }
        });
        DrawerLayoutHelper.setCustomLeftEdgeSize(getDrawer(), 1.0f);
    }

    public static Intent newIntent(Context context, boolean refreshDrawerHeader) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(EXTRA_REFRESH_DRAWER_HEADER, refreshDrawerHeader);
        return intent;
    }

    private void initFragment() {
        if (Shaft.sSettings.isMainViewR18()) {
            baseBind.navigationView.inflateMenu(R.menu.main_activity0_with_r18);
            baseFragments = new Fragment[]{
                    new FragmentLeft(),
                    new FragmentCenter(),
                    new FragmentRight(),
                    FragmentViewPager.newInstance(Params.VIEW_PAGER_R18),
            };
        } else {
            baseBind.navigationView.inflateMenu(R.menu.main_activity0);
            baseFragments = new Fragment[]{
                    new FragmentLeft(),
                    new FragmentCenter(),
                    new FragmentRight()
            };
        }
        baseBind.viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return baseFragments[i];
            }

            @Override
            public int getCount() {
                return baseFragments.length;
            }
        });
        baseBind.viewPager.setOffscreenPageLimit(baseFragments.length - 1);
        baseBind.viewPager.setCurrentItem(getNavigationInitPosition());
        Manager.get().restore();
    }

    @Override
    protected void initData() {
        if (sUserModel != null && sUserModel.getUser() != null && sUserModel.getUser().isIs_login()) {
            if (Common.isAndroidQ()) {
                initFragment();
//                startActivity(new Intent(this, ListActivity.class));
            } else {
                ensureStoragePermission();
            }
        } else {
            TemplateActivity.startLogin(mContext);
            finish();
        }
    }

    private void ensureStoragePermission() {
        if (hasStoragePermission()) {
            initFragment();
            return;
        }
        requestStoragePermission();
    }

    private boolean hasStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_WRITE_STORAGE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            handleStoragePermissionResult(grantResults);
        }
    }

    private void handleStoragePermissionResult(@NonNull int[] grantResults) {
        if (isPermissionGranted(grantResults)) {
            initFragment();
            return;
        }
        Common.showToast(getString(R.string.access_denied));
        finish();
    }

    private boolean isPermissionGranted(@NonNull int[] grantResults) {
        return grantResults.length > 0
                && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    public DrawerLayout getDrawer() {
        return baseBind.drawerLayout;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        handleDrawerNavigation(item.getItemId());
        baseBind.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleDrawerNavigation(int itemId) {
        if (itemId == R.id.nav_new_work) {
            TemplateActivity.startLatest(this);
            return;
        }
        Intent intent = resolveDrawerDestination(itemId);
        if (intent != null) {
            startActivity(intent);
        }
    }

    private boolean navigateToPage(int itemId) {
        int pageIndex = getPageIndex(itemId);
        if (pageIndex == -1) {
            return false;
        }
        baseBind.viewPager.setCurrentItem(pageIndex);
        return true;
    }

    private int getPageIndex(int itemId) {
        if (itemId == R.id.action_1) {
            return PAGE_LEFT;
        } else if (itemId == R.id.action_2) {
            return PAGE_CENTER;
        } else if (itemId == R.id.action_3) {
            return PAGE_RIGHT;
        } else if (itemId == R.id.action_4) {
            return PAGE_R18;
        }
        return -1;
    }

    private void forceRefreshCurrentPage(int itemId) {
        for (Fragment baseFragment : baseFragments) {
            if (itemId == R.id.action_1 && baseFragment instanceof FragmentLeft) {
                ((FragmentLeft) baseFragment).forceRefresh();
            } else if (itemId == R.id.action_2 && baseFragment instanceof FragmentCenter) {
                ((FragmentCenter) baseFragment).forceRefresh();
            } else if (itemId == R.id.action_3 && baseFragment instanceof FragmentRight) {
                ((FragmentRight) baseFragment).forceRefresh();
            } else if (itemId == R.id.action_4 && baseFragment instanceof FragmentViewPager) {
                ((FragmentViewPager) baseFragment).forceRefresh();
            }
        }
    }

    private void syncBottomNavigation(int position) {
        if (position == PAGE_LEFT) {
            baseBind.navigationView.setSelectedItemId(R.id.action_1);
        } else if (position == PAGE_CENTER) {
            baseBind.navigationView.setSelectedItemId(R.id.action_2);
        } else if (position == PAGE_RIGHT) {
            baseBind.navigationView.setSelectedItemId(R.id.action_3);
        } else if (position == PAGE_R18) {
            baseBind.navigationView.setSelectedItemId(R.id.action_4);
        }
    }

    private Intent resolveDrawerDestination(int itemId) {
        if (itemId == nav_gallery) {
            return TemplateActivity.newDownloadManagerIntent(this);
        } else if (itemId == nav_slideshow) {
            return TemplateActivity.newHistoryIntent(this);
        } else if (itemId == R.id.nav_manage) {
            return TemplateActivity.newSettingsIntent(this);
        } else if (itemId == R.id.nav_share) {
            return TemplateActivity.newAboutIntent(this);
        } else if (itemId == R.id.main_page) {
            return UActivity.newCurrentUserIntent(this);
        } else if (itemId == R.id.muted_list) {
            return TemplateActivity.newMutedTagsIntent(this);
        } else if (itemId == R.id.nav_feature) {
            return TemplateActivity.newFeatureIntent(this);
        } else if (itemId == R.id.nav_fans) {
            return TemplateActivity.newFansIntent(this, sUserModel.getUser().getId());
        } else if (itemId == R.id.illust_star) {
            return TemplateActivity.newMyIllustBookmarksIntent(this);
        } else if (itemId == R.id.novel_star) {
            return TemplateActivity.newMyNovelBookmarksIntent(this);
        } else if (itemId == R.id.watchlist) {
            return TemplateActivity.newWatchlistIntent(this);
        } else if (itemId == R.id.novel_markers) {
            return TemplateActivity.newNovelMarkersIntent(this);
        } else if (itemId == R.id.follow_user) {
            return TemplateActivity.newMyFollowingIntent(this);
        }
        return null;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }

    private void initDrawerHeader() {
        if (sUserModel != null && sUserModel.getUser() != null) {
            Glide.with(mContext)
                    .load(GlideUtil.getHead(sUserModel.getUser()))
                    .into(userHead);
            username.setText(sUserModel.getUser().getName());
            user_email.setText(TextUtils.isEmpty(sUserModel.getUser().getMail_address()) ?
                    mContext.getString(R.string.no_mail_address) : sUserModel.getUser().getMail_address());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (handleOpenDrawerOnBack()) {
            return true;
        }
        if (shouldInterceptBack(keyCode, event)) {
            exit();
            return true;
        }
        return false;
    }

    private boolean handleOpenDrawerOnBack() {
        if (!baseBind.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            return false;
        }
        baseBind.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean shouldInterceptBack(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0;
    }

    public void exit() {
        if (shouldExitImmediately()) {
            finish();
            return;
        }
        if (hasPendingDownloadTasks()) {
            showPendingDownloadExitDialog();
            return;
        }
        promptDoubleTapExit();
    }

    private boolean shouldExitImmediately() {
        return (System.currentTimeMillis() - mExitTime) <= 2000;
    }

    private boolean hasPendingDownloadTasks() {
        return Manager.get().getContent().size() != 0;
    }

    private void showPendingDownloadExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getString(R.string.shaft_hint));
        builder.setMessage(mContext.getString(R.string.you_have_download_plan));
        builder.setPositiveButton(mContext.getString(R.string.sure), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopAllTasksAndExit();
            }
        });
        builder.setNegativeButton(mContext.getString(R.string.cancel), null);
        builder.setNeutralButton(getString(R.string.see_download_task), (dialog, which) -> openDownloadManager());
        builder.create().show();
    }

    private void stopAllTasksAndExit() {
        Manager.get().stopAll();
        finish();
    }

    private void openDownloadManager() {
        TemplateActivity.startDownloadManager(this);
    }

    private void promptDoubleTapExit() {
        Common.showToast(getString(R.string.double_click_finish));
        mExitTime = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldRefreshDrawerHeader()) {
            initDrawerHeader();
            getIntent().removeExtra(EXTRA_REFRESH_DRAWER_HEADER);
        }
    }

    private boolean shouldRefreshDrawerHeader() {
        return getIntent().getBooleanExtra(EXTRA_REFRESH_DRAWER_HEADER, false);
    }

    @Override
    public void finish() {
        int currentPosition = baseBind.viewPager.getCurrentItem();
        Shaft.getMMKV().putInt(Params.MAIN_ACTIVITY_NAVIGATION_POSITION, currentPosition);
        super.finish();
    }

    private int getNavigationInitPosition() {
        int defaultPosition = 0;
        String settingValue = Shaft.sSettings.getNavigationInitPosition();
        if (settingValue.equals(NavigationLocationHelper.LATEST)) {
            int latestPosition = Shaft.getMMKV().getInt(Params.MAIN_ACTIVITY_NAVIGATION_POSITION, 0);
            return latestPosition < baseFragments.length ? latestPosition : defaultPosition;
        }
        NavigationLocationHelper.NavigationItem navigationValue = NavigationLocationHelper.NAVIGATION_MAP.getOrDefault(settingValue, null);
        if (navigationValue == null) {
            return defaultPosition;
        }
        Class clazz = navigationValue.getInstanceClass();
        for (int i = 0; i < baseFragments.length; i++) {
            Fragment fragment = baseFragments[i];
            if (clazz == fragment.getClass()) {
                return i;
            }
        }
        return defaultPosition;
    }
}
