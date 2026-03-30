package com.example.daggumaker;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StickerPreviewActivity extends AppCompatActivity {

    private ImageView[] ivStickers = new ImageView[5];
    private final String REPLICATE_API_KEY = "여기에_토큰을_넣으세요";

    private OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_preview);

        // [1] 이미지 뷰 연결
        ivStickers[0] = findViewById(R.id.iv_sticker_1);
        ivStickers[1] = findViewById(R.id.iv_sticker_2);
        ivStickers[2] = findViewById(R.id.iv_sticker_3);
        ivStickers[3] = findViewById(R.id.iv_sticker_4);
        ivStickers[4] = findViewById(R.id.iv_sticker_5);

        // [2] 스타일 변환 버튼들
        TextView btnVintage = findViewById(R.id.tv_style_vintage);
        TextView btnHanddrawn = findViewById(R.id.tv_style_cute);
        TextView btnPolaroid = findViewById(R.id.tv_style_polaroid);
        TextView btnPixel = findViewById(R.id.tv_style_pixel);

        if (btnVintage != null) btnVintage.setOnClickListener(v -> startStyleChange("vintage style"));
        if (btnHanddrawn != null) btnHanddrawn.setOnClickListener(v -> startStyleChange("watercolor style"));
        if (btnPolaroid != null) btnPolaroid.setOnClickListener(v -> startStyleChange("polaroid style"));
        if (btnPixel != null) btnPixel.setOnClickListener(v -> startStyleChange("pixel art style"));

        // [3] 상단 버튼들 (뒤로가기 & 메인)
        TextView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // ⭐ 여기에 메인 버튼 기능을 넣었습니다! ⭐
        TextView btnMain = findViewById(R.id.btn_main);
        if (btnMain != null) {
            btnMain.setOnClickListener(v -> {
                // MainActivity로 이동하는 명령
                Intent intent = new Intent(StickerPreviewActivity.this, MainActivity.class);
                // 기존에 쌓여있던 화면들을 싹 정리하고 메인으로 깔끔하게 이동합니다.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish(); // 현재 미리보기 화면은 닫기
            });
        }

        // [4] 하단 액션 버튼들 (배치 등)
        LinearLayout btnPlace = findViewById(R.id.btn_place);
        if (btnPlace != null) btnPlace.setOnClickListener(v -> startActivity(new Intent(this, PlacementActivity.class)));
    }

    // --- 아래 AI 관련 및 저장 메서드들은 민하님 기존 코드와 동일합니다 ---
    private void startStyleChange(String style) {
        Toast.makeText(this, "스타일 변환 중...", Toast.LENGTH_SHORT).show();
        for (int i = 0; i < ivStickers.length; i++) {
            final int index = i;
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                ImageView iv = ivStickers[index];
                if (iv != null && iv.getDrawable() instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                    sendToAi(bitmap, style, iv);
                }
            }, i * 600);
        }
    }

    private void sendToAi(Bitmap bitmap, String style, ImageView targetIv) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, baos);
        String base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);

        try {
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("version", "39ed52f6c203b60388d7529452b4146a47a1660d705c879d20c58e5f8f10731d");
            JSONObject inputJson = new JSONObject();
            inputJson.put("image", "data:image/png;base64," + base64);
            inputJson.put("prompt", "sticker of " + style);
            bodyJson.put("input", inputJson);

            Request request = new Request.Builder()
                    .url("https://api.replicate.com/v1/predictions")
                    .post(RequestBody.create(bodyJson.toString(), MediaType.parse("application/json")))
                    .addHeader("Authorization", "Token " + REPLICATE_API_KEY)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {}
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String id = new JSONObject(response.body().string()).getString("id");
                            pollResult(id, targetIv);
                        } catch (Exception e) {}
                    }
                }
            });
        } catch (Exception e) {}
    }

    private void pollResult(String id, ImageView targetIv) {
        Request request = new Request.Builder()
                .url("https://api.replicate.com/v1/predictions/" + id)
                .addHeader("Authorization", "Token " + REPLICATE_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject res = new JSONObject(response.body().string());
                    if (res.getString("status").equals("succeeded")) {
                        String imageUrl = res.getJSONArray("output").getString(0);
                        new Handler(Looper.getMainLooper()).post(() -> Glide.with(StickerPreviewActivity.this).load(imageUrl).into(targetIv));
                    } else if (!res.getString("status").equals("failed")) {
                        Thread.sleep(3000);
                        pollResult(id, targetIv);
                    }
                } catch (Exception e) {}
            }
            @Override
            public void onFailure(Call call, IOException e) {}
        });
    }

    private ArrayList<String> getStickerUriList() {
        ArrayList<String> uriList = new ArrayList<>();
        int[] ids = {R.drawable.s5, R.drawable.s6, R.drawable.s7, R.drawable.s8, R.drawable.s9};
        for (int i = 0; i < ids.length; i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), ids[i]);
            if (bitmap != null) {
                Uri uri = saveToGallery(bitmap, "Daku_" + i);
                if (uri != null) uriList.add(uri.toString());
            }
        }
        return uriList;
    }

    private Uri saveToGallery(Bitmap bitmap, String title) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, title + ".png");
            values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "DakuMaker");
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                OutputStream os = getContentResolver().openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                os.close();
            }
            return uri;
        } catch (Exception e) { return null; }
    }
}