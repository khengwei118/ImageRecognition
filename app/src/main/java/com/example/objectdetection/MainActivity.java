package com.example.objectdetection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1000;
    private static final int CAMERA_REQUEST_CODE = 10001;
    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 1002;
    private static final int IMAGE_REQUEST_CODE = 1003;

    private ImageView imageView;
    private ListView listView;
    private ImageClassifier imageClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUIElements();
    }

    private void initializeUIElements() {
        imageView = findViewById(R.id.iv_capture);
        listView = findViewById(R.id.lv_probabilities);
        Button takepicture = findViewById(R.id.bt_take_pic);
        Button openGallery = findViewById(R.id.bt_open_gal);

        try {
            imageClassifier = new ImageClassifier(this);
        } catch (IOException e) {
            Log.e("Image Classifier Error", "Error" + e);
        }

        takepicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasPermission("camera")) {
                    openCamera();
                } else {
                    requestPermission("camera");
                }
            }
        });

        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasPermission("readStorage")) {
                    pickImage();
                } else {
                    requestPermission("readStorage");
                }
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // if this is the result of our camera image request
        if (requestCode == CAMERA_REQUEST_CODE) {
            // getting bitmap of the image
            Bitmap photo = (Bitmap) Objects.requireNonNull(Objects.requireNonNull(data).getExtras()).get("data");
            // displaying this bitmap in imageview
            imageView.setImageBitmap(photo);
            // pass this bitmap to classifier to make prediction
            List<ImageClassifier.Recognition> predicitons = imageClassifier.recognizeImage(
                    photo, 0);
            // creating a list of string to display in list view
            final List<String> predictionsList = new ArrayList<>();
            // displaying predictions
            for (ImageClassifier.Recognition rec : predicitons) {
                String objName = removeDigits(rec.getName());
                objName = removeOtherWords(objName);
                double objConf = (Math.round((((double) rec.getConfidence()) * 10000.0))) / 100.0;
                predictionsList.add(objName + ", " + objConf + "% confident");
            }
            // creating an array adapter to display the classification result in list view
            ArrayAdapter<String> predictionsAdapter = new ArrayAdapter<>(
                    this, R.layout.support_simple_spinner_dropdown_item, predictionsList);
            listView.setAdapter(predictionsAdapter);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // helper method to remove digits from label name
    private String removeDigits(String name) {
        String result = "";
        for (int i = 0; i < name.length(); i++) {
            if (!Character.isDigit(name.charAt(i))) {
                result += name.charAt(i);
            }
        }
        return result;
    }

    // helper method to remove words after first comma
    private String removeOtherWords(String name) {
        int commaIndex = name.length() - 1;
        for (int i = name.length() - 1; i >= 0; i--) {
            if (name.charAt(i) == ',') {
                commaIndex = i - 1;
            }
        }
        return name.substring(0, commaIndex + 1);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // if this is the result of our camera permission request
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (hasAllPermissions(grantResults)) {
                openCamera();
            } else {
                requestPermission("camera");
            }
        }
        if (requestCode == READ_STORAGE_PERMISSION_REQUEST_CODE) {
            if (hasAllPermissions(grantResults)) {
                pickImage();
            } else {
                requestPermission("readStorage");
            }
        }
    }



    private boolean hasAllPermissions(int[] grantResults) {
        for (int result: grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermission(String permType) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permType.equalsIgnoreCase("camera")) {
            if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Toast.makeText(this, "Camera Permission Required", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permType.equalsIgnoreCase("readStorage")) {
            if(shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Storage Permission Required", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_REQUEST_CODE);
        }

    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    private boolean hasPermission(String permType) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permType.equalsIgnoreCase("camera")) {
            return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permType.equalsIgnoreCase("readStorage")) {
            return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }
}