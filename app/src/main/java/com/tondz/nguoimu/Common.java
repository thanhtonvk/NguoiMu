package com.tondz.nguoimu;

import com.tondz.nguoimu.models.CauHoi;

import java.util.ArrayList;
import java.util.List;

public class Common {
    public static String[] classNames;
    public static CauHoi CAU_HOI = new CauHoi();
    public static List<CauHoi> cauHoiArrayList = new ArrayList<>();
    static {
        cauHoiArrayList.add(new CauHoi(
                "1",
                "Ai là người đầu tiên đặt chân lên Mặt Trăng?",
                "A. Neil Armstrong",
                "B. Buzz Aldrin",
                "C. Yuri Gagarin",
                "D. John Glenn",
                "A. Neil Armstrong",
                "Neil Armstrong là người đầu tiên đặt chân lên Mặt Trăng vào năm 1969."
        ));

        cauHoiArrayList.add(new CauHoi(
                "2",
                "Quốc gia nào có nền kinh tế lớn nhất thế giới?",
                "A. Trung Quốc",
                "B. Nhật Bản",
                "C. Mỹ",
                "D. Đức",
                "C. Mỹ",
                "Mỹ có nền kinh tế lớn nhất thế giới."
        ));

        cauHoiArrayList.add(new CauHoi(
                "3",
                "Đơn vị đo tần số trong hệ SI là gì?",
                "A. Giây",
                "B. Héc (Hz)",
                "C. Jun (J)",
                "D. Newton (N)",
                "B. Héc (Hz)",
                "Tần số được đo bằng đơn vị Héc (Hz)."
        ));

        cauHoiArrayList.add(new CauHoi(
                "4",
                "Quốc gia nào nổi tiếng với tháp nghiêng Pisa?",
                "A. Tây Ban Nha",
                "B. Pháp",
                "C. Ý",
                "D. Đức",
                "C. Ý",
                "Tháp nghiêng Pisa nằm ở Ý."
        ));

        cauHoiArrayList.add(new CauHoi(
                "5",
                "Ai là người sáng lập ra Microsoft?",
                "A. Steve Jobs",
                "B. Mark Zuckerberg",
                "C. Bill Gates",
                "D. Elon Musk",
                "C. Bill Gates",
                "Bill Gates và Paul Allen đã sáng lập Microsoft vào năm 1975."
        ));

        cauHoiArrayList.add(new CauHoi(
                "6",
                "Người Việt Nam đầu tiên bay vào vũ trụ là ai?",
                "A. Phạm Tuân",
                "B. Trịnh Công Sơn",
                "C. Nguyễn Văn Cừ",
                "D. Võ Nguyên Giáp",
                "A. Phạm Tuân",
                "Phạm Tuân là người Việt Nam đầu tiên bay vào vũ trụ vào năm 1980."
        ));

        cauHoiArrayList.add(new CauHoi(
                "7",
                "Hệ điều hành Windows 10 được phát hành vào năm nào?",
                "A. 2010",
                "B. 2015",
                "C. 2020",
                "D. 2007",
                "B. 2015",
                "Windows 10 được phát hành vào năm 2015."
        ));

        cauHoiArrayList.add(new CauHoi(
                "8",
                "Loài chim nào không biết bay?",
                "A. Đại bàng",
                "B. Cú mèo",
                "C. Chim cánh cụt",
                "D. Diều hâu",
                "C. Chim cánh cụt",
                "Chim cánh cụt không biết bay nhưng bơi rất giỏi."
        ));

        cauHoiArrayList.add(new CauHoi(
                "9",
                "Quả nào có nhiều vitamin C nhất?",
                "A. Táo",
                "B. Cam",
                "C. Chuối",
                "D. Dưa hấu",
                "B. Cam",
                "Cam là loại quả giàu vitamin C nhất."
        ));

        cauHoiArrayList.add(new CauHoi(
                "10",
                "Vạn Lý Trường Thành thuộc quốc gia nào?",
                "A. Nhật Bản",
                "B. Trung Quốc",
                "C. Ấn Độ",
                "D. Hàn Quốc",
                "B. Trung Quốc",
                "Vạn Lý Trường Thành là một công trình nổi tiếng của Trung Quốc."
        ));

        cauHoiArrayList.add(new CauHoi(
                "11",
                "Ai là tác giả của 'Đắc Nhân Tâm'?",
                "A. Dale Carnegie",
                "B. Napoleon Hill",
                "C. John Maxwell",
                "D. Robert Kiyosaki",
                "A. Dale Carnegie",
                "'Đắc Nhân Tâm' được viết bởi Dale Carnegie."
        ));

        cauHoiArrayList.add(new CauHoi(
                "12",
                "Sông nào dài nhất thế giới?",
                "A. Sông Nile",
                "B. Sông Amazon",
                "C. Sông Mississippi",
                "D. Sông Mekong",
                "A. Sông Nile",
                "Sông Nile là con sông dài nhất thế giới, nằm ở châu Phi."
        ));

        cauHoiArrayList.add(new CauHoi(
                "13",
                "Loài động vật nào có thể đổi màu da để ngụy trang?",
                "A. Bạch tuộc",
                "B. Tắc kè hoa",
                "C. Cá mập",
                "D. Sư tử",
                "B. Tắc kè hoa",
                "Tắc kè hoa có thể đổi màu để thích nghi với môi trường."
        ));

        cauHoiArrayList.add(new CauHoi(
                "14",
                "Chất nào làm cho ớt có vị cay?",
                "A. Axit Citric",
                "B. Capsaicin",
                "C. Glucose",
                "D. Protein",
                "B. Capsaicin",
                "Capsaicin là hợp chất tạo ra vị cay của ớt."
        ));

        cauHoiArrayList.add(new CauHoi(
                "15",
                "Loài động vật nào ngủ ít nhất?",
                "A. Ngựa",
                "B. Cá heo",
                "C. Voi",
                "D. Hươu cao cổ",
                "D. Hươu cao cổ",
                "Hươu cao cổ ngủ trung bình chỉ 2 tiếng mỗi ngày."
        ));

        cauHoiArrayList.add(new CauHoi(
                "16",
                "Nhạc sĩ nào sáng tác bài hát 'Happy Birthday'?",
                "A. Mozart",
                "B. Beethoven",
                "C. Mildred Hill & Patty Hill",
                "D. Taylor Swift",
                "C. Mildred Hill & Patty Hill",
                "Hai chị em Mildred Hill & Patty Hill sáng tác bài 'Happy Birthday'."
        ));

        cauHoiArrayList.add(new CauHoi(
                "17",
                "Hành tinh nào có nhiều vệ tinh nhất trong hệ Mặt Trời?",
                "A. Sao Mộc",
                "B. Sao Thổ",
                "C. Sao Hỏa",
                "D. Sao Kim",
                "B. Sao Thổ",
                "Sao Thổ có hơn 80 vệ tinh tự nhiên, nhiều nhất trong hệ Mặt Trời."
        ));

        cauHoiArrayList.add(new CauHoi(
                "18",
                "Quốc gia nào có kim tự tháp nổi tiếng?",
                "A. Ấn Độ",
                "B. Mexico",
                "C. Ai Cập",
                "D. Brazil",
                "C. Ai Cập",
                "Ai Cập là nơi có nhiều kim tự tháp nổi tiếng như Kim tự tháp Giza."
        ));

        cauHoiArrayList.add(new CauHoi(
                "19",
                "Bộ phim nào giành nhiều giải Oscar nhất?",
                "A. Titanic",
                "B. The Godfather",
                "C. Avatar",
                "D. Ben-Hur",
                "A. Titanic",
                "Titanic đã giành 11 giải Oscar, là một trong những bộ phim thành công nhất."
        ));

        cauHoiArrayList.add(new CauHoi(
                "20",
                "Ai là người vẽ bức tranh Mona Lisa?",
                "A. Van Gogh",
                "B. Michelangelo",
                "C. Leonardo da Vinci",
                "D. Picasso",
                "C. Leonardo da Vinci",
                "Leonardo da Vinci là tác giả của bức tranh Mona Lisa."
        ));
    }
    public static List<CauHoi> cauHoiDaTraLoi = new ArrayList<>();
    public static List<String> dapanChon = new ArrayList<>();
    public static String[] emotionClasses = {
            "tức giận", "ghê tởm", "sợ", "vui vẻ", "buồn", "bất ngờ", "tự nhiên", "khinh miệt"
    };
    public static int ngonNgu = 0;
    public static String[] languages = {"vi-VN", "en-US", "zh-CN"};
}
