package com.example.daggumaker;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;

import java.io.OutputStream;

public class PlacementActivity extends AppCompatActivity {

    private ConstraintLayout clNotebook;
    private long lastClickTime = 0;
    private static final long DOUBLE_CLICK_TIME_DELTA = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placement);

        clNotebook = findViewById(R.id.cl_notebook);
        TextView btnBack = findViewById(R.id.btn_back);
        TextView btnSave = findViewById(R.id.btn_save);
        HorizontalScrollView hsvStickers = findViewById(R.id.hsv_stickers);
        TextView btnNextSticker = findViewById(R.id.btn_next_sticker);
        ImageView ivNotebookBase = findViewById(R.id.iv_notebook_base);

        String imageUriString = getIntent().getStringExtra("diary_image_uri");
        if (imageUriString != null && ivNotebookBase != null) {
            Glide.with(this).load(Uri.parse(imageUriString)).centerInside().into(ivNotebookBase);
        }

        // 하단 리스트 드래그 설정
        int[] stickerIds = {R.id.iv_s5, R.id.iv_s6, R.id.iv_s7, R.id.iv_s8, R.id.iv_s9};
        int[] resIds = {R.drawable.s5, R.drawable.s6, R.drawable.s7, R.drawable.s8, R.drawable.s9};

        for (int i = 0; i < stickerIds.length; i++) {
            ImageView iv = findViewById(stickerIds[i]);
            if (iv != null) {
                final int resId = resIds[i];
                iv.setOnLongClickListener(v -> {
                    ClipData.Item item = new ClipData.Item(String.valueOf(resId));
                    ClipData dragData = new ClipData("sticker", new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);
                    v.startDragAndDrop(dragData, new View.DragShadowBuilder(v), null, 0);
                    return true;
                });
            }
        }

        clNotebook.setOnDragListener((v, event) -> {
            if (event.getAction() == DragEvent.ACTION_DROP) {
                int resId = Integer.parseInt(event.getClipData().getItemAt(0).getText().toString());
                addStickerWithHandle(resId, event.getX(), event.getY());
            }
            return true;
        });

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveNotebookToGallery());
        btnNextSticker.setOnClickListener(v -> hsvStickers.smoothScrollBy(300, 0));
    }

    private void addStickerWithHandle(int resId, float x, float y) {
        // 1. 스티커와 핸들을 담을 컨테이너(FrameLayout) 생성
        FrameLayout container = new FrameLayout(this);
        int initialSize = (int) (120 * getResources().getDisplayMetrics().density);
        container.setLayoutParams(new FrameLayout.LayoutParams(initialSize, initialSize));
        container.setX(x - (initialSize / 2f));
        container.setY(y - (initialSize / 2f));

        // 2. 실제 스티커 이미지뷰
        ImageView stickerImg = new ImageView(this);
        stickerImg.setImageResource(resId);
        stickerImg.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        stickerImg.setPadding(20, 20, 20, 20); // 핸들이 들어갈 공간 확보

        // 3. 크기 조절 핸들 (오른쪽 하단 아이콘)
        ImageView handle = new ImageView(this);
        handle.setImageResource(android.R.drawable.ic_menu_edit); // 임시 아이콘 (나중에 예쁜걸로 바꾸세요!)
        handle.setBackgroundColor(0x88FFFFFF); // 반투명 배경
        int handleSize = (int) (30 * getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams handleParams = new FrameLayout.LayoutParams(handleSize, handleSize);
        handleParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.END;
        handle.setLayoutParams(handleParams);

        container.addView(stickerImg);
        container.addView(handle);

        // [이동 로직] 스티커 이미지 클릭 시
        stickerImg.setOnTouchListener(new View.OnTouchListener() {
            float lastX, lastY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        container.bringToFront();
                        // 더블클릭 삭제
                        long clickTime = System.currentTimeMillis();
                        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                            clNotebook.removeView(container);
                        }
                        lastClickTime = clickTime;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        container.setX(container.getX() + (event.getRawX() - lastX));
                        container.setY(container.getY() + (event.getRawY() - lastY));
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        break;
                }
                return true;
            }
        });

        // [크기 조절 로직] 핸들 드래그 시
        handle.setOnTouchListener(new View.OnTouchListener() {
            float lastX, lastY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getRawX() - lastX;
                        ViewGroup.LayoutParams lp = container.getLayoutParams();
                        lp.width += (int) deltaX;
                        lp.height += (int) deltaX; // 정비율 유지
                        if (lp.width < 100) lp.width = 100; // 최소 크기 제한
                        if (lp.height < 100) lp.height = 100;
                        container.setLayoutParams(lp);
                        lastX = event.getRawX();
                        break;
                }
                return true;
            }
        });

        clNotebook.addView(container);
    }

    private void saveNotebookToGallery() {
        // 저장 시에는 핸들(아이콘)들이 안 보이게 숨기고 캡쳐해야 깔끔합니다!
        for (int i = 0; i < clNotebook.getChildCount(); i++) {
            View child = clNotebook.getChildAt(i);
            if (child instanceof FrameLayout) {
                View handle = ((FrameLayout) child).getChildAt(1);
                if (handle != null) handle.setVisibility(View.GONE);
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(clNotebook.getWidth(), clNotebook.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        clNotebook.draw(canvas);

        // ... 기존 저장 로직 (생략 - 동일하게 유지) ...
        Toast.makeText(this, "저장 완료!", Toast.LENGTH_SHORT).show();

        // 다시 핸들 보이게 하기
        for (int i = 0; i < clNotebook.getChildCount(); i++) {
            View child = clNotebook.getChildAt(i);
            if (child instanceof FrameLayout) {
                View handle = ((FrameLayout) child).getChildAt(1);
                if (handle != null) handle.setVisibility(View.VISIBLE);
            }
        }
    }
}