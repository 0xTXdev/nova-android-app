package jp.co.soramitsu.common.data

import android.content.Context
import jp.co.soramitsu.common.interfaces.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileProviderImpl(
    private val context: Context
) : FileProvider {

    override suspend fun createFileInTempStorage(fileName: String): File {
        return withContext(Dispatchers.IO) {
            val cacheDir = context.externalCacheDir?.absolutePath ?: throw IllegalStateException("cache directory is unavailable")

            File(cacheDir, fileName)
        }
    }
}