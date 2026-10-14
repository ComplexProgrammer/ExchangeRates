package exchangerates.complexprogrammer.uz.services;

import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.os.Build;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.caverock.androidsvg.SVG;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import exchangerates.complexprogrammer.uz.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.ResponseBody;

/**
 * Loads SVG flags. Parsed flags stay in memory and the files are kept on disk,
 * so scrolling never re-downloads them and they show offline too.
 */
public class FlagLoader {
    private static final LruCache<String, Picture> memory = new LruCache<>(80);
    private static final ExecutorService diskExecutor = Executors.newSingleThreadExecutor();

    public static void load(ImageView target, String ccy) {
        String url = String.format(ApiClient.FLAG_URL, ccy);
        target.setTag(R.id.flag_url, url);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // PictureDrawable needs a software layer before Android 6.
            target.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        Picture cached = memory.get(url);
        if (cached != null) {
            target.setImageDrawable(new PictureDrawable(cached));
            return;
        }
        target.setImageResource(R.drawable.ic_flag_placeholder);

        File file = new File(new File(target.getContext().getFilesDir(), "flags"), ccy + ".svg");
        diskExecutor.execute(() -> {
            if (file.exists()) {
                try (InputStream in = new FileInputStream(file)) {
                    show(target, url, SVG.getFromInputStream(in).renderToPicture());
                    return;
                } catch (Exception e) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
            download(target, url, file);
        });
    }

    private static void download(ImageView target, String url, File file) {
        Request request = new Request.Builder().url(url).build();
        ApiClient.getHttpClient(target.getContext()).newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Placeholder stays.
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) {
                try (ResponseBody body = response.body()) {
                    if (!response.isSuccessful() || body == null) return;
                    byte[] bytes = body.bytes();
                    Picture picture = SVG.getFromString(new String(bytes, "UTF-8")).renderToPicture();
                    //noinspection ResultOfMethodCallIgnored
                    file.getParentFile().mkdirs();
                    try (FileOutputStream out = new FileOutputStream(file)) {
                        out.write(bytes);
                    }
                    show(target, url, picture);
                } catch (Exception ignored) {
                }
            }
        });
    }

    private static void show(ImageView target, String url, Picture picture) {
        memory.put(url, picture);
        target.post(() -> {
            // The row may have been recycled for another currency meanwhile.
            if (url.equals(target.getTag(R.id.flag_url))) {
                target.setImageDrawable(new PictureDrawable(picture));
            }
        });
    }
}
