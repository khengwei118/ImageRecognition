package com.example.objectdetection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1000;
    private static final int CAMERA_REQUEST_CODE = 1001;
    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 1002;
    private static final int IMAGE_REQUEST_CODE = 1003;

    private ImageView imageView;
    //private ListView listView;
    private ImageClassifier imageClassifier;
    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private TextView textView5;
    private SwitchCompat confSwitch;

    private TextToSpeech mTTS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUIElements();
    }

    // Initializes UI Elements
    private void initializeUIElements() {
        imageView = findViewById(R.id.iv_capture);
        //listView = findViewById(R.id.lv_probabilities);
        Button takePic = findViewById(R.id.bt_take_pic);
        Button openGallery = findViewById(R.id.bt_open_gal);
        Button speak = findViewById(R.id.bt_speak);
        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        textView4 = findViewById(R.id.textView4);
        textView5 = findViewById(R.id.textView5);
        confSwitch = findViewById(R.id.confSwitch);

        imageView.setImageResource(R.drawable.radsnapper_white);


        try {
            imageClassifier = new ImageClassifier(this);
        } catch (IOException e) {
            Log.e("Image Classifier Error", "Error" + e);
        }

        takePic.setOnClickListener(v -> {
            if (hasPermission("camera")) {
                openCamera();
            } else {
                requestPermission("camera");
            }
        });

        openGallery.setOnClickListener(v -> {
            if (hasPermission("readStorage")) {
                pickImage();
            } else {
                requestPermission("readStorage");
            }
        });

        mTTS = new TextToSpeech(getApplicationContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                mTTS.setLanguage(Locale.US);
            } else {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });

        speak.setOnClickListener(v -> {
            String wordToSpeak = textView1.getText().toString()
                    .replaceAll("[0-9](.*)", "").trim();
            if (wordToSpeak.equals("")) {
                Toast.makeText(MainActivity.this, "Invalid Word", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, wordToSpeak, Toast.LENGTH_SHORT).show();
                mTTS.speak(wordToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        confSwitch.setChecked(false);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // getting bitmap of the image
            Bitmap photo = ImageUtils.getBitmapFromIntent(this, data);
            photo = rotateBitmap(photo, 90);
            imageView.setImageBitmap(photo);
            classifyImage(photo);
            // to save captured image to storage
            //String imgPath = ImageUtils.createFile(this, photo);
            //File imageFile = new File(imgPath);

        }
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            Uri imageUri = data.getData();
            try {
                // casts URI to Bitmap
                Bitmap photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imageView.setImageBitmap(photo);
                classifyImage(photo);
            } catch (IOException e) {
                Log.e("Bitmap Error", "Error:" + e);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // Runs the image classification model and displays in list view
    private void classifyImage(Bitmap photo) {
        List<ImageClassifier.Recognition> predictions = imageClassifier.recognizeImage(
                photo, 0);
        final List<String> predictionsList = new ArrayList<>();
        final List<Double> predictionsListConf = new ArrayList<>();
        // displays predictions
        for (ImageClassifier.Recognition rec : predictions) {
            String objName = removeDigits(rec.getName());
            objName = removeOtherWords(objName);
            // converts confidence numbers into % form and limits to 2 decimal places
            double objConf = (Math.round((((double) rec.getConfidence()) * 10000.0))) / 100.0;
            predictionsList.add(objName);
            predictionsListConf.add(objConf);
        }
        replaceTexts(predictionsList, predictionsListConf);

        /*
        // creates an array adapter to display the classification result in list view
        ArrayAdapter<String> predictionsAdapter = new ArrayAdapter<>(
                this, R.layout.support_simple_spinner_dropdown_item, predictionsList.subList(0,3));
        listView.setAdapter(predictionsAdapter); */
    }

    // Updates text in the textView boxes
    private void replaceTexts(List<String> predictionsList, List<Double> predictionsListConf) {
        String text1 = predictionsList.subList(0, 1).toString()
                .replaceAll("\\[", "").replaceAll("]", "");
        String text2 = predictionsList.subList(1, 2).toString()
                .replaceAll("\\[", "").replaceAll("]", "");
        String text3 = predictionsList.subList(2, 3).toString()
                .replaceAll("\\[", "").replaceAll("]", "");

        textView4.setText(R.string.the_object_is);
        textView5.setText(R.string.or);

        textView1.setText(text1);
        textView2.setText(text2);
        textView3.setText(text3);

        // Updates textView with confidence percentages if confidence switch is on
        confSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Log.i("Switch", "ON");
                String textConf1 = text1;
                String textConf2 = text2;
                String textConf3 = text3;
                textConf1 = textConf1.concat("  " + predictionsListConf.subList(0, 1).toString()
                        .replaceAll("\\[", "").replaceAll("]", "") + "%");
                textConf2 = textConf2.concat("  " + predictionsListConf.subList(1, 2).toString()
                        .replaceAll("\\[", "").replaceAll("]", "") + "%");
                textConf3 = textConf3.concat("  " + predictionsListConf.subList(2, 3).toString()
                        .replaceAll("\\[", "").replaceAll("]", "") + "%");
                System.out.println(textConf1);
                textView1.setText(textConf1);
                textView2.setText(textConf2);
                textView3.setText(textConf3);
            } else {
                Log.i("Switch", "OFF");
                textView1.setText(text1);
                textView2.setText(text2);
                textView3.setText(text3);
            }
        });
    }

    // Requests permissions, takes in String "camera" or "readStorage"
    private void requestPermission(String permType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permType.equalsIgnoreCase("camera")) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Toast.makeText(this, "Camera Permission Required", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permType.equalsIgnoreCase("readStorage")) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Storage Permission Required", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    // Opens camera or storage/gallery after checking for permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

    // Checks for permissions if Android version is later than M
    // Checks for permissions to open camera and read storage
    private boolean hasPermission(String permType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permType.equalsIgnoreCase("camera")) {
            return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permType.equalsIgnoreCase("readStorage")) {
            return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    // Checks for all permissions
    private boolean hasAllPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    // Captures image from camera
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    // Picks image from storage/gallery
    private void pickImage() {
        Intent imageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                .setType("image/*");
        startActivityForResult(imageIntent, IMAGE_REQUEST_CODE);
    }

    // Removes digits from label name
    // Needed due to certain TensorFlow models having numbers before words in labels.txt String
    private String removeDigits(String name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            if (!Character.isDigit(name.charAt(i))) {
                sb.append(name.charAt(i));
            }
        }
        return sb.toString();
    }

    // Removes words after first comma
    // Needed due to certain TensorFlow models having multiple words in labels.txt String
    private String removeOtherWords(String name) {
        int commaIndex = name.length() - 1;
        for (int i = name.length() - 1; i >= 0; i--) {
            if (name.charAt(i) == ',') {
                commaIndex = i - 1;
            }
        }
        return name.substring(0, commaIndex + 1);
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

     /*
    private static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }
    */

    /*
    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(img, selectedImage);
        return img;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = Math.min(heightRatio, widthRatio);

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }
    */

    private static class ImageUtils {
        public static Bitmap getBitmapFromIntent(Context context, Intent data) {
            Bitmap bitmap = null;

            if (data.getData() == null) {
                bitmap = (Bitmap) data.getExtras().get("data");
            } else {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return bitmap;

        }

        public static String createFile(Context context, Bitmap data) {
            Uri selectedImage = getImageUri(context, data);
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor c = context.getContentResolver().query(selectedImage, filePath, null, null, null);
            c.moveToFirst();
            c.getColumnIndex(filePath[0]);
            int columnIndex = c.getColumnIndex(filePath[0]);
            String picturePath = c.getString(columnIndex);
            c.close();

            return picturePath;
        }

        public static Uri getImageUri(Context context, Bitmap inImage) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Image", null);
            return Uri.parse(path);
        }
    }
}