package tools

import android.graphics.Bitmap

class Msg {

    var id: Int = 0
    var userid: String? = null
    var nickname: String? = null
    var time: String? = null
    var content: String? = null
    var portrait: Bitmap? = null
    var hasPic: Int = 0
    private var picture: Array<Bitmap>? = null

    constructor() {
        this.hasPic = 0
    }

    constructor(id: Int, userid: String, nickname: String, time: String, content: String, portrait: Bitmap, hasPic: Int, picture: Array<Bitmap>) {
        this.id = id
        this.userid = userid
        this.nickname = nickname
        this.time = time
        this.content = content
        this.portrait = portrait
        this.hasPic = hasPic
        this.picture = picture
    }

    constructor(id: Int, userid: String, nickname: String, time: String, content: String, portrait: Bitmap) {
        this.id = id
        this.userid = userid
        this.nickname = nickname
        this.time = time
        this.content = content
        this.portrait = portrait
        this.hasPic = 0
    }

    constructor(userid: String, nickname: String, time: String, content: String, portrait: Bitmap) {
        this.userid = userid
        this.nickname = nickname
        this.time = time
        this.content = content
        this.portrait = portrait
        this.hasPic = 0
    }

    constructor(userid: String, nickname: String, time: String, content: String) {
        this.userid = userid
        this.nickname = nickname
        this.time = time
        this.content = content
        this.hasPic = 0
    }

    fun getPicture(): Array<Bitmap>? {
        return picture
    }

    fun setPicture(picture: Array<Bitmap>?) {
        this.picture = picture
        this.hasPic = picture?.size ?: 0
    }
}
