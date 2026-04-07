package ceui.lisa.helper;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import ceui.lisa.activities.Shaft;
import ceui.lisa.database.AppDatabase;
import ceui.lisa.database.MuteEntity;
import ceui.lisa.models.IllustsBean;
import ceui.lisa.models.NovelBean;
import ceui.lisa.models.TagsBean;
import ceui.lisa.utils.Common;
import ceui.lisa.utils.Params;

public class IllustNovelFilter {

    private static List<MuteEntity> mutedWorksCache = null;
    private static List<TagsBean> mutedTagsCache = null;
    private static Set<Integer> mutedUserIdsCache = null;

    public static boolean judge(IllustsBean illust) {
        return judgeID(illust) || judgeTag(illust) || judgeUserID(illust) ;
    }

    public static boolean judge(NovelBean illust) {
        return judgeID(illust) || judgeTag(illust) || judgeUserID(illust) ;
    }

    public static boolean judgeID(IllustsBean illust) {
        List<MuteEntity> temp = getMutedWorksInternal();
        boolean isBanned = false;
        if (!Common.isEmpty(temp)) {
            for (MuteEntity muteEntity : temp) {
                if (muteEntity.getId() == illust.getId()) {
                    isBanned = true;
                    break;
                }
            }
        }
        return isBanned;
    }

    public static boolean judgeID(NovelBean illust) {
        List<MuteEntity> temp = getMutedWorksInternal();
        boolean isBanned = false;
        if (!Common.isEmpty(temp)) {
            for (MuteEntity muteEntity : temp) {
                if (muteEntity.getId() == illust.getId()) {
                    isBanned = true;
                    break;
                }
            }
        }
        return isBanned;
    }

    public static boolean judgeUserID(IllustsBean illust) {
        return getMutedUserIdsInternal().contains(illust.getUser().getUserId());
    }

    public static boolean judgeUserID(NovelBean illust) {
        return getMutedUserIdsInternal().contains(illust.getUser().getUserId());
    }

    public static boolean judgeTag(IllustsBean illustsBean) {
        String tagString = illustsBean.getTagString();
        if (TextUtils.isEmpty(tagString)) {
            return false;
        }

        List<TagsBean> temp = getMutedTags();
        for (TagsBean bean : temp) {
            if (bean.isEffective()) {
                String name = "*#" + bean.getName() + ",";
                if (bean.getFilter_mode() == 0 && tagString.contains(name)) {
                    illustsBean.setShield(true);
                    return true;
                } else if (bean.getFilter_mode() == 1 && Pattern.compile(bean.getName()).matcher(tagString).find()) {
                    illustsBean.setShield(true);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean judgeTag(NovelBean illustsBean) {
        String tagString = illustsBean.getTagString();
        if (TextUtils.isEmpty(tagString)) {
            return false;
        }

        List<TagsBean> temp = getMutedTags();
        for (TagsBean bean : temp) {
            if (bean.isEffective()) {
                String name = "*#" + bean.getName() + ",";
                if (bean.getFilter_mode() == 0 && tagString.contains(name)) {
//                    illustsBean.setShield(true);
                    return true;
                } else if (bean.getFilter_mode() == 1 && Pattern.compile(bean.getName()).matcher(tagString).find()) {
//                    illustsBean.setShield(true);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean judgeR18Filter(IllustsBean illustsBean) {
        if (!Shaft.sSettings.isR18FilterTempEnable()) {
            return false;
        }
        String tagString = illustsBean.getTagString();
        boolean isHit = tagString.contains("*#R-18,") || tagString.contains("*#R-18G,");
        illustsBean.setShield(isHit);
        return isHit;
    }

    public static boolean judgeR18Filter(NovelBean illustsBean) {
        if (!Shaft.sSettings.isR18FilterTempEnable()) {
            return false;
        }
        String tagString = illustsBean.getTagString();
        boolean isHit = tagString.contains("*#R-18,") || tagString.contains("*#R-18G,");
//        illustsBean.setShield(isHit);
        return isHit;
    }

    public static List<TagsBean> getMutedTags() {
        return new ArrayList<>(getMutedTagsInternal());
    }

    public static List<MuteEntity> getMutedWorks() {
        return new ArrayList<>(getMutedWorksInternal());
    }

    public static synchronized void invalidateMutedWorks() {
        mutedWorksCache = null;
    }

    public static synchronized void invalidateMutedTags() {
        mutedTagsCache = null;
    }

    public static synchronized void invalidateMutedUsers() {
        mutedUserIdsCache = null;
    }

    public static synchronized void invalidateAll() {
        invalidateMutedWorks();
        invalidateMutedTags();
        invalidateMutedUsers();
    }

    private static synchronized List<MuteEntity> getMutedWorksInternal() {
        if (mutedWorksCache == null) {
            mutedWorksCache = AppDatabase.searchDao(Shaft.getContext()).getMutedWorks();
        }
        return mutedWorksCache;
    }

    private static synchronized List<TagsBean> getMutedTagsInternal() {
        if (mutedTagsCache == null) {
            mutedTagsCache = new ArrayList<>();
            List<MuteEntity> muteEntities = AppDatabase.searchDao(Shaft.getContext()).getAllMutedTags();
            if (!Common.isEmpty(muteEntities)) {
                for (MuteEntity muteEntity : muteEntities) {
                    TagsBean bean = Shaft.sGson.fromJson(muteEntity.getTagJson(), TagsBean.class);
                    mutedTagsCache.add(bean);
                }
            }
        }
        return mutedTagsCache;
    }

    private static synchronized Set<Integer> getMutedUserIdsInternal() {
        if (mutedUserIdsCache == null) {
            mutedUserIdsCache = new HashSet<>();
            List<MuteEntity> muteEntities = AppDatabase.searchDao(Shaft.getContext()).getAllMuteEntities();
            if (!Common.isEmpty(muteEntities)) {
                for (MuteEntity muteEntity : muteEntities) {
                    if (muteEntity.getType() == Params.MUTE_USER) {
                        mutedUserIdsCache.add(muteEntity.getId());
                    }
                }
            }
        }
        return mutedUserIdsCache;
    }
}
