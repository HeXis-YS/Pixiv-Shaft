package ceui.lisa.helper

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import ceui.lisa.core.DownloadItem
import ceui.lisa.file.SAFile
import okhttp3.Response
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import rxhttp.wrapper.callback.UriFactory

class SAFactory(@NotNull context: Context, item: DownloadItem) : UriFactory(context) {
    private val mItem: DownloadItem = item
    private var mUri: Uri

    init {
        val file: DocumentFile = SAFile.getDocument(context, mItem.illust, mItem.index, mItem.shouldStartNewDownload())
        mUri = file.uri
    }

    @Nullable
    override fun query(): Uri {
        return mUri
    }

    @NotNull
    override fun insert(@NotNull response: Response): Uri {
        return mUri
    }

    fun getUri(): Uri {
        return mUri
    }

    fun setUri(uri: Uri) {
        mUri = uri
    }
}
