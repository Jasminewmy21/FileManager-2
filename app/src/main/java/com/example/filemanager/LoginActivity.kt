package com.example.filemanager

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {
    var dbHelper: DBHelper? = null
    private  lateinit var login_name_et: EditText
    private  lateinit var login_code_et: EditText
    private  lateinit var tv_register: TextView
    private  lateinit var btn_login: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_login)
        dbHelper = DBHelper(this)
        login_name_et = findViewById(R.id.login_name_et)
        login_code_et = findViewById(R.id.login_code_et)
        tv_register = findViewById(R.id.tv_register)
        btn_login = findViewById(R.id.btn_login)
        val oldName = SPUtil.get(this, "name", "") as String
        val oldPwd = SPUtil.get(this, "pwd", "") as String
        if (!TextUtils.isEmpty(oldName) && !TextUtils.isEmpty(oldPwd)) {
            val user = dbHelper!!.login(oldName, oldPwd)
            if (user != null) {
                UserManager.instance.user = user
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        tv_register.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            intent.putExtra("Register", true)
            startActivity(intent)
        })
        btn_login.setOnClickListener(View.OnClickListener {
            val name = login_name_et.getText().toString().trim { it <= ' ' }
            val pwd = login_code_et.getText().toString().trim { it <= ' ' }
            if (name == null || TextUtils.isEmpty(name)) {
                Toast.makeText(this@LoginActivity, "请输入用户名", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (pwd == null || TextUtils.isEmpty(pwd)) {
                Toast.makeText(this@LoginActivity, "请输入密码", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            val user = dbHelper!!.login(name, pwd)
            if (user != null) {
                UserManager.instance.user = user
                SPUtil.put(this@LoginActivity, "name", name)
                SPUtil.put(this@LoginActivity, "pwd", pwd)
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this@LoginActivity, "登录失败，未查询到此用户", Toast.LENGTH_SHORT).show()
            }
        })
    }
}