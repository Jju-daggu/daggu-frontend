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
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.OutputStream;

public class PlacementActivity extends AppCompatActivity {

    private ConstraintLayout clNotebook;

    // 🌟 더블 클릭 판정을 위한 시간 변수
    private long lastClickTime = 0;
    private static final long DOUBLE_CLICK_TIME_DELTA = 300; // 0.3초 이내 클릭 시 더블 클릭

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placement);

        clNotebook = findViewById(R.id.cl_notebook);
        TextView btnBack = findViewById(R.id.btn_back);
        TextView btnSave = findViewById(R.id.btn_save);
        HorizontalScrollView hsvStickers = findViewById(R.id.hsv_stickers);
        TextView btnNextSticker = findViewById(R.id.btn_next_sticker);

        // 1. 하단 트레이 스티커 리스트 설정 (롱클릭 시 드래그 시작)
        int[] stickerIds = {R.id.iv_s5, R.id.iv_s6, R.id.iv_s7, R.id.iv_s8, R.id.iv_s9};
        int[] resIds = {R.drawable.s5, R.drawable.s6, R.drawable.s7, R.drawable.s8, R.drawable.s9};

        for (int i = 0; i < stickerIds.length; i++) {
            ImageView iv = findViewById(stickerIds[i]);
            if (iv != null) {
                final int resId = resIds[i];
                iv.setOnLongClickListener(v -> {
                    ClipData.Item item = new ClipData.Item(String.valueOf(resId));
                    ClipData dragData = new ClipData("sticker", new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);
                    View.DragShadowBuilder myShadow = new View.DragShadowBuilder(v);
                    v.startDragAndDrop(dragData, myShadow, null, 0);
                    return true;
                });
            }
        }

        // 2. 다이어리 영역 드롭 리스너 (스티커 추가)
        clNotebook.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
                case DragEvent.ACTION_DROP:
                    ClipData.Item item = event.getClipData().getItemAt(0);
                    int resId = Integer.parseInt(item.getText().toString());
                    addStickerAtLocation(resId, event.getX(), event.getY());
                    return true;
            }
            return true;
        });

        // 3. 버튼 클릭 이벤트
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveNotebookToGallery());
        btnNextSticker.setOnClickListener(v -> hsvStickers.smoothScrollBy(300, 0));
    }

    // 스티커를 다이어리에 동적으로 추가
    private void addStickerAtLocation(int resId, float x, float y) {
        ImageView newSticker = new ImageView(this);
        newSticker.setImageResource(resId);
        int size = (int) (120 * getResources().getDisplayMetrics().density);
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(size, size);
        newSticker.setLayoutParams(lp);

        newSticker.setX(x - (size / 2f));
        newSticker.setY(y - (size / 2f));

        // 이동 및 더블 클릭 삭제 리스너 연결
        setupMovingTouchListener(newSticker);
        clNotebook.addView(newSticker);
    }

    // 🌟 스티커 이동 및 더블 클릭 삭제 로직
    @SuppressLint("ClickableViewAccessibility")
    private void setupMovingTouchListener(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private float lastX, lastY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        v.bringToFront();

                        // 더블 클릭 체크
                        long clickTime = System.currentTimeMillis();
                        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                            showDeleteConfirmDialog(v); // 확인 창 띄우기
                        }
                        lastClickTime = clickTime;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        v.setX(v.getX() + (event.getRawX() - lastX));
                        v.setY(v.getY() + (event.getRawY() - lastY));
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        return true;
                }
                return false;
            }
        });
    }

    // 🌟 삭제 확인 다이얼로그
    private void showDeleteConfirmDialog(View stickerView) {
        new AlertDialog.Builder(this)
                .setTitle("스티커 제거")
                .setMessage("이 스티커를 제거하시겠습니까?")
                .setPositiveButton("네", (dialog, which) -> {
                    ((ViewGroup) stickerView.getParent()).removeView(stickerView);
                    Toast.makeText(this, "스티커가 제거되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("아니오", null)
                .show();
    }

    // 갤러리 저장 기능
    private void saveNotebookToGallery() {
        Bitmap bitmap = Bitmap.createBitmap(clNotebook.getWidth(), clNotebook.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        clNotebook.draw(canvas);

        String fileName = "Dagu_" + System.currentTimeMillis() + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DaguMaker");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        }

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (uri != null) {
            try {
                OutputStream out = getContentResolver().openOutputStream(uri);
                if (out != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.close();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.clear();
                        values.put(MediaStore.Images.Media.IS_PENDING, 0);
                        getContentResolver().update(uri, values, null, null);
                    }
                    Toast.makeText(this, "갤러리에 다이어리가 저장되었습니다!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}