package ceui.lisa.fragments

import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.adapters.FileNameAdapter
import ceui.lisa.databinding.FragmentFileNameBinding
import ceui.lisa.download.FileCreator
import ceui.lisa.interfaces.OnItemClickListener
import ceui.lisa.model.CustomFileNameCell
import ceui.lisa.models.IllustsBean
import ceui.lisa.utils.Common
import ceui.lisa.utils.DensityUtil
import ceui.lisa.utils.Local
import ceui.lisa.utils.Params
import ceui.lisa.view.LinearItemDecoration
import com.google.gson.reflect.TypeToken
import java.util.ArrayList
import java.util.Collections

class FragmentFileName : BaseLazyFragment<FragmentFileNameBinding>() {
    private lateinit var illust: IllustsBean
    private val allItems = ArrayList<CustomFileNameCell>()
    private var mAdapter: FileNameAdapter? = null

    companion object {
        @JvmStatic
        fun newInstance(): FragmentFileName = FragmentFileName()
    }

    override fun initLayout() {
        mLayoutID = R.layout.fragment_file_name
    }

    override fun initView() {
        illust = Shaft.sGson.fromJson(Params.EXAMPLE_ILLUST, IllustsBean::class.java)
        baseBind.toolbar.toolbar.setNavigationOnClickListener { mActivity.finish() }
        baseBind.toolbar.toolbar.setTitle(R.string.string_242)
        baseBind.showNow.setOnClickListener { showPreview() }
        baseBind.saveNow.setOnClickListener { saveSettings() }
        baseBind.reset.setOnClickListener {
            if (mAdapter != null) {
                allItems.clear()
                allItems.addAll(FileCreator.defaultFileCells())
                mAdapter!!.notifyDataSetChanged()
            }
        }
        baseBind.hasP0.isChecked = Shaft.sSettings.isHasP0
        baseBind.hasP0.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            Shaft.sSettings.isHasP0 = isChecked
            Common.showToast(getString(R.string.string_428))
            Local.setSettings(Shaft.sSettings)
        }
    }

    override fun initData() {
        allItems.clear()
        val defaultFileCells = FileCreator.defaultFileCells()
        if (TextUtils.isEmpty(Shaft.sSettings.fileNameJson)) {
            allItems.addAll(defaultFileCells)
        } else {
            val customFileNameCells: Collection<CustomFileNameCell> = Shaft.sGson.fromJson(
                Shaft.sSettings.fileNameJson,
                object : TypeToken<List<CustomFileNameCell>>() {}.type,
            )
            allItems.addAll(customFileNameCells)
            val set = allItems.map { it.code }.toSet()
            allItems.addAll(defaultFileCells.filter { !set.contains(it.code) })
        }

        mAdapter = FileNameAdapter(allItems, mContext)
        baseBind.recyclerView.layoutManager = LinearLayoutManager(mContext)
        baseBind.recyclerView.isNestedScrollingEnabled = true
        baseBind.recyclerView.addItemDecoration(LinearItemDecoration(DensityUtil.dp2px(12.0f)))
        baseBind.recyclerView.adapter = mAdapter
        object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
            ): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                return makeMovementFlags(dragFlags, 0)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                Collections.swap(allItems, viewHolder.adapterPosition, target.adapterPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun onMoved(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                fromPos: Int,
                target: RecyclerView.ViewHolder,
                toPos: Int,
                x: Int,
                y: Int,
            ) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
                mAdapter!!.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
                showPreview()
            }
        }.let { ItemTouchHelper(it).attachToRecyclerView(baseBind.recyclerView) }
        mAdapter!!.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(v: View?, position: Int, viewType: Int) {
                showPreview()
            }
        })

        showPreview()
    }

    private fun showPreview() {
        for (allItem in allItems) {
            if (allItem.code == FileCreator.ILLUST_ID && !allItem.isChecked) {
                Common.showToast("作品ID为必选项，请选择作品ID")
                return
            }
            if (allItem.code == FileCreator.P_SIZE && !allItem.isChecked) {
                Common.showToast("作品P数为必选项，请选择作品P数")
                return
            }
        }
        val name = FileCreator.customFileNameForPreview(illust, allItems, 1)
        baseBind.fileName.text = name
    }

    private fun saveSettings() {
        for (allItem in allItems) {
            if (allItem.code == FileCreator.ILLUST_ID && !allItem.isChecked) {
                Common.showToast("作品ID为必选项，请选择作品ID")
                return
            }
            if (allItem.code == FileCreator.P_SIZE && !allItem.isChecked) {
                Common.showToast("作品P数为必选项，请选择作品P数")
                return
            }
        }

        val json = Shaft.sGson.toJson(allItems)
        Shaft.sSettings.fileNameJson = json
        Local.setSettings(Shaft.sSettings)
        Common.showToast("保存成功！")
    }
}
