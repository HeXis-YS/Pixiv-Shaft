package ceui.lisa.fragments

import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.databinding.FragmentFilterBinding
import ceui.lisa.utils.Common
import ceui.lisa.utils.PixivSearchParamUtil
import ceui.lisa.viewmodel.SearchModel
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import org.honorato.multistatetogglebutton.ToggleButton
import java.time.LocalDate
import java.util.Arrays
import java.util.Calendar

class FragmentFilter : BaseFragment<FragmentFilterBinding>() {
    private lateinit var searchModel: SearchModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        searchModel = ViewModelProvider(requireActivity())[SearchModel::class.java]
        searchModel.isNovel.observe(
            viewLifecycleOwner,
            Observer { aBoolean -> initTagSpinner(aBoolean) },
        )
        searchModel.startDate.observe(
            viewLifecycleOwner,
            Observer { s ->
                baseBind.startDate.text =
                    if (TextUtils.isEmpty(s)) getString(R.string.string_330) else s
            },
        )
        searchModel.endDate.observe(
            viewLifecycleOwner,
            Observer { s ->
                baseBind.endDate.text =
                    if (TextUtils.isEmpty(s)) getString(R.string.string_330) else s
            },
        )
        super.onActivityCreated(savedInstanceState)
    }

    override fun initLayout() {
        mLayoutID = R.layout.fragment_filter
    }

    override fun initView() {
        baseBind.submit.setOnClickListener { performSearch() }

        initTagSpinner(false)

        val starAdapter =
            ArrayAdapter(mContext, R.layout.spinner_item, PixivSearchParamUtil.ALL_SIZE_NAME)
        baseBind.starSizeSpinner.adapter = starAdapter
        baseBind.starSizeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    searchModel.starSize.value = PixivSearchParamUtil.ALL_SIZE_VALUE[position]
                    performSearch()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        for (i in PixivSearchParamUtil.ALL_SIZE_VALUE.indices) {
            if (PixivSearchParamUtil.ALL_SIZE_VALUE[i] == Shaft.sSettings.searchFilter) {
                baseBind.starSizeSpinner.setSelection(i)
                break
            }
        }

        val sortTypeAdapter =
            ArrayAdapter(mContext, R.layout.spinner_item, PixivSearchParamUtil.SORT_TYPE_NAME)
        baseBind.sortTypeSpinner.adapter = sortTypeAdapter
        baseBind.sortTypeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    searchModel.sortType.value = PixivSearchParamUtil.SORT_TYPE_VALUE[position]
                    performSearch()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        baseBind.sortTypeSpinner.setSelection(
            PixivSearchParamUtil.getSortTypeIndex(Shaft.sSettings.searchDefaultSortType),
        )

        baseBind.startDate.setOnClickListener { setDatePicker(searchModel.startDate) }
        baseBind.endDate.setOnClickListener { setDatePicker(searchModel.endDate) }
        baseBind.startEndDateClear.setOnClickListener {
            searchModel.startDate.value = null
            searchModel.endDate.value = null
            performSearch()
        }

        baseBind.restrictionToggle.setElements(PixivSearchParamUtil.R18_RESTRICTION_NAME)
        baseBind.restrictionToggle.setColors(
            Common.resolveThemeAttribute(mContext, androidx.appcompat.R.attr.colorPrimary),
            resources.getColor(R.color.fragment_center),
        )
        baseBind.restrictionToggle.value = 0
        baseBind.restrictionToggle.setOnValueChangedListener(
            object : ToggleButton.OnValueChangedListener {
                override fun onValueChanged(value: Int) {
                    searchModel.r18Restriction.value = value
                    performSearch()
                }
            },
        )
    }

    private fun initTagSpinner(isNovel: Boolean) {
        val titles =
            if (isNovel) PixivSearchParamUtil.TAG_MATCH_NAME_NOVEL else PixivSearchParamUtil.TAG_MATCH_NAME
        val values =
            if (isNovel) PixivSearchParamUtil.TAG_MATCH_VALUE_NOVEL else PixivSearchParamUtil.TAG_MATCH_VALUE
        val tagAdapter = ArrayAdapter(mContext, R.layout.spinner_item, titles)
        baseBind.tagSpinner.adapter = tagAdapter
        baseBind.tagSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    if (::searchModel.isInitialized &&
                        searchModel.searchType.value != null &&
                        searchModel.searchType.value == values[position]
                    ) {
                        return
                    }
                    searchModel.searchType.value = values[position]
                    performSearch()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        if (::searchModel.isInitialized) {
            val index = Arrays.asList(*values).indexOf(searchModel.searchType.value)
            baseBind.tagSpinner.setSelection(maxOf(index, 0))
        }
    }

    private fun setDatePicker(dateData: MutableLiveData<String>) {
        val currentDate = dateData.value
        val listener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val date = LocalDate.of(year, monthOfYear + 1, dayOfMonth).toString()
                dateData.value = date
                performSearch()
            }

        val now = Calendar.getInstance()
        val start = Calendar.getInstance()
        val dpd =
            if (!TextUtils.isEmpty(currentDate)) {
                val t = currentDate!!.split("-")
                DatePickerDialog.newInstance(
                    listener,
                    t[0].toInt(),
                    t[1].toInt() - 1,
                    t[2].toInt(),
                )
            } else {
                DatePickerDialog.newInstance(
                    listener,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH),
                )
            }
        start.set(1970, 0, 1)
        dpd.minDate = start
        dpd.maxDate = now
        dpd.accentColor = Common.resolveThemeAttribute(mContext, androidx.appcompat.R.attr.colorPrimary)
        dpd.isThemeDark = mContext.resources.getBoolean(R.bool.is_night_mode)
        dpd.show(parentFragmentManager, "DatePickerDialog")
    }

    private fun performSearch() {
        searchModel.nowGo.value = "search_now"
    }
}
