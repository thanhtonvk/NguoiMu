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
                "A. Albert Einstein",
                "B. Thomas Edison",
                "C. Nikola Tesla",
                "D. Isaac Newton",
                "B. Thomas Edison",
                "Thomas Edison là người phát minh và phát triển bóng đèn sợi đốt."
        ));

        cauHoiArrayList.add(new CauHoi(
                "2",
                "Thủ đô của nước Pháp là gì?",
                "A. Rome",
                "B. Madrid",
                "C. Berlin",
                "D. Paris",
                "D. Paris",
                "Thủ đô của nước Pháp là Paris."
        ));

        cauHoiArrayList.add(new CauHoi(
                "3",
                "Nguyên tố hóa học nào có ký hiệu là O?",
                "A. Oxy",
                "B. Vàng",
                "C. Bạc",
                "D. Sắt",
                "A. Oxy",
                "Nguyên tố hóa học có ký hiệu O là Oxy."
        ));

        cauHoiArrayList.add(new CauHoi(
                "4",
                "Số Pi (π) xấp xỉ bằng bao nhiêu?",
                "A. 2.14",
                "B. 3.14",
                "C. 4.14",
                "D. 5.14",
                "B. 3.14",
                "Số Pi (π) xấp xỉ bằng 3.14."
        ));

        cauHoiArrayList.add(new CauHoi(
                "5",
                "Quốc gia nào có diện tích lớn nhất thế giới?",
                "A. Mỹ",
                "B. Trung Quốc",
                "C. Nga",
                "D. Canada",
                "C. Nga",
                "Nga là quốc gia có diện tích lớn nhất thế giới."
        ));

        cauHoiArrayList.add(new CauHoi(
                "6",
                "Ai là tác giả của tác phẩm 'Truyện Kiều'?",
                "A. Nguyễn Du",
                "B. Nguyễn Trãi",
                "C. Hồ Xuân Hương",
                "D. Nguyễn Bỉnh Khiêm",
                "A. Nguyễn Du",
                "Nguyễn Du là tác giả của 'Truyện Kiều'."
        ));

        cauHoiArrayList.add(new CauHoi(
                "7",
                "Hành tinh nào trong hệ Mặt Trời có kích thước lớn nhất?",
                "A. Sao Hỏa",
                "B. Sao Kim",
                "C. Sao Mộc",
                "D. Sao Thủy",
                "C. Sao Mộc",
                "Sao Mộc là hành tinh lớn nhất trong hệ Mặt Trời."
        ));

        cauHoiArrayList.add(new CauHoi(
                "8",
                "Ai là nhà bác học tìm ra định luật vạn vật hấp dẫn?",
                "A. Albert Einstein",
                "B. Isaac Newton",
                "C. Galileo Galilei",
                "D. Archimedes",
                "B. Isaac Newton",
                "Isaac Newton là người tìm ra định luật vạn vật hấp dẫn."
        ));

        cauHoiArrayList.add(new CauHoi(
                "9",
                "Nước nào có dân số đông nhất thế giới?",
                "A. Mỹ",
                "B. Ấn Độ",
                "C. Trung Quốc",
                "D. Brazil",
                "C. Trung Quốc",
                "Trung Quốc là quốc gia có dân số đông nhất thế giới."
        ));

        cauHoiArrayList.add(new CauHoi(
                "10",
                "Châu lục nào có diện tích nhỏ nhất?",
                "A. Châu Á",
                "B. Châu Âu",
                "C. Châu Đại Dương",
                "D. Châu Nam Cực",
                "C. Châu Đại Dương",
                "Châu Đại Dương là châu lục có diện tích nhỏ nhất."
        ));

        cauHoiArrayList.add(new CauHoi(
                "11",
                "Đơn vị đo cường độ dòng điện là gì?",
                "A. Vôn",
                "B. Watt",
                "C. Ampe",
                "D. Ôm",
                "C. Ampe",
                "Ampe là đơn vị đo cường độ dòng điện."
        ));

        cauHoiArrayList.add(new CauHoi(
                "12",
                "Ai là tổng thống đầu tiên của Hoa Kỳ?",
                "A. Abraham Lincoln",
                "B. George Washington",
                "C. Thomas Jefferson",
                "D. John Adams",
                "B. George Washington",
                "George Washington là tổng thống đầu tiên của Hoa Kỳ."
        ));

        cauHoiArrayList.add(new CauHoi(
                "13",
                "Tác phẩm 'Lão Hạc' là của nhà văn nào?",
                "A. Nam Cao",
                "B. Ngô Tất Tố",
                "C. Nguyễn Công Hoan",
                "D. Thạch Lam",
                "A. Nam Cao",
                "Nam Cao là tác giả của 'Lão Hạc'."
        ));

        cauHoiArrayList.add(new CauHoi(
                "14",
                "Động vật nào được coi là loài có vú lớn nhất?",
                "A. Cá voi xanh",
                "B. Voi châu Phi",
                "C. Gấu Bắc Cực",
                "D. Hươu cao cổ",
                "A. Cá voi xanh",
                "Cá voi xanh là loài động vật có vú lớn nhất trên Trái Đất."
        ));

        cauHoiArrayList.add(new CauHoi(
                "15",
                "Hệ điều hành Android được phát triển bởi công ty nào?",
                "A. Apple",
                "B. Google",
                "C. Microsoft",
                "D. Samsung",
                "B. Google",
                "Android được phát triển bởi Google."
        ));

        cauHoiArrayList.add(new CauHoi(
                "16",
                "Ngọn núi cao nhất thế giới là gì?",
                "A. Kilimanjaro",
                "B. Everest",
                "C. Phú Sĩ",
                "D. K2",
                "B. Everest",
                "Everest là ngọn núi cao nhất thế giới."
        ));

        cauHoiArrayList.add(new CauHoi(
                "17",
                "Năm 1945, Việt Nam tuyên bố độc lập vào ngày nào?",
                "A. 19/8",
                "B. 2/9",
                "C. 30/4",
                "D. 7/5",
                "B. 2/9",
                "Ngày 2/9/1945, Chủ tịch Hồ Chí Minh đọc Tuyên ngôn Độc lập."
        ));

        cauHoiArrayList.add(new CauHoi(
                "18",
                "Loại khí nào chiếm phần lớn trong khí quyển Trái Đất?",
                "A. Oxy",
                "B. Carbon dioxide",
                "C. Nitơ",
                "D. Hidro",
                "C. Nitơ",
                "Nitơ chiếm khoảng 78% trong khí quyển Trái Đất."
        ));

        cauHoiArrayList.add(new CauHoi(
                "19",
                "Nước Việt Nam có bao nhiêu tỉnh thành?",
                "A. 58",
                "B. 63",
                "C. 64",
                "D. 65",
                "B. 63",
                "Việt Nam có 63 tỉnh và thành phố trực thuộc trung ương."
        ));

        cauHoiArrayList.add(new CauHoi(
                "20",
                "Vị vua đầu tiên của Việt Nam là ai?",
                "A. Lý Công Uẩn",
                "B. Trần Nhân Tông",
                "C. Gia Long",
                "D. Đinh Tiên Hoàng",
                "D. Đinh Tiên Hoàng",
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
