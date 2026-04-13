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

        // 배경 클릭 시 모든 핸들 숨김
        clNotebook.setOnClickListener(v -> hideAllHandles());

        String imageUriString = getIntent().getStringExtra("diary_image_uri");
        if (imageUriString != null && ivNotebookBase != null) {
            Glide.with(this).load(Uri.parse(imageUriString)).centerInside().into(ivNotebookBase);
        }

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
        FrameLayout container = new FrameLayout(this);
        int initialSize = (int) (120 * getResources().getDisplayMetrics().density);
        container.setLayoutParams(new FrameLayout.LayoutParams(initialSize, initialSize));
        container.setX(x - (initialSize / 2f));
        container.setY(y - (initialSize / 2f));

        // 1. 스티커 이미지
        ImageView stickerImg = new ImageView(this);
        stickerImg.setImageResource(resId);
        stickerImg.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        stickerImg.setPadding(35, 35, 35, 35); // 핸들이 겹치지 않게 패딩 넉넉히

        // 2. 삭제 버튼 (왼쪽 상단)
        ImageView btnDelete = new ImageView(this);
        btnDelete.setImageResource(android.R.drawable.ic_menu_close_clear_cancel); // X 아이콘
        btnDelete.setBackgroundColor(0xAAFF4444); // 빨간색 반투명 배경
        int handleSize = (int) (30 * getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams deleteParams = new FrameLayout.LayoutParams(handleSize, handleSize);
        deleteParams.gravity = android.view.Gravity.TOP | android.view.Gravity.START;
        btnDelete.setLayoutParams(deleteParams);

        // 3. 크기 조절 핸들 (오른쪽 하단)
        ImageView btnScale = new ImageView(this);
        btnScale.setImageResource(android.R.drawable.ic_menu_edit);
        btnScale.setBackgroundColor(0x88FFFFFF);
        FrameLayout.LayoutParams scaleParams = new FrameLayout.LayoutParams(handleSize, handleSize);
        scaleParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.END;
        btnScale.setLayoutParams(scaleParams);

        // 🌟 초기에는 두 버튼 모두 숨김
        btnDelete.setVisibility(View.GONE);
        btnScale.setVisibility(View.GONE);

        container.addView(stickerImg);
        container.addView(btnDelete);
        container.addView(btnScale);

        // [삭제 로직]
        btnDelete.setOnClickListener(v -> clNotebook.removeView(container));

        // [이동 및 핸들 표시 로직]
        stickerImg.setOnTouchListener(new View.OnTouchListener() {
            float lastX, lastY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        hideAllHandles();
                        btnDelete.setVisibility(View.VISIBLE); // 🌟 삭제 버튼 표시
                        btnScale.setVisibility(View.VISIBLE);  // 🌟 조절 핸들 표시

                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        container.bringToFront();
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

        // [크기 조절 로직]
        btnScale.setOnTouchListener(new View.OnTouchListener() {
            float lastX;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getRawX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getRawX() - lastX;
                        ViewGroup.LayoutParams lp = container.getLayoutParams();
                        lp.width += (int) deltaX;
                        lp.height += (int) deltaX;
                        if (lp.width < 150) lp.width = 150;
                        container.setLayoutParams(lp);
                        lastX = event.getRawX();
                        break;
                }
                return true;
            }
        });

        clNotebook.addView(container);
    }

    private void hideAllHandles() {
        for (int i = 0; i < clNotebook.getChildCount(); i++) {
            View child = clNotebook.getChildAt(i);
            if (child instanceof FrameLayout) {
                FrameLayout container = (FrameLayout) child;
                // 자식 1번(삭제), 2번(조절) 모두 숨김
                container.getChildAt(1).setVisibility(View.GONE);
                container.getChildAt(2).setVisibility(View.GONE);
            }
        }
    }

    private void saveNotebookToGallery() {
        hideAllHandles();
        // ... (이후 저장 로직은 기존과 동일) ...
        Bitmap bitmap = Bitmap.createBitmap(clNotebook.getWidth(), clNotebook.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        clNotebook.draw(canvas);

        // 저장 로직 생략 (기본 코드 유지)
        Toast.makeText(this, "갤러리에 저장되었습니다!", Toast.LENGTH_SHORT).show();
    }
}