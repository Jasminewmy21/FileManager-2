package com.example.filemanager

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileUtil {
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

    @Throws(RuntimeException::class)
    fun toZip(srcDir: String?, out: OutputStream?, KeepDirStructure: Boolean) {
        val start = System.currentTimeMillis()
        var zos: ZipOutputStream? = null
        try {
            zos = ZipOutputStream(out)
            val sourceFile = File(srcDir)
            compress(sourceFile, zos, sourceFile.name, KeepDirStructure)
            val end = System.currentTimeMillis()
            println("压缩完成，耗时：" + (end - start) + " ms")
        } catch (e: Exception) {
            throw RuntimeException("zip error from ZipUtils", e)
        } finally {
            if (zos != null) {
                try {
                    zos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
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

        @Throws(Exception::class)
        private fun compress(sourceFile: File, zos: ZipOutputStream, name: String, KeepDirStructure: Boolean) {
            val buf = ByteArray(BUFFER_SIZE)
            if (sourceFile.isFile) { // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
                zos.putNextEntry(ZipEntry(name))
                // copy文件到zip输出流中
                var len: Int
                val `in` = FileInputStream(sourceFile)
                while (`in`.read(buf).also { len = it } != -1) {
                    zos.write(buf, 0, len)
                }
                zos.closeEntry()
                `in`.close()
            } else {
                val listFiles = sourceFile.listFiles()
                if (listFiles == null || listFiles.size == 0) { // 需要保留原来的文件结构时,需要对空文件夹进行处理
                    if (KeepDirStructure) { // 空文件夹的处理
                        zos.putNextEntry(ZipEntry("$name/"))
                        // 没有文件，不需要文件的copy
                        zos.closeEntry()
                    }
                } else {
                    for (file in listFiles) { // 判断是否需要保留原来的文件结构
                        if (KeepDirStructure) { // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
                            compress(file, zos, name + "/" + file.name, KeepDirStructure)
                        } else {
                            compress(file, zos, file.name, KeepDirStructure)
                        }
                    }
                }
            }
        }
    }
}