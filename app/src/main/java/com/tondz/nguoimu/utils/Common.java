package com.tondz.nguoimu.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

public class Common {
    public static String[] listObject = {
            "người", "xe đạp", "ô tô", "xe máy", "máy bay", "xe buýt", "tàu hỏa", "xe tải", "thuyền", "đèn giao thông",
            "vòi cứu hỏa", "biển báo dừng", "đồng hồ đỗ xe", "ghế dài", "chim", "mèo", "chó", "ngựa", "cừu", "bò",
            "voi", "gấu", "ngựa vằn", "hươu cao cổ", "ba lô", "ô", "túi xách", "cà vạt", "va li", "đĩa ném đĩa",
            "ván trượt tuyết", "ván trượt tuyết", "bóng thể thao", "diều", "gậy bóng chày", "găng tay bóng chày", "ván trượt", "ván lướt sóng",
            "vợt tennis", "chai", "ly rượu", "cốc", "dĩa", "dao", "thìa", "bát", "chuối", "táo",
            "sandwich", "cam", "bông cải xanh", "cà rốt", "xúc xích", "pizza", "bánh rán", "bánh ngọt", "ghế", "đi văng",
            "chậu cây", "giường", "bàn ăn", "nhà vệ sinh", "ti vi", "máy tính xách tay", "chuột", "điều khiển từ xa", "bàn phím", "điện thoại di động",
            "lò vi sóng", "lò nướng", "máy nướng bánh mì", "bồn rửa", "tủ lạnh", "sách", "đồng hồ", "bình hoa", "kéo", "gấu bông",
            "máy sấy tóc", "bàn chải đánh răng"
    };
    public static String[] moneys = {"100 nghìn",
            "10 nghìn",
            "1 nghìn",
            "200 nghìn",
            "20 nghìn",
            "2 nghìn",
            "500 nghìn",
            "50 nghìn",
            "5 nghìn"};
    public static String[] lightTraffic = {"Xanh", "Đỏ", "Vàng"};

    public static String convertArrayToString(double[] list) {
        String result = "";
        for (int i = 0; i < list.length; i++) {
            result += CalDistance.widthInImages[i] + ",";
        }
        return result;
    }

    public static String[] side = {"bên trái", "bên phải", "phía trên", "phía dưới", "ở giữa"};

    public static double[] xywhToCenter(double x, double y, double w, double h) {
        double centerX = x + w / 2;
        double centerY = y + h / 2;
        return new double[]{centerX, centerY};
    }

    public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }


}
