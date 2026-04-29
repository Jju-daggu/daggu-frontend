package com.example.daggumaker;

import android.graphics.Bitmap;
import android.graphics.Color;
import androidx.annotation.NonNull;

// 🌟 초심플 컬러 기반 배경 제거 (흰색 배경에 최적화)
public class StickerRemover {

    public StickerRemover() {
        // 복잡한 초기화 과정이 필요 없습니다!
    }

    // 🌟 원본 비트맵에서 흰색 계열 배경을 날려버리는 함수
    public void removeBackground(@NonNull Bitmap originalBitmap, @NonNull final OnBackgroundRemovedListener listener) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        Bitmap transparentBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // 🌟 '흰색'으로 판단할 기준값 (255가 완전 흰색, 이보다 크면 흰색으로 판단)
        int whiteThreshold = 245;

        // 픽셀 하나하나 돌면서 컬러 검사
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelColor = originalBitmap.getPixel(x, y);
                int r = Color.red(pixelColor);
                int g = Color.green(pixelColor);
                int b = Color.blue(pixelColor);

                // 🌟 R, G, B 값이 모두 245보다 크면(흰색에 가까우면) 투명 처리!
                if (r > whiteThreshold && g > whiteThreshold && b > whiteThreshold) {
                    transparentBitmap.setPixel(x, y, Color.TRANSPARENT);
                } else {
                    // 흰색이 아니면(강아지 그림이면) 원래 색 유지
                    transparentBitmap.setPixel(x, y, pixelColor);
                }
            }
        }
        // 결과 전달
        listener.onBackgroundRemoved(transparentBitmap);
    }

    public interface OnBackgroundRemovedListener {
        void onBackgroundRemoved(Bitmap transparentBitmap);
    }
}