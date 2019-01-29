package ru.androidtools.archivetest;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

public class MainActivity extends AppCompatActivity {
  private static final int REQUEST_PERMISSION_READ = 101;
  private ListView listView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    listView = findViewById(R.id.listview);
    Button btn_extract = findViewById(R.id.btn_extract);
    btn_extract.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
          extractArchive();
          fillList();
        } else {
          ActivityCompat.requestPermissions(MainActivity.this,
              new String[] { android.Manifest.permission.WRITE_EXTERNAL_STORAGE },
              REQUEST_PERMISSION_READ);
        }
      }
    });
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    switch (requestCode) {
      case REQUEST_PERMISSION_READ: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          extractArchive();
          fillList();
        }
        break;
      }
    }
  }

  private void extractArchive() {
    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        .getAbsolutePath();
    String filename = "test.7z";
    try {
      SevenZFile sevenZFile = new SevenZFile(new File(path, filename));
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
    File cache_dir = new File(getCacheDir() + "/test");
    for (File f : cache_dir.listFiles()) {
      files.add(f.getAbsolutePath());
    }
    ArrayAdapter<String> itemsAdapter =
        new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, files);
    listView.setAdapter(itemsAdapter);
    listView.setVisibility(View.VISIBLE);
  }
}
