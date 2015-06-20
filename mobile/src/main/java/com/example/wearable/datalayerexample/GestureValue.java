package com.example.wearable.datalayerexample;

/**
 * Created by imac_06 on 15. 6. 20..
 */
public interface GestureValue {
    static final int SWIPE_MIN_DISTANCE = 120;          // 제스처 최소 거리
    static final int SWIPE_MAX_OFF_PATH = 2000;         // 제스처 최대 거리
    static final int SWIPE_THRESHOLD_VELOCITY = 50;    // 제스처 인식 속도
    static final int TAP_TERM = 500;                   // 탭 간격
}
