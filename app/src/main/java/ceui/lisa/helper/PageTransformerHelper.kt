package ceui.lisa.helper

import ceui.lisa.activities.Shaft
import ceui.lisa.viewpager.transforms.ABaseTransformer
import ceui.lisa.viewpager.transforms.AccordionTransformer
import ceui.lisa.viewpager.transforms.BackgroundToForegroundTransformer
import ceui.lisa.viewpager.transforms.CubeInTransformer
import ceui.lisa.viewpager.transforms.CubeOutTransformer
import ceui.lisa.viewpager.transforms.DefaultTransformer
import ceui.lisa.viewpager.transforms.DepthPageTransformer
import ceui.lisa.viewpager.transforms.DrawerTransformer
import ceui.lisa.viewpager.transforms.FlipHorizontalTransformer
import ceui.lisa.viewpager.transforms.FlipVerticalTransformer
import ceui.lisa.viewpager.transforms.ForegroundToBackgroundTransformer
import ceui.lisa.viewpager.transforms.RotateDownTransformer
import ceui.lisa.viewpager.transforms.RotateUpTransformer
import ceui.lisa.viewpager.transforms.ScaleInOutTransformer
import ceui.lisa.viewpager.transforms.StackTransformer
import ceui.lisa.viewpager.transforms.TabletTransformer
import ceui.lisa.viewpager.transforms.ZoomInTransformer
import ceui.lisa.viewpager.transforms.ZoomOutSlideTransformer
import ceui.lisa.viewpager.transforms.ZoomOutTransformer

object PageTransformerHelper {
    private val transformerMap: IndexedLinkedHashMap<Int, TransformerType> =
        IndexedLinkedHashMap<Int, TransformerType>().apply {
            put(0, TransformerType(0, DefaultTransformer::class.java))
            put(1, TransformerType(1, AccordionTransformer::class.java))
            put(2, TransformerType(2, BackgroundToForegroundTransformer::class.java))
            put(3, TransformerType(3, ForegroundToBackgroundTransformer::class.java))
            put(4, TransformerType(4, CubeInTransformer::class.java))
            put(5, TransformerType(5, CubeOutTransformer::class.java))
            put(6, TransformerType(6, DepthPageTransformer::class.java))
            put(7, TransformerType(7, FlipHorizontalTransformer::class.java))
            put(8, TransformerType(8, FlipVerticalTransformer::class.java))
            put(9, TransformerType(9, RotateDownTransformer::class.java))
            put(10, TransformerType(10, RotateUpTransformer::class.java))
            put(11, TransformerType(11, ScaleInOutTransformer::class.java))
            put(12, TransformerType(12, ZoomOutSlideTransformer::class.java))
            put(13, TransformerType(13, ZoomInTransformer::class.java))
            put(14, TransformerType(14, ZoomOutTransformer::class.java))
            put(15, TransformerType(15, StackTransformer::class.java))
            put(16, TransformerType(16, TabletTransformer::class.java))
            put(17, TransformerType(17, DrawerTransformer::class.java))
        }.tidyIndexes()

    @JvmStatic
    fun getCurrentTransformerIndex(): Int {
        val transformerType = Shaft.sSettings.transformerType
        if (!transformerMap.containsKey(transformerType)) {
            return 0
        }
        val index = ArrayList(transformerMap.keys).indexOf(transformerType)
        return index.coerceIn(0, transformerMap.size - 1)
    }

    @JvmStatic
    fun getCurrentTransformer(): ABaseTransformer {
        return try {
            transformerMap[Shaft.sSettings.transformerType]!!.pageTransformer.newInstance()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            DefaultTransformer()
        } catch (e: InstantiationException) {
            e.printStackTrace()
            DefaultTransformer()
        }
    }

    @JvmStatic
    fun getTransformerNames(): Array<String> {
        return transformerMap.values.map { it.name }.toTypedArray()
    }

    @JvmStatic
    fun setCurrentTransformer(index: Int) {
        var currentIndex = index
        if (currentIndex < 0 || currentIndex >= transformerMap.size) {
            currentIndex = 0
        }
        Shaft.sSettings.transformerType = transformerMap.getIndexed(currentIndex)!!.typeId
    }

    private class TransformerType(
        val typeId: Int,
        val pageTransformer: Class<out ABaseTransformer>,
    ) {
        val name: String
            get() = pageTransformer.simpleName.replace("Transformer", "")
    }
}
