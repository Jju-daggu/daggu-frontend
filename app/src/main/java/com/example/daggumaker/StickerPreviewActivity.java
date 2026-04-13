package com.example.daggumaker;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

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
    private final String REPLICATE_API_KEY = BuildConfig.REPLICATE_API_KEY;

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

        // [3] 상단 버튼들
        TextView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        TextView btnMain = findViewById(R.id.btn_main);
        if (btnMain != null) {
            btnMain.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        // [4] 하단 액션 버튼들
        // 🌟 [수정] 배치 화면 이동 시 이미지 경로 전달
        LinearLayout btnPlace = findViewById(R.id.btn_place);
        if (btnPlace != null) {
            btnPlace.setOnClickListener(v -> {
                Intent intent = new Intent(StickerPreviewActivity.this, PlacementActivity.class);
                intent.putExtra("diary_image_uri", getIntent().getStringExtra("diary_image_uri"));
                startActivity(intent);
            });
        }

        // ⭐ 보관 버튼
        LinearLayout btnStore = findViewById(R.id.btn_store);
        if (btnStore != null) {
            btnStore.setOnClickListener(v -> {
                ArrayList<String> stickerUris = getStickerUriList();
                if (!stickerUris.isEmpty()) {
                    Intent intent = new Intent(StickerPreviewActivity.this, VaultActivity.class);
                    intent.putStringArrayListExtra("sticker_uri_list", stickerUris);
                    startActivity(intent);
                    Toast.makeText(this, "갤러리와 보관함에 저장되었습니다!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "저장할 스티커가 아직 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 🌟 [ID 수정] btnPrint가 아니라 btn_print로 수정
        LinearLayout btnPrint = findViewById(R.id.btn_print);
        if (btnPrint != null) {
            btnPrint.setOnClickListener(v -> {
                ArrayList<String> stickerUris = getStickerUriList();
                Intent intent = new Intent(this, PrintActivity.class);
                intent.putStringArrayListExtra("sticker_uri_list", stickerUris);
                startActivity(intent);
            });
        }
    }

    private void startStyleChange(String style) {
        if (REPLICATE_API_KEY == null || REPLICATE_API_KEY.isEmpty() || REPLICATE_API_KEY.equals("\"\"")) {
            Toast.makeText(this, "AI 스타일 변환 기능이 비활성화되어 있습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

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
                @Override public void onFailure(Call call, IOException e) {}
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
            @Override public void onFailure(Call call, IOException e) {}
        });
    }

    private ArrayList<String> getStickerUriList() {
        ArrayList<String> uriList = new ArrayList<>();
        for (int i = 0; i < ivStickers.length; i++) {
            if (ivStickers[i].getDrawable() instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) ivStickers[i].getDrawable()).getBitmap();
                Uri uri = saveToGallery(bitmap, "DakuSticker_" + System.currentTimeMillis() + "_" + i);
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

                Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                scanIntent.setData(uri);
                sendBroadcast(scanIntent);
            }
            return uri;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}