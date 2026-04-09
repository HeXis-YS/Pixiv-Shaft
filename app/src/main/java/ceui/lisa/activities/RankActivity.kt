package ceui.lisa.activities

import android.content.Intent
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import ceui.lisa.R
import ceui.lisa.databinding.ActivityMultiViewPagerBinding
import ceui.lisa.fragments.FragmentRankIllust
import ceui.lisa.fragments.FragmentRankNovel
import ceui.lisa.utils.Common
import ceui.lisa.utils.MyOnTabSelectedListener
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import java.util.Calendar

class RankActivity : BaseActivity<ActivityMultiViewPagerBinding>(), DatePickerDialog.OnDateSetListener {
    private var dataType = ""
    private var queryDate = ""

    override fun initLayout(): Int = R.layout.activity_multi_view_pager

    override fun initView() {
        setSupportActionBar(baseBind.toolbar)
        baseBind.toolbar.setNavigationOnClickListener { finish() }
        baseBind.toolbarTitle.text = mContext.getString(R.string.ranking_illust)
        dataType = intent.getStringExtra("dataType").orEmpty()
        queryDate = intent.getStringExtra("date").orEmpty()

        val chineseTitles = arrayOf(
            mContext.getString(R.string.daily_rank),
            mContext.getString(R.string.weekly_rank),
            mContext.getString(R.string.monthly_rank),
            mContext.getString(R.string.created_by_ai),
            mContext.getString(R.string.man_like),
            mContext.getString(R.string.woman_like),
            mContext.getString(R.string.self_done),
            mContext.getString(R.string.new_fish),
            mContext.getString(R.string.r_eighteen),
            mContext.getString(R.string.r_eighteen_weekly_rank),
            mContext.getString(R.string.r_eighteen_male_rank),
            mContext.getString(R.string.r_eighteen_female_rank),
            mContext.getString(R.string.r_eighteen_ai_rank),
            mContext.getString(R.string.r_eighteen_guro_rank),
        )

        val chineseTitlesManga = arrayOf(
            getString(R.string.string_124),
            getString(R.string.string_125),
            getString(R.string.string_126),
            getString(R.string.string_127),
            getString(R.string.string_128),
        )
        val chineseTitlesNovel = arrayOf(
            getString(R.string.string_129),
            getString(R.string.string_130),
            getString(R.string.string_131),
            getString(R.string.string_132),
            getString(R.string.string_133),
            getString(R.string.string_134),
        )

        val titles = getTitles(chineseTitles, chineseTitlesManga, chineseTitlesNovel)
        val fragments = getFragments(chineseTitles, chineseTitlesManga, chineseTitlesNovel)

        baseBind.viewPager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment = fragments[position]

            override fun getCount(): Int = titles.size

            override fun getPageTitle(position: Int): CharSequence = titles[position]
        }
        baseBind.tabLayout.setupWithViewPager(baseBind.viewPager)
        val listener = MyOnTabSelectedListener(fragments)
        baseBind.tabLayout.addOnTabSelectedListener(listener)
        val intentIndex = intent.getIntExtra("index", 0)
        if (intentIndex >= 0) {
            baseBind.viewPager.currentItem = intentIndex
        }
    }

    private fun getTitles(
        chineseTitles: Array<String>,
        chineseTitlesManga: Array<String>,
        chineseTitlesNovel: Array<String>,
    ): Array<String> {
        return when (dataType) {
            "插画" -> chineseTitles
            "漫画" -> chineseTitlesManga
            "小说" -> chineseTitlesNovel
            else -> emptyArray()
        }
    }

    private fun getFragments(
        chineseTitles: Array<String>,
        chineseTitlesManga: Array<String>,
        chineseTitlesNovel: Array<String>,
    ): Array<Fragment> {
        return when (dataType) {
            "插画" -> Array(chineseTitles.size) { index ->
                FragmentRankIllust.newInstance(index, queryDate, false)
            }

            "漫画" -> Array(chineseTitlesManga.size) { index ->
                FragmentRankIllust.newInstance(index, queryDate, true)
            }

            "小说" -> Array(chineseTitlesNovel.size) { index ->
                FragmentRankNovel.newInstance(index, queryDate)
            }

            else -> emptyArray()
        }
    }

    override fun initData() = Unit

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.select_date, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_select_date) {
            val now = Calendar.getInstance()
            now.add(Calendar.DAY_OF_MONTH, -1)
            val dpd = if (!TextUtils.isEmpty(queryDate) && queryDate.contains("-")) {
                val t = queryDate.split("-")
                DatePickerDialog.newInstance(
                    this,
                    t[0].toInt(),
                    t[1].toInt() - 1,
                    t[2].toInt(),
                )
            } else {
                DatePickerDialog.newInstance(
                    this,
                    now[Calendar.YEAR],
                    now[Calendar.MONTH],
                    now[Calendar.DAY_OF_MONTH],
                )
            }
            val start = Calendar.getInstance()
            start.set(2008, 0, 1)
            dpd.minDate = start
            dpd.maxDate = now
            dpd.accentColor = Common.resolveThemeAttribute(mContext, androidx.appcompat.R.attr.colorPrimary)
            dpd.isThemeDark = mContext.resources.getBoolean(R.bool.is_night_mode)
            dpd.show(supportFragmentManager, "DatePickerDialog")
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDateSet(view: DatePickerDialog, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val date = year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth
        Common.showLog(date)
        val intent = Intent(mContext, RankActivity::class.java)
        intent.putExtra("date", date)
        intent.putExtra("dataType", dataType)
        intent.putExtra("index", baseBind.viewPager.currentItem)
        startActivity(intent)
        finish()
    }

    override fun hideStatusBar(): Boolean = false
}
