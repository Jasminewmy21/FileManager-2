package com.example.filemanager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import android.widget.Toast
import java.io.File
import java.util.*

class Openfile(private val mContext: Context, filePath: String?) {
    private fun getUri(intent: Intent, file: File): Uri? {
        var uri: Uri? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //判断版本是否在7.0以上
            uri = FileProvider.getUriForFile(mContext, "com.example.filemanager.fileprovider", file)
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            uri = Uri.fromFile(file)
        }
        return uri
    }

    private fun generateVideoAudioIntent(filePath: String?, dataType: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("oneshot", 0)
        intent.putExtra("configchange", 0)
        val file = File(filePath)
        intent.setDataAndType(getUri(intent, file), dataType)
        return intent
    }

    private fun generateCommonIntent(filePath: String?, dataType: String): Intent {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = Intent.ACTION_VIEW
        val file = File(filePath)
        val uri = getUri(intent, file)
        intent.setDataAndType(uri, dataType)
        return intent
    }

    companion object {
        private const val DATA_TYPE_ALL = "*/*" //未指定明确的文件类型，不能使用精确类型的工具打开，需要用户选择
        private const val DATA_TYPE_APK = "application/vnd.android.package-archive"
        private const val DATA_TYPE_VIDEO = "video/*"
        private const val DATA_TYPE_AUDIO = "audio/*"
        private const val DATA_TYPE_HTML = "text/html"
        private const val DATA_TYPE_IMAGE = "image/*"
        private const val DATA_TYPE_PPT = "application/vnd.ms-powerpoint"
        private const val DATA_TYPE_EXCEL = "application/vnd.ms-excel"
        private const val DATA_TYPE_WORD = "application/msword"
        private const val DATA_TYPE_CHM = "application/x-chm"
        private const val DATA_TYPE_TXT = "text/plain"
        private const val DATA_TYPE_PDF = "application/pdf"
        private fun getHtmlFileIntent(filePath: String?): Intent {
            val uri = Uri.parse(filePath)
                    .buildUpon()
                    .encodedAuthority("com.android.htmlfileprovider")
                    .scheme("content")
                    .encodedPath(filePath)
                    .build()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, DATA_TYPE_HTML)
            return intent
        }
    }

    init {
        val file = File(filePath)
        if (!file.exists()) { //如果文件不存在
            Toast.makeText(mContext, "打开失败，原因：文件已经被移动或者删除", Toast.LENGTH_SHORT).show()

        }else{
            /* 取得扩展名 */
            val end = file.name.substring(file.name.lastIndexOf(".") + 1, file.name.length).toLowerCase(Locale.getDefault())
            /* 依扩展名的类型决定MimeType */
            var intent: Intent? = null
            intent = if (end == "m4a" || end == "mp3" || end == "mid" || end == "xmf" || end == "ogg" || end == "wav") {
                generateVideoAudioIntent(filePath, DATA_TYPE_AUDIO)
            } else if (end == "3gp" || end == "mp4") {
                generateVideoAudioIntent(filePath, DATA_TYPE_VIDEO)
            } else if (end == "jpg" || end == "gif" || end == "png" || end == "jpeg" || end == "bmp") {
                generateCommonIntent(filePath, DATA_TYPE_IMAGE)
            } else if (end == "apk") {
                generateCommonIntent(filePath, DATA_TYPE_APK)
            } else if (end == "html" || end == "htm") {
                getHtmlFileIntent(filePath)
            } else if (end == "ppt") {  
                generateCommonIntent(filePath, DATA_TYPE_PPT)
            } else if (end == "xls") {
                generateCommonIntent(filePath, DATA_TYPE_EXCEL)
            } else if (end == "doc") {
                generateCommonIntent(filePath, DATA_TYPE_WORD)
            } else if (end == "pdf") {
                generateCommonIntent(filePath, DATA_TYPE_PDF)
            } else if (end == "chm") {
                generateCommonIntent(filePath, DATA_TYPE_CHM)
            } else if (end == "txt") {
                generateCommonIntent(filePath, DATA_TYPE_TXT)
            } else {
                generateCommonIntent(filePath, DATA_TYPE_ALL)
            }
            mContext.startActivity(intent)
        }

    }
}