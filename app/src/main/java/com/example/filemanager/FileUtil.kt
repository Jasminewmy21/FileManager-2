package com.example.filemanager

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileUtil {
    /**
     * 检测所删除项目为文件还是文件夹
     * @param fileName 文件名
     */
    fun delete(fileName: String?) {
        val file = File(fileName)
        if (!file.exists()) {
            println("删除文件失败:" + fileName + "不存在！")
        } else {
            if (file.isFile) deleteFile(fileName) else deleteDirectory(fileName)
        }
    }

    fun renameFile(path: String?, oldname: String?, newname: String) {
        val oldfile = File("$path/$oldname")
        val newfile = File("$path/$newname")
        if (!oldfile.exists()) {
            println("重命名文件不存在")
            return  //重命名文件不存在
        }
        if (newfile.exists()) //若在该目录下已经有一个文件和新文件名相同，则不允许重命名
            println(newname + "已经存在！") else {
            oldfile.renameTo(newfile)
        }
    }


    companion object {
        private const val BUFFER_SIZE = 2 * 1024
        fun deleteFile(fileName: String?): Boolean {
            val file = File(fileName)
            // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
            return if (file.exists() && file.isFile) {
                if (file.delete()) {
                    println("删除单个文件" + fileName + "成功！")
                    true
                } else {
                    println("删除单个文件" + fileName + "失败！")
                    false
                }
            } else {
                println("删除单个文件失败：" + fileName + "不存在！")
                false
            }
        }

        fun deleteDirectory(dir: String?): Boolean { // 如果dir不以文件分隔符结尾，自动添加文件分隔符
            var dir = dir
            if (!dir!!.endsWith(File.separator)) dir = dir + File.separator
            val dirFile = File(dir)
            // 如果dir对应的文件不存在，或者不是一个目录，则退出
            if (!dirFile.exists() || !dirFile.isDirectory) {
                println("删除目录失败：" + dir + "不存在！")
                return false
            }
            var flag = true
            // 删除文件夹中的所有文件包括子目录
            val files = dirFile.listFiles()
            for (i in files.indices) { // 删除子文件
                if (files[i].isFile) {
                    flag = deleteFile(files[i].absolutePath)
                    if (!flag) break
                } else if (files[i].isDirectory) {
                    flag = deleteDirectory(files[i].absolutePath)
                    if (!flag) break
                }
            }
            if (!flag) {
                println("删除目录失败！")
                return false
            }
            // 删除当前目录
            return if (dirFile.delete()) {
                println("删除目录" + dir + "成功！")
                true
            } else {
                false
            }
        }
    }
}