package com.sungbin.reply.bot.dto

class DebugItem {
    var name: String? = null
    var msg: String? = null
    var gravity: Int? = null

    constructor() {}
    constructor(name: String?, msg: String?, gravity: Int?) {
        this.name = name
        this.msg = msg
        this.gravity = gravity
    }
}