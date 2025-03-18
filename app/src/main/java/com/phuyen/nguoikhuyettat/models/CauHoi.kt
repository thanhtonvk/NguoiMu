package com.phuyen.nguoikhuyettat.models

class CauHoi {
    var id: String? = null
    @kotlin.jvm.JvmField
    var cauhoi: String? = null
    var a: String? = null
    var b: String? = null
    var c: String? = null
    var d: String? = null
    var dapan: String? = null
    var giaithich: String? = null

    constructor(
        id: String?,
        cauhoi: String?,
        a: String?,
        b: String?,
        c: String?,
        d: String?,
        dapan: String?,
        giaithich: String?
    ) {
        this.id = id
        this.cauhoi = cauhoi
        this.a = a
        this.b = b
        this.c = c
        this.d = d
        this.dapan = dapan
        this.giaithich = giaithich
    }

    constructor()

    override fun toString(): String {
        return cauhoi!!
    }
}

