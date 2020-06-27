package com.example.filemanager

import android.app.AlertDialog
import android.os.Bundle
import android.os.Process
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.*
import com.example.filemanager.RegisterActivity

/**
 * @desc:
 * @datetime: 2020/6/21
 */
class RegisterActivity : AppCompatActivity() {
    private lateinit var ivBack: ImageView
    private lateinit var btnRegister: Button
    private lateinit  var btn_update: Button
    private  lateinit var tv_title: TextView
    private lateinit  var ed_useName: EditText
    private lateinit  var ed_password: EditText
    var dbHelper: DBHelper? = null
    private var isRegister = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_register)
        isRegister = intent.getBooleanExtra("Register", true)
        ivBack = findViewById(R.id.iv_back)
        tv_title = findViewById(R.id.tv_title)
        btn_update = findViewById(R.id.btn_update)
        btnRegister = findViewById(R.id.btn_register)
        ed_useName = findViewById(R.id.ed_useName)
        ed_password = findViewById(R.id.ed_password)
        dbHelper = DBHelper(this)
        if (isRegister) {
            tv_title.setText("注册账户")
            btnRegister.setText("注册")
            btn_update.setVisibility(View.GONE)
        } else {
            btn_update.setVisibility(View.VISIBLE)
            tv_title.setText("编辑个人信息")
            btnRegister.setText("退出登录")
            val user = UserManager.instance.user
            ed_useName.setText(user!!.name)
            ed_password.setText(user.pwd)
        }
        ivBack.setOnClickListener(View.OnClickListener { finish() })
        btn_update.setOnClickListener(View.OnClickListener {
            val name = ed_useName.getText().toString().trim { it <= ' ' }
            if (name == null || TextUtils.isEmpty(name)) {
                Toast.makeText(this@RegisterActivity, "请输入用户名", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            val pwd = ed_password.getText().toString().trim { it <= ' ' }
            if (pwd == null || TextUtils.isEmpty(pwd)) {
                Toast.makeText(this@RegisterActivity, "请输入密码", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            val user = UserManager.instance.user
            val isRit = dbHelper!!.updateUser(pwd, name, user!!.id)
            if (isRit) {
                user.pwd = pwd
                user.name = name
                UserManager.instance.user = user
                SPUtil.put(this@RegisterActivity, "name", name)
                SPUtil.put(this@RegisterActivity, "pwd", pwd)
                finish()
                Toast.makeText(this@RegisterActivity, "变更成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@RegisterActivity, "变更失败", Toast.LENGTH_SHORT).show()
            }
        })
        btnRegister.setOnClickListener(View.OnClickListener {
            if (isRegister) {
                val name = ed_useName.getText().toString().trim { it <= ' ' }
                if (name == null || TextUtils.isEmpty(name)) {
                    Toast.makeText(this@RegisterActivity, "请输入用户名", Toast.LENGTH_SHORT).show()
                    return@OnClickListener
                }
                val pwd = ed_password.getText().toString().trim { it <= ' ' }
                if (pwd == null || TextUtils.isEmpty(pwd)) {
                    Toast.makeText(this@RegisterActivity, "请输入密码", Toast.LENGTH_SHORT).show()
                    return@OnClickListener
                }
                val isRit = dbHelper!!.register(pwd, name)
                if (isRit) {
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, "注册失败,请重新注册", Toast.LENGTH_SHORT).show()
                }
            } else { //退出登录
                AlertDialog.Builder(this@RegisterActivity)
                        .setTitle("退出登录")
                        .setPositiveButton("确定") { dialog, which ->
                            SPUtil.put(this@RegisterActivity, "name", "")
                            SPUtil.put(this@RegisterActivity, "pwd", "")
                            object : Thread() {
                                override fun run() {
                                    super.run()
                                    try {
                                        sleep(300)
                                        Process.killProcess(Process.myPid())
                                        System.exit(0)
                                    } catch (e: InterruptedException) {
                                        e.printStackTrace()
                                    }
                                }
                            }.start()
                        }
                        .setNegativeButton("取消", null)
                        .show()
            }
        })
    }
}