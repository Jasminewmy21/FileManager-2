package com.example.filemanager

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.*
import android.os.StrictMode.VmPolicy
import android.support.annotation.RequiresApi
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.github.clans.fab.FloatingActionMenu
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private var rootPath: String? = null
    private var Path: String? = null
    private var recyclerView: RecyclerView? = null
    var toolbar: Toolbar? = null
    var dialog: AlertDialog.Builder? = null
    private val fileList: MutableList<Fileit> = ArrayList()
    private var orderList: List<Fileit> = ArrayList()
    private val finalList: MutableList<Fileit> = ArrayList()
    private var cutmode = false
    private var isfile = false
    private var fabCopy: FloatingActionButton? = null
    private var fam_add: FloatingActionMenu? = null
    private var fab_add_file: com.github.clans.fab.FloatingActionButton? = null
    private var fab_add_folder: com.github.clans.fab.FloatingActionButton? = null
    val items = arrayOf("复制", "剪切", "重命名", "删除", "分享")
    var copyPath: String? = null
    var filename: String? = null
    var fileutil: FileUtil? = null
    private var mExitTime = 0L //退出时间
    private var adapter: FileAdapter? = null
    private var mCurPath: String? = ""
    private val allFiles = LinkedList<String?>()

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        builder.detectFileUriExposure()
        val rxPermissions = RxPermissions(this)
        rxPermissions.request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(object : Observer<Boolean> {
                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(aBoolean: Boolean) {
                        if (aBoolean) {
                            init()
                            initlist()
                        } else {
                            Toast.makeText(this@MainActivity, "权限获取失败，即将退出APP", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onError(e: Throwable) {}
                    override fun onComplete() {}
                })
    }

    private fun init() { //获取所需权限
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar!!.setNavigationIcon(R.drawable.ic_person_black_24dp)
        //初始化控件
        fileutil = FileUtil()
        fam_add = findViewById(R.id.fam_add)
        fab_add_file = findViewById(R.id.fab_add_file)
        fab_add_folder = findViewById(R.id.fab_add_folder)
        recyclerView = findViewById<View>(R.id.recycler_view) as RecyclerView
        val layoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = layoutManager
        fabCopy = findViewById<View>(R.id.float_button) as FloatingActionButton
        dealFabInfo(true)
        adapter = FileAdapter(this, finalList)
        recyclerView!!.adapter = adapter
        adapter!!.setOnItemClickListener(object : FileAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, postion: Int) {
                val s = finalList[postion]
                clickFile(s)
            }

        });
        adapter!!.setOnItemLongClickListener(object : FileAdapter.OnItemLongClickListener {
            override fun onItemLongClick(view: View?, postion: Int) {
                val s = finalList[postion]
                longClickItem(s)
            }

        })
        //新建文件
        fab_add_file!!.setOnClickListener(View.OnClickListener {
            val et = EditText(this@MainActivity)
            AlertDialog.Builder(this@MainActivity).setTitle("请输入文件名称").setView(et).setPositiveButton("确定") { dialogInterface, i ->
                fam_add!!.close(true)
                val file = File(mCurPath + "/" + et.text)
                Toast.makeText(this@MainActivity, mCurPath + "/" + et.text, Toast.LENGTH_SHORT).show()
                if (!file.exists()) {
                    try {
                        file.createNewFile()
                        getFileDir(mCurPath)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@MainActivity, "文件创建失败", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "文件已存在", Toast.LENGTH_SHORT).show()
                }
            }.setNegativeButton("取消", null).show()
        })
        //新建文件夹
        fab_add_folder!!.setOnClickListener(View.OnClickListener {
            fam_add!!.close(true)
            val et = EditText(this@MainActivity)
            AlertDialog.Builder(this@MainActivity).setTitle("请输入文件夹名称").setView(et).setPositiveButton("确定") { dialogInterface, i ->
                val file = File(mCurPath + "/" + et.text)
                Toast.makeText(this@MainActivity, mCurPath + "/" + et.text, Toast.LENGTH_SHORT).show()
                if (!file.exists()) {
                    file.mkdirs()
                    getFileDir(mCurPath)
                } else {
                    Toast.makeText(this@MainActivity, "文件夹已存在", Toast.LENGTH_SHORT).show()
                }
            }.setNegativeButton("取消", null).show()
        })
        /**
         * 点击添加
         */
        fabCopy!!.setOnClickListener {
            if (cutmode) {
                if (isfile) {
                    copyFile(copyPath, mCurPath)
                    fileutil!!.delete(copyPath)
                    cutmode = false
                    dealFabInfo(true)
                    getFileDir(mCurPath)
                } else {
                    copyFolder(copyPath, mCurPath)
                    fileutil!!.delete(copyPath)
                    cutmode = false
                    dealFabInfo(true)
                    getFileDir(mCurPath)
                }
            } else {
                if (isfile) {
                    copyFile(copyPath, mCurPath)
                    dealFabInfo(true)
                    getFileDir(mCurPath)
                } else {
                    copyFolder(copyPath, mCurPath)
                    dealFabInfo(true)
                    getFileDir(mCurPath)
                }
            }
        }
    }

    private fun initlist() {
        rootPath = getRootPath()
        allFiles.add(rootPath)
        getFileDir(rootPath)
    }

    /**
     *
     * @param isAdd
     */
    @SuppressLint("RestrictedApi")
    private fun dealFabInfo(isAdd: Boolean) {
        if (isAdd) {
            fam_add!!.visibility = View.VISIBLE
            fabCopy!!.visibility = View.GONE
        } else {
            fam_add!!.visibility = View.GONE
            fabCopy!!.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        for (fileit in finalList) {
            if (fileit.imageId == 3) {
                clickFile(fileit)
                return
            }
        }
        exit()
    }

    /**
     * 处理搜索栏
     *
     * @param menu
     * @return
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean { // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        val home = menu.findItem(android.R.id.home)
        if (home != null) {
            Log.d(TAG, "onCreateOptionsMenu: home " + home.actionView)
        }
        val searchItem = menu.findItem(R.id.search)
        var searchView: SearchView? = null
        if (searchItem != null) {
            searchView = searchItem.actionView as SearchView
        }
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean { //处理搜索结果
                searchFile(query)
                Toast.makeText(this@MainActivity, "搜索: $query", Toast.LENGTH_LONG).show()
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val intent = Intent(this@MainActivity, RegisterActivity::class.java)
            intent.putExtra("Register", false)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 点击文件目录
     */
    private fun clickFile(s: Fileit) {
        Path = s.path
        if (s.imageId == 3) { //返回上一层
            allFiles.removeLast()
            getFileDir(Path)
        } else if (s.imageId == 2) { //到根目录
            allFiles.add(Path)
            getFileDir(Path)
        } else if (s.imageId == 1) { //判断点击item是否为文件
            allFiles.add(Path)
            Openfile(applicationContext, s.path)
        } else if (s.imageId == 0) { //点击文件夹
            allFiles.add(Path)
            getFileDir(Path)
        }
    }

    private fun longClickItem(s: Fileit) {
        Log.d(TAG, "长按了" + s.name)
        Path = s.path
        dialog = AlertDialog.Builder(this@MainActivity)
                .setTitle(s.name)
                .setItems(items) { dialog, which ->
                    if (items[which] == "复制") {
                        val file = File(s.path)
                        if (file.isFile) {
                            isfile = true
                            copyPath = Path
                            filename = s.name
                            dealFabInfo(false)
                        }
                        if (file.isDirectory) {
                            isfile = false
                            copyPath = Path
                            dealFabInfo(false)
                        }
                        Toast.makeText(this@MainActivity, items[which], Toast.LENGTH_SHORT).show()
                    }
                    if (items[which] == "剪切") {
                        val file = File(s.path)
                        if (file.isFile) {
                            isfile = true
                            cutmode = true
                            copyPath = Path
                            filename = s.name
                            dealFabInfo(false)
                        }
                        if (file.isDirectory) {
                            isfile = false
                            cutmode = true
                            copyPath = Path
                            dealFabInfo(false)
                        }
                        Toast.makeText(this@MainActivity, items[which], Toast.LENGTH_SHORT).show()
                    }
                    if (items[which] == "分享") {
                        val file = File(s.path)
                        MimeTypeUtils.instance.shareSingleFile(this@MainActivity, file)
                    }
                    if (items[which] == "重命名") {
                        val et = EditText(this@MainActivity)
                        AlertDialog.Builder(this@MainActivity).setTitle("请输入更改的名称").setView(et).setPositiveButton("确定") { dialogInterface, i ->
                            if (et.text.toString() != s.name) {
                                fileutil!!.renameFile(mCurPath, s.name, et.text.toString())
                                getFileDir(mCurPath)
                            } else {
                                Toast.makeText(this@MainActivity, "文件名相同", Toast.LENGTH_SHORT).show()
                            }
                        }.setNegativeButton("取消", null).show()
                    }
                    if (items[which] == "删除") {
                        fileutil!!.delete(s.path)
                        getFileDir(mCurPath)
                    }
                }
        dialog!!.show()
    }

    /**
     * 获取文件夹
     *
     * @param filePath
     */
    private fun getFileDir(filePath: String?) {
        mCurPath = filePath
        val file = File(mCurPath)
        val files = file.listFiles()
        fileList.clear()
        if (files == null) {
            Toast.makeText(this, "请获取权限！", Toast.LENGTH_SHORT).show()
            return
        }
        for (i in files.indices) {
            var j = 0
            val f = files[i]
            if (f.isFile) {
                j = 1
            }
            if (!f.name.startsWith(".")) {
                fileList.add(Fileit(f.name, j, f.path))
            } else {
                Log.d(TAG, "getFileDir: getName " + f.name)
            }
        }
        orderList = orderByName(fileList)
        finalList.clear()
        if (mCurPath != rootPath) {
            finalList.add(Fileit("返回根目录", 2, rootPath))
            finalList.add(Fileit("返回上一层", 3, getlast()))
        } else {
            allFiles.clear()
            allFiles.add(rootPath)
        }
        getlist(orderList, finalList)
        adapter!!.notifyDataSetChanged()
    }

    fun getlast(): String? {
        return if (allFiles.size <= 1) {
            "/storage/emulated/0"
        } else {
            allFiles[allFiles.size - 2]
        }
    }

    /**
     * 搜搜文件
     *
     * @param name
     */
    private fun searchFile(name: String) {
        val file = File(rootPath)
        val files = file.listFiles()
        fileList.clear()
        if (files == null) {
            Toast.makeText(this, "请获取权限！", Toast.LENGTH_SHORT).show()
            return
        }
        for (i in files.indices) {
            var j = 0
            val f = files[i]
            if (f.isFile) {
                j = 1
            }
            if (!f.name.startsWith(".") && f.name.contains(name)) {
                fileList.add(Fileit(f.name, j, f.path))
            } else {
                Log.d(TAG, "getFileDir: getName " + f.name)
            }
        }
        orderList = orderByName(fileList)
        finalList.clear()
        allFiles.clear()
        allFiles.add(rootPath)
        getlist(orderList, finalList)
        adapter!!.notifyDataSetChanged()
    }

    /**
     * 拷贝文件
     *
     * @return
     */
    fun copyFile(oldName: String?, newName: String?): Boolean {
        return try {
            val oldFile = File(oldName)
            if (!oldFile.exists()) {
                Log.e("--Method--", "copyFile:  oldFile not exist.")
                return false
            } else if (!oldFile.isFile) {
                Log.e("--Method--", "copyFile:  oldFile not file.")
                return false
            } else if (!oldFile.canRead()) {
                Log.e("--Method--", "copyFile:  oldFile cannot read.")
                return false
            }
            val fileInputStream = FileInputStream(oldName) //读入原文件
            val fileOutputStream = FileOutputStream("$newName/$filename")
            val buffer = ByteArray(1024)
            var byteRead: Int
            while (fileInputStream.read(buffer).also { byteRead = it } != -1) {
                fileOutputStream.write(buffer, 0, byteRead)
            }
            fileInputStream.close()
            fileOutputStream.flush()
            fileOutputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 拷贝文件夹
     *
     * @param oldPath
     * @param newPath
     * @return
     */
    fun copyFolder(oldPath: String?, newPath: String?): Boolean {
        return try {
            val newFile = File(newPath)
            if (!newFile.exists()) {
                if (!newFile.mkdirs()) {
                    Log.e("--Method--", "copyFolder: cannot create directory.")
                    return false
                }
            }
            val oldFile = File(oldPath)
            val files = oldFile.list()
            var temp: File
            for (file in files) {
                temp = if (oldPath!!.endsWith(File.separator)) {
                    File(oldPath + file)
                } else {
                    File(oldPath + File.separator + file)
                }
                if (temp.isDirectory) { //如果是子文件夹
                    copyFolder("$oldPath/$file", "$newPath/$file")
                } else if (!temp.exists()) {
                    Log.e("--Method--", "copyFolder:  oldFile not exist.")
                    return false
                } else if (!temp.isFile) {
                    Log.e("--Method--", "copyFolder:  oldFile not file.")
                    return false
                } else if (!temp.canRead()) {
                    Log.e("--Method--", "copyFolder:  oldFile cannot read.")
                    return false
                } else {
                    val fileInputStream = FileInputStream(temp)
                    val fileOutputStream = FileOutputStream(newPath + "/" + temp.name)
                    val buffer = ByteArray(1024)
                    var byteRead: Int
                    while (fileInputStream.read(buffer).also { byteRead = it } != -1) {
                        fileOutputStream.write(buffer, 0, byteRead)
                    }
                    fileInputStream.close()
                    fileOutputStream.flush()
                    fileOutputStream.close()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 获取根目录
     *
     * @return
     */
    private fun getRootPath(): String? {
        return try {
            val rootPath: String
            Log.d(TAG, "getRootPath: 正在获取内置SD卡根目录")
            rootPath = Environment.getExternalStorageDirectory()
                    .toString()
            Log.d(TAG, "getRootPath: 内置SD卡目录为:$rootPath")
            rootPath
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 排序
     *
     * @param list
     * @param flist
     */
    fun getlist(list: List<Fileit>, flist: MutableList<Fileit>) {
        for (file2 in list) {
            flist.add(Fileit(file2.name, file2.imageId, file2.path))
        }
    }

    /**
     * 退出
     */
    private fun exit() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - mExitTime > 2000) {
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show()
            mExitTime = currentTime
        } else {
            Process.killProcess(Process.myPid())
            System.exit(0)
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName + "--->"
        fun orderByName(filelist: List<Fileit>): List<Fileit> {
            val FileNameList: MutableList<Fileit> = ArrayList()
            Collections.sort(filelist, Comparator { o1, o2 ->
                if (o1.imageId == 0 && o2.imageId == 1) return@Comparator -1
                if (o1.imageId == 1 && o2.imageId == 0) 1 else o1.name!!.compareTo(o2.name!!)
            })
            for (file1 in filelist) {
                FileNameList.add(Fileit(file1.name, file1.imageId, file1.path))
            }
            return FileNameList
        }
    }
}