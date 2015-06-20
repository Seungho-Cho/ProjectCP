package com.example.wearable.datalayerexample;

/**
 * Created by imac_06 on 15. 6. 20..
 */
public class GNodeList {

     static final int CATE_NODE = 1;
     static final int CATE_NORM = 2;
     static final int CATE_USEF = 4;
     static final int CATE_ENGI = 8;

    GNode[] GNodeArr = {
                new GNode(0, 1, null, null, 126.7320299, 37.3422762),
                new GNode(1, 1, null, null, 126.7315529, 37.3417747),
                new GNode(2, 1, null, null, 126.7321684, 37.3414226),
                new GNode(3, 1, null, null, 126.732261, 37.3415269),
                new GNode(4, 2, "TIP", new String[]{"TIP", "기숙사", "기술혁신파크"}, 126.7324044, 37.3413025),
                new GNode(5, 1, null, null, 126.7328204, 37.3416928),
                new GNode(6, 1, null, null, 126.7327854, 37.3410327),
                new GNode(7, 4, "농구장", new String[]{"농구장", "농구골대"}, 126.7328133, 37.3410663),
                new GNode(8, 1, null, null, 126.733123, 37.3408069),
                new GNode(9, 4, "체육관", new String[]{"체육관", "실내체육관"}, 126.7328133, 37.3410663),
                new GNode(10, 8, "A동 옆문", new String[]{"A동", "공학관", "A"}, 126.733102, 37.340649),
                new GNode(11, 1, null, null, 126.7330052, 37.340734),
                new GNode(12, 4, "주차타워", new String[]{"주차타워", "주차", "주차장"}, 126.7329518, 37.3407607),
                new GNode(13, 1, null, null, 126.7325259, 37.3410148),
                new GNode(14, 4, "운동장", new String[]{"운동장", "야외운동장"}, 126.732269, 37.3407499),
                new GNode(15, 1, null, null, 126.7324946, 37.3402106),
                new GNode(16, 1, null, null, 126.7333928, 37.3406941),
                new GNode(17, 1, null, null, 126.7335508, 37.340697),
                new GNode(18, 1, null, null, 126.7338409, 37.3410369),
                new GNode(19, 1, null, null, 126.7331917, 37.3404828),
                new GNode(20, 8, "A동  정문", new String[]{"A동", "공학관", "A"}, 126.733012, 37.3404005),
                new GNode(21, 8, "B동", new String[]{"B동", "공학관", "B"}, 126.7333303, 37.3403981),
                new GNode(22, 1, null, null, 126.7327469, 37.3400473),
                new GNode(23, 2, "비즈니스센터", new String[]{"비즈니스센터", "비지니스센터", "비센"}, 126.7326993, 37.3397297),
                new GNode(24, 1, null, null, 126.7337407, 37.3405675),
                new GNode(25, 1, null, null, 126.7334535, 37.3402799),
                new GNode(26, 1, null, null, 126.7341991, 37.340785),
                new GNode(27, 2, "후문", new String[]{"후문", "뒷문"}, 126.7342771, 37.3408865),
                new GNode(28, 8, "종합관", new String[]{"종합관", "종합교육관"}, 126.7340643, 37.3407165),
                new GNode(29, 1, null, null, 126.7339313, 37.3404741),
                new GNode(30, 1, null, null, 126.7336486, 37.3401883),
                new GNode(31, 1, null, null, 126.7331765, 37.3397643),
                new GNode(32, 2, "행정동", new String[]{"행정동", "행정"}, 126.7333881, 37.3397134),
                new GNode(33, 2, "정문", new String[]{"정문", "앞문"}, 126.7328697, 37.3394036),
                new GNode(34, 1, null, null, 126.7340958, 37.340394),
                new GNode(35, 1, null, null, 126.7339456, 37.3401767),
                new GNode(36, 1, null, null, 126.7337904, 37.3401126),
                new GNode(37, 8, "C동", new String[]{"C동", "공학관", "C"}, 126.7340423, 37.3401007),
                new GNode(38, 1, null, null, 126.7345681, 37.3405729),
                new GNode(39, 1, null, null, 126.7344959, 37.3404579),
                new GNode(40, 8, "G동 뒷문", new String[]{"G동", "공학관", "G"}, 126.7347369, 37.34029),
                new GNode(41, 1, null, null, 126.7342688, 37.3402554),
                new GNode(42, 1, null, null, 126.7342912, 37.3400528),
                new GNode(43, 1, null, null, 126.7341305, 37.3399751),
                new GNode(44, 8, "D동", new String[]{"D동", "공학관", "D"}, 126.7339943, 37.3398282),
                new GNode(45, 1, null, null, 126.7336674, 37.3394493),
                new GNode(46, 1, null, null, 126.7343518, 37.3401956),
                new GNode(47, 8, "G동 앞문", new String[]{"G동", "공학관", "G"}, 126.7346199, 37.3401387),
                new GNode(48, 1, null, null, 126.7347054, 37.3400582),
                new GNode(49, 1, null, null, 126.734515, 37.3399127),
                new GNode(50, 1, null, null, 126.7345649, 37.3397778),
                new GNode(51, 1, null, null, 126.7340227, 37.3392488),
                new GNode(52, 2, "산학융합본부 옆문", new String[]{"산융", "산학융합본부"}, 126.733845, 37.3389637),
                new GNode(53, 1, null, null, 126.7349993, 37.3394652),
                new GNode(54, 8, "E동 정문", new String[]{"E동", "공학관", "E"}, 126.7350439, 37.339524),
                new GNode(55, 1, null, null, 126.7342825, 37.3389035),
                new GNode(56, 1, null, null, 126.7352209, 37.3393179),
                new GNode(57, 8, "P동", new String[]{"P동", "공학관", "P"}, 126.7353305, 37.3394222),
                new GNode(58, 2, "산학융합본부 정문", new String[]{"산융", "산학융합본부"}, 126.7346572, 37.3386878)

        };
}
