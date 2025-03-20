package com.tondz.nguoimu;

import com.tondz.nguoimu.models.CauHoi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final Map<String, List<String>> gestureEmotionRules = new HashMap<>();

    static {
        gestureEmotionRules.put("cám ơn", Arrays.asList("tự nhiên", "vui vẻ"));
        gestureEmotionRules.put("hẹn gặp lại", Arrays.asList("vui vẻ", "tự nhiên"));
        gestureEmotionRules.put("khỏe", Arrays.asList("tự nhiên", "vui vẻ"));
        gestureEmotionRules.put("không thích", Arrays.asList("khinh miệt", "ghê tởm"));
        gestureEmotionRules.put("rất vui được gặp bạn", Arrays.asList("vui vẻ", "bất ngờ"));
        gestureEmotionRules.put("sợ", Arrays.asList("sợ", "bất ngờ"));
        gestureEmotionRules.put("tạm biệt", Arrays.asList("tự nhiên", "buồn"));
        gestureEmotionRules.put("thích", Arrays.asList("vui vẻ", "tự nhiên"));
        gestureEmotionRules.put("xin chào", Arrays.asList("vui vẻ", "tự nhiên"));
        gestureEmotionRules.put("xin lỗi", Arrays.asList("buồn", "tự nhiên"));
        gestureEmotionRules.put("biết", Arrays.asList("tự nhiên"));
        gestureEmotionRules.put("nhớ", Arrays.asList("buồn", "tự nhiên"));
        gestureEmotionRules.put("tò mò", Arrays.asList("bất ngờ", "tự nhiên"));
        gestureEmotionRules.put("yêu", Arrays.asList("vui vẻ", "tự nhiên"));
        gestureEmotionRules.put("giúp đỡ", Arrays.asList("tự nhiên", "vui vẻ"));
        gestureEmotionRules.put("khen", Arrays.asList("vui vẻ", "tự nhiên"));
        gestureEmotionRules.put("lắng nghe", Arrays.asList("tự nhiên"));
        gestureEmotionRules.put("năn nỉ", Arrays.asList("buồn", "tự nhiên"));
    }

    public static String getMatchingGesture(String gesture, String emotion) {
        if (gestureEmotionRules.containsKey(gesture)) {
            List<String> emotions = gestureEmotionRules.get(gesture);
            if (emotions.contains(emotion)) {
                return gesture;
            }
        }
        return "";
    }

}
