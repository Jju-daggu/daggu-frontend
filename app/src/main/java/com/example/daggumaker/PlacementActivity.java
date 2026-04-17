package com.example.daggumaker;

import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView; // ✨ CardView 임포트 추가

import com.bumptech.glide.Glide;

public class PlacementActivity extends AppCompatActivity {

    // ✨ ConstraintLayout -> CardView로 타입 변경
    private CardView cvNotebookContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placement);

        // ✨ 뷰 연결
        cvNotebookContainer = findViewById(R.id.cv_notebook_container);
        TextView btnBack = findViewById(R.id.btn_back);
        TextView btnSave = findViewById(R.id.btn_save);
        HorizontalScrollView hsvStickers = findViewById(R.id.hsv_stickers);
        TextView btnNextSticker = findViewById(R.id.btn_next_sticker);
        ImageView ivNotebookBase = findViewById(R.id.iv_notebook_base);

        // 배경 클릭 시 모든 핸들 숨김
        cvNotebookContainer.setOnClickListener(v -> hideAllHandles());

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

        // ✨ 드롭 리스너 연결
        cvNotebookContainer.setOnDragListener((v, event) -> {
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

        ImageView stickerImg = new ImageView(this);
        stickerImg.setImageResource(resId);
        stickerImg.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        stickerImg.setPadding(35, 35, 35, 35);

        ImageView btnDelete = new ImageView(this);
        btnDelete.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        btnDelete.setBackgroundColor(0xAAFF4444);
        int handleSize = (int) (30 * getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams deleteParams = new FrameLayout.LayoutParams(handleSize, handleSize);
        deleteParams.gravity = android.view.Gravity.TOP | android.view.Gravity.START;
        btnDelete.setLayoutParams(deleteParams);

        ImageView btnScale = new ImageView(this);
        btnScale.setImageResource(android.R.drawable.ic_menu_edit);
        btnScale.setBackgroundColor(0x88FFFFFF);
        FrameLayout.LayoutParams scaleParams = new FrameLayout.LayoutParams(handleSize, handleSize);
        scaleParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.END;
        btnScale.setLayoutParams(scaleParams);

        btnDelete.setVisibility(View.GONE);
        btnScale.setVisibility(View.GONE);

        container.addView(stickerImg);
        container.addView(btnDelete);
        container.addView(btnScale);

        // ✨ 삭제 버튼 로직
        btnDelete.setOnClickListener(v -> cvNotebookContainer.removeView(container));

        stickerImg.setOnTouchListener(new View.OnTouchListener() {
            float lastX, lastY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        hideAllHandles();
                        btnDelete.setVisibility(View.VISIBLE);
                        btnScale.setVisibility(View.VISIBLE);

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

        // ✨ 생성된 스티커 컨테이너를 CardView에 추가
        cvNotebookContainer.addView(container);
    }

    private void hideAllHandles() {
        for (int i = 0; i < cvNotebookContainer.getChildCount(); i++) {
            View child = cvNotebookContainer.getChildAt(i);
            if (child instanceof FrameLayout) {
                FrameLayout container = (FrameLayout) child;
                container.getChildAt(1).setVisibility(View.GONE);
                container.getChildAt(2).setVisibility(View.GONE);
            }
        }
    }

    private void saveNotebookToGallery() {
        hideAllHandles();
        Bitmap bitmap = Bitmap.createBitmap(cvNotebookContainer.getWidth(), cvNotebookContainer.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        cvNotebookContainer.draw(canvas);

        Toast.makeText(this, "갤러리에 저장되었습니다!", Toast.LENGTH_SHORT).show();
    }
}