package com.example.filemanager

/**
 * @name: User
 * @date: 2020-05-18 10:28
 * @comment: 用户信息
 */
class User {
    var id = 0
    var name: String? = null
    var pwd: String? = null
    override fun toString(): String {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", pwd='" + pwd + '\'' +
                '}'
    }
}