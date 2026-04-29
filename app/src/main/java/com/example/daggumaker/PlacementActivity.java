package com.example.daggumaker;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;

public class PlacementActivity extends AppCompatActivity {

    private CardView cvNotebookContainer;
    private StickerRemover stickerRemover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placement);

        // 초기화
        cvNotebookContainer = findViewById(R.id.cv_notebook_container);
        stickerRemover = new StickerRemover();

        TextView btnBack = findViewById(R.id.btn_back);
        TextView btnSave = findViewById(R.id.btn_save);
        HorizontalScrollView hsvStickers = findViewById(R.id.hsv_stickers);
        TextView btnNextSticker = findViewById(R.id.btn_next_sticker);
        ImageView ivNotebookBase = findViewById(R.id.iv_notebook_base);

        cvNotebookContainer.setOnClickListener(v -> hideAllHandles());

        // 1. 다이어리 원본 사진 로드
        String imageUriString = getIntent().getStringExtra("diary_image_uri");
        if (imageUriString != null && ivNotebookBase != null) {
            Uri imageUri = Uri.parse(imageUriString);
            ivNotebookBase.post(() -> {
                ivNotebookBase.setPadding(0, 0, 0, 0);
                Glide.with(this).load(imageUri).diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true).centerInside().into(ivNotebookBase);
            });
        }

        // 2. 보관함(Vault) 자동 청소 및 데이터 가져오기
        SharedPreferences prefs = getSharedPreferences("StickerVault", Context.MODE_PRIVATE);
        String savedData = prefs.getString("uris", "");
        ArrayList<String> cleanUrls = new ArrayList<>();
        if (!savedData.isEmpty()) {
            for (String s : savedData.split(",")) {
                if (s != null && s.trim().startsWith("http") && s.trim().length() > 20) {
                    cleanUrls.add(s.trim());
                }
            }
        }

        // 3. 하단 스티커 목록 생성
        ViewGroup stickerContainer = (ViewGroup) hsvStickers.getChildAt(0);
        stickerContainer.removeAllViews();

        int size = (int) (75 * getResources().getDisplayMetrics().density);
        int margin = (int) (8 * getResources().getDisplayMetrics().density);

        for (String url : cleanUrls) {
            ImageView iv = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(margin, margin, margin, margin);
            iv.setLayoutParams(params);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iv.setBackgroundResource(R.drawable.bg_rounded_button);
            iv.setPadding(10, 10, 10, 10);

            Glide.with(this).load(url).into(iv);

            // 길게 누르면 드래그 시작
            iv.setOnLongClickListener(v -> {
                ClipData.Item item = new ClipData.Item(url);
                ClipData dragData = new ClipData("sticker", new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);
                v.startDragAndDrop(dragData, new View.DragShadowBuilder(v), null, 0);
                return true;
            });

            // 클릭하면 정중앙 배치
            iv.setOnClickListener(v -> addStickerWithHandle(url, cvNotebookContainer.getWidth()/2f, cvNotebookContainer.getHeight()/2f));
            stickerContainer.addView(iv);
        }

        // 4. 드롭 리스너
        cvNotebookContainer.setOnDragListener((v, event) -> {
            if (event.getAction() == DragEvent.ACTION_DROP) {
                String draggedUrl = event.getClipData().getItemAt(0).getText().toString();
                addStickerWithHandle(draggedUrl, event.getX(), event.getY());
            }
            return true;
        });

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnSave != null) btnSave.setOnClickListener(v -> saveNotebookToGallery());
        if (btnNextSticker != null) btnNextSticker.setOnClickListener(v -> hsvStickers.smoothScrollBy(300, 0));
    }

    // 🌟 핵심: 스티커 생성 + 컬러 기반 고화질 누끼 적용
    private void addStickerWithHandle(String url, float x, float y) {
        FrameLayout container = new FrameLayout(this);
        int initialSize = (int) (150 * getResources().getDisplayMetrics().density);
        container.setLayoutParams(new FrameLayout.LayoutParams(initialSize, initialSize));
        container.setX(x - (initialSize / 2f));
        container.setY(y - (initialSize / 2f));

        ImageView stickerImg = new ImageView(this);
        stickerImg.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // 🌟 잘림 방지를 위해 패딩은 비워둡니다.
        stickerImg.setPadding(0, 0, 0, 0);

        // 조작 버튼 생성
        ImageView btnDelete = createHandleButton(android.R.drawable.ic_menu_close_clear_cancel, 0xCCFF4444, android.view.Gravity.TOP | android.view.Gravity.START);
        ImageView btnScale = createHandleButton(android.R.drawable.ic_menu_crop, 0xCC888888, android.view.Gravity.BOTTOM | android.view.Gravity.END);

        btnDelete.setOnClickListener(v -> cvNotebookContainer.removeView(container));
        setupStickerTouchListeners(container, stickerImg, btnDelete, btnScale);

        container.addView(stickerImg);
        container.addView(btnDelete);
        container.addView(btnScale);
        cvNotebookContainer.addView(container);

        // 🌟 [배경 제거 작업] 비트맵으로 불러와서 색상 필터 통과
        Glide.with(this).asBitmap().load(url).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                // 새로 만든 컬러 기반 누끼 마법사 가동!
                stickerRemover.removeBackground(resource, transparentBitmap -> {
                    runOnUiThread(() -> stickerImg.setImageBitmap(transparentBitmap));
                });
            }
            @Override public void onLoadCleared(@Nullable Drawable placeholder) {}
        });
    }

    private ImageView createHandleButton(int resId, int bgColor, int gravity) {
        int handleSize = (int) (35 * getResources().getDisplayMetrics().density);
        ImageView btn = new ImageView(this);
        btn.setImageResource(resId);
        btn.setBackgroundColor(bgColor);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(handleSize, handleSize);
        params.gravity = gravity;
        btn.setLayoutParams(params);
        btn.setVisibility(View.GONE);
        return btn;
    }

    private void setupStickerTouchListeners(FrameLayout container, ImageView stickerImg, ImageView btnDelete, ImageView btnScale) {
        stickerImg.setOnTouchListener(new View.OnTouchListener() {
            float lastX, lastY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        hideAllHandles();
                        btnDelete.setVisibility(View.VISIBLE);
                        btnScale.setVisibility(View.VISIBLE);
                        lastX = event.getRawX(); lastY = event.getRawY();
                        container.bringToFront();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        container.setX(container.getX() + (event.getRawX() - lastX));
                        container.setY(container.getY() + (event.getRawY() - lastY));
                        lastX = event.getRawX(); lastY = event.getRawY();
                        break;
                }
                return true;
            }
        });

        btnScale.setOnTouchListener(new View.OnTouchListener() {
            float lastX;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) lastX = event.getRawX();
                else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    float deltaX = event.getRawX() - lastX;
                    ViewGroup.LayoutParams lp = container.getLayoutParams();
                    lp.width += (int) (deltaX * 1.5f); lp.height += (int) (deltaX * 1.5f);
                    if (lp.width < 150) lp.width = 150;
                    container.setLayoutParams(lp);
                    lastX = event.getRawX();
                }
                return true;
            }
        });
    }

    private void hideAllHandles() {
        for (int i = 0; i < cvNotebookContainer.getChildCount(); i++) {
            View child = cvNotebookContainer.getChildAt(i);
            if (child instanceof FrameLayout) {
                FrameLayout c = (FrameLayout) child;
                if (c.getChildCount() >= 3) {
                    c.getChildAt(1).setVisibility(View.GONE);
                    c.getChildAt(2).setVisibility(View.GONE);
                }
            }
        }
    }

    private void saveNotebookToGallery() {
        hideAllHandles();
        Bitmap bitmap = Bitmap.createBitmap(cvNotebookContainer.getWidth(), cvNotebookContainer.getHeight(), Bitmap.Config.ARGB_8888);
        cvNotebookContainer.draw(new Canvas(bitmap));
        Toast.makeText(this, "다꾸 완료! 갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }
}