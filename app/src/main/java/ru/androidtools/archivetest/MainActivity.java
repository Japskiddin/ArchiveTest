package ru.androidtools.archivetest;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

public class MainActivity extends AppCompatActivity {
  private ListView listView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    listView = findViewById(R.id.listview);
    Button btn_extract = findViewById(R.id.btn_extract);
    btn_extract.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        extractArchive();
        fillList();
      }
    });
  }

  public static File getAssetFile(Context context, String asset_name, String name)
      throws IOException {
    File cacheFile = new File(context.getCacheDir(), name);
    try {
      InputStream inputStream = context.getAssets().open(asset_name);
      try {
        FileOutputStream outputStream = new FileOutputStream(cacheFile);
        try {
          byte[] buf = new byte[1024];
          int len;
          while ((len = inputStream.read(buf)) > 0) {
            outputStream.write(buf, 0, len);
          }
        } finally {
          outputStream.close();
        }
      } finally {
        inputStream.close();
      }
    } catch (IOException e) {
      throw new IOException("Could not open file" + asset_name, e);
    }
    return cacheFile;
  }

  private void extractArchive() {
    String filename = "kittens.7z";
    try {
      SevenZFile sevenZFile = new SevenZFile(getAssetFile(this, "kittens.7z", "tmp"));
      SevenZArchiveEntry entry = sevenZFile.getNextEntry();
      File cache_dir = new File(getCacheDir() + "/" + filename.substring(0, filename.indexOf(".")));
      if (!cache_dir.exists()) cache_dir.mkdirs();
      while (entry != null) {
        FileOutputStream out = new FileOutputStream(cache_dir + "/" + entry.getName());
        byte[] content = new byte[(int) entry.getSize()];
        sevenZFile.read(content, 0, content.length);
        out.write(content);
        out.close();
        entry = sevenZFile.getNextEntry();
      }
      sevenZFile.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void fillList() {
    List<String> files = new ArrayList<>();
    File cache_dir = new File(getCacheDir() + "/kittens");
    for (File f : cache_dir.listFiles()) {
      files.add(f.getAbsolutePath());
    }
    ArrayAdapter<String> itemsAdapter =
        new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, files);
    listView.setAdapter(itemsAdapter);
    listView.setVisibility(View.VISIBLE);
  }
}