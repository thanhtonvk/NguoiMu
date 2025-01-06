package com.tondz.nguoimu;

import com.tondz.nguoimu.models.CauHoi;

import java.util.ArrayList;
import java.util.List;

public class Common {
    public static String[] classNames;
    public static CauHoi CAU_HOI = new CauHoi();
    public static List<CauHoi> cauHoiArrayList = new ArrayList<>();
    public static List<CauHoi> cauHoiDaTraLoi = new ArrayList<>();
    public static List<String> dapanChon = new ArrayList<>();
    public static String[] emotionClasses = {
            "tức giận", "ghê tởm", "sợ", "vui vẻ", "buồn", "bất ngờ", "tự nhiên", "khinh miệt"
    };
    public static int ngonNgu = 0;
    public static String[] languages = {"vi-VN", "en-US", "zh-CN"};
}
