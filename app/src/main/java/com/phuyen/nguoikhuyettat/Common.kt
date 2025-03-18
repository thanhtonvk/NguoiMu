package com.phuyen.nguoikhuyettat

import com.phuyen.nguoikhuyettat.models.CauHoi

object Common {
    lateinit var classNames: Array<String>
    var CAU_HOI: CauHoi = CauHoi()
    var cauHoiArrayList: List<CauHoi> = ArrayList()
    var cauHoiDaTraLoi: List<CauHoi> = ArrayList()
    var dapanChon: List<String> = ArrayList()
    var emotionClasses: Array<String> = arrayOf(
        "tức giận", "ghê tởm", "sợ", "vui vẻ", "buồn", "bất ngờ", "tự nhiên", "khinh miệt"
    )
    var ngonNgu: Int = 0
    var languages: Array<String> = arrayOf("vi-VN", "en-US", "zh-CN")
}
