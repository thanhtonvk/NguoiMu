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
                "Ai là người phát minh ra bóng đèn?",
                "1.  Albert Einstein",
                "2.  Thomas Edison",
                "3.  Nikola Tesla",
                "4.  Isaac Newton",
                "2.  Thomas Edison",
                "Thomas Edison là người phát minh và phát triển bóng đèn sợi đốt."
        ));

        cauHoiArrayList.add(new CauHoi(
                "2",
                "Thủ đô của nước Pháp là gì?",
                "1.  Rome",
                "2.  Madrid",
                "3.  Berlin",
                "4.  Paris",
                "4.  Paris",
                "Thủ đô của nước Pháp là Paris."
        ));

        cauHoiArrayList.add(new CauHoi(
                "3",
                "Nguyên tố hóa học nào có ký hiệu là O?",
                "1.  Oxy",
                "2.  Vàng",
                "3.  Bạc",
                "4.  Sắt",
                "1.  Oxy",
                "Nguyên tố hóa học có ký hiệu O là Oxy."
        ));

        cauHoiArrayList.add(new CauHoi(
                "4",
                "Số Pi (π) xấp xỉ bằng bao nhiêu?",
                "1.  2.14",
                "2.  3.14",
                "3.  4.14",
                "4.  5.14",
                "2.  3.14",
                "Số Pi (π) xấp xỉ bằng 3.14."
        ));

        cauHoiArrayList.add(new CauHoi(
                "5",
                "Quốc gia nào có diện tích lớn nhất thế giới?",
                "1.  Mỹ",
                "2.  Trung Quốc",
                "3.  Nga",
                "4.  Canada",
                "3.  Nga",
                "Nga là quốc gia có diện tích lớn nhất thế giới."
        ));

        cauHoiArrayList.add(new CauHoi(
                "6",
                "Ai là tác giả của tác phẩm 'Truyện Kiều'?",
                "1.  Nguyễn Du",
                "2.  Nguyễn Trãi",
                "3.  Hồ Xuân Hương",
                "4.  Nguyễn Bỉnh Khiêm",
                "1.  Nguyễn Du",
                "Nguyễn Du là tác giả của 'Truyện Kiều'."
        ));

        cauHoiArrayList.add(new CauHoi(
                "7",
                "Hành tinh nào trong hệ Mặt Trời có kích thước lớn nhất?",
                "1.  Sao Hỏa",
                "2.  Sao Kim",
                "3.  Sao Mộc",
                "4.  Sao Thủy",
                "3.  Sao Mộc",
                "Sao Mộc là hành tinh lớn nhất trong hệ Mặt Trời."
        ));

        cauHoiArrayList.add(new CauHoi(
                "8",
                "Ai là nhà bác học tìm ra định luật vạn vật hấp dẫn?",
                "1.  Albert Einstein",
                "2.  Isaac Newton",
                "3.  Galileo Galilei",
                "4.  Archimedes",
                "2.  Isaac Newton",
                "Isaac Newton là người tìm ra định luật vạn vật hấp dẫn."
        ));

        cauHoiArrayList.add(new CauHoi(
                "9",
                "Nước nào có dân số đông nhất thế giới?",
                "1.  Mỹ",
                "2.  Ấn Độ",
                "3.  Trung Quốc",
                "4.  Brazil",
                "3.  Trung Quốc",
                "Trung Quốc là quốc gia có dân số đông nhất thế giới."
        ));

        cauHoiArrayList.add(new CauHoi(
                "10",
                "Châu lục nào có diện tích nhỏ nhất?",
                "1.  Châu Á",
                "2.  Châu Âu",
                "3.  Châu Đại Dương",
                "4.  Châu Nam Cực",
                "3.  Châu Đại Dương",
                "Châu Đại Dương là châu lục có diện tích nhỏ nhất."
        ));

        cauHoiArrayList.add(new CauHoi(
                "11",
                "Đơn vị đo cường độ dòng điện là gì?",
                "1.  Vôn",
                "2.  Watt",
                "3.  Ampe",
                "4.  Ôm",
                "3.  Ampe",
                "Ampe là đơn vị đo cường độ dòng điện."
        ));

        cauHoiArrayList.add(new CauHoi(
                "12",
                "Ai là tổng thống đầu tiên của Hoa Kỳ?",
                "1.  Abraham Lincoln",
                "2.  George Washington",
                "3.  Thomas Jefferson",
                "4.  John Adams",
                "2.  George Washington",
                "George Washington là tổng thống đầu tiên của Hoa Kỳ."
        ));

        cauHoiArrayList.add(new CauHoi(
                "13",
                "Tác phẩm 'Lão Hạc' là của nhà văn nào?",
                "1.  Nam Cao",
                "2.  Ngô Tất Tố",
                "3.  Nguyễn Công Hoan",
                "4.  Thạch Lam",
                "1.  Nam Cao",
                "Nam Cao là tác giả của 'Lão Hạc'."
        ));

        cauHoiArrayList.add(new CauHoi(
                "14",
                "Động vật nào được coi là loài có vú lớn nhất?",
                "1.  Cá voi xanh",
                "2.  Voi châu Phi",
                "3.  Gấu Bắc Cực",
                "4.  Hươu cao cổ",
                "1.  Cá voi xanh",
                "Cá voi xanh là loài động vật có vú lớn nhất trên Trái Đất."
        ));

        cauHoiArrayList.add(new CauHoi(
                "15",
                "Hệ điều hành Android được phát triển bởi công ty nào?",
                "1.  Apple",
                "2.  Google",
                "3.  Microsoft",
                "4.  Samsung",
                "2.  Google",
                "Android được phát triển bởi Google."
        ));

        cauHoiArrayList.add(new CauHoi(
                "16",
                "Ngọn núi cao nhất thế giới là gì?",
                "1.  Kilimanjaro",
                "2.  Everest",
                "3.  Phú Sĩ",
                "4.  K2",
                "2.  Everest",
                "Everest là ngọn núi cao nhất thế giới."
        ));

        cauHoiArrayList.add(new CauHoi(
                "17",
                "Năm 1945, Việt Nam tuyên bố độc lập vào ngày nào?",
                "1.  19/8",
                "2.  2/9",
                "3.  30/4",
                "4.  7/5",
                "2.  2/9",
                "Ngày 2/9/1945, Chủ tịch Hồ Chí Minh đọc Tuyên ngôn Độc lập."
        ));

        cauHoiArrayList.add(new CauHoi(
                "18",
                "Loại khí nào chiếm phần lớn trong khí quyển Trái Đất?",
                "1.  Oxy",
                "2.  Carbon dioxide",
                "3.  Nitơ",
                "4.  Hidro",
                "3.  Nitơ",
                "Nitơ chiếm khoảng 78% trong khí quyển Trái Đất."
        ));

        cauHoiArrayList.add(new CauHoi(
                "19",
                "Nước Việt Nam có bao nhiêu tỉnh thành?",
                "1.  58",
                "2.  63",
                "3.  64",
                "4.  65",
                "2.  63",
                "Việt Nam có 63 tỉnh và thành phố trực thuộc trung ương."
        ));

        cauHoiArrayList.add(new CauHoi(
                "20",
                "Vị vua đầu tiên của Việt Nam là ai?",
                "1.  Lý Công Uẩn",
                "2.  Trần Nhân Tông",
                "3.  Gia Long",
                "4.  Đinh Tiên Hoàng",
                "4.  Đinh Tiên Hoàng",
                "Đinh Tiên Hoàng là vị vua đầu tiên của Việt Nam, lập ra nhà Đinh."
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
