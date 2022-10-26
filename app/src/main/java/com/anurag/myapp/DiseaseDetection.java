package com.anurag.myapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiseaseDetection extends AppCompatActivity {
    AssetManager assetManager;
    ByteBuffer imgData;
    int[] intValues = new int[224 * 224];
    static final int IMAGE_MEAN = 128;
    static final float IMAGE_STD = 128.0f;
    ArrayList<String> labelList;
    ImageAnalysis imageAnalysis;
    Interpreter interpreter;
    PreviewView previewView;
    TextView textView;
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ExecutorService executor;
    CameraSelector cameraSelector;
    Preview preview;
    Camera camera;
    String output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_detection);
        previewView = findViewById(R.id.previewView);
        textView = findViewById(R.id.textView);
        labelList = new ArrayList<>();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        executor = Executors.newSingleThreadExecutor();
        assetManager = getAssets();
        startCamera();
    }

    private void startCamera() {

        // Method to Start Live feed
        if(checkPermissions()) {
            cameraProviderFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);
                } catch (InterruptedException | ExecutionException e) {
                }
            }, ContextCompat.getMainExecutor(this));
        }
        else {
            ActivityCompat.requestPermissions(DiseaseDetection.this,new String[]{Manifest.permission.CAMERA},100);
        }
    }

    @SuppressLint("RestrictedApi")
    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        //Method to create preview and bind it to the preview lifecycle
        CameraX.unbindAll();
        preview = new Preview.Builder().build();
        imageAnalysis = new ImageAnalysis.Builder().setBackgroundExecutor(executor).build();
        imageAnalysis.setAnalyzer(executor, image -> {
            Bitmap bitmap;
            bitmap = DiseaseDetection.this.imageProxyToBitmap(image);
            try {
                classify(bitmap);
                image.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        preview.setSurfaceProvider(previewView.createSurfaceProvider(camera.getCameraInfo()));
    }

    private void classify(Bitmap bitmap) throws IOException {

        //Method to classify image
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open("labels.txt")));
        String line;
        labelList.clear();
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        float[][] labelProbArray = new float[1][labelList.size()];
        //noinspection deprecation
        interpreter = new Interpreter(loadModelFile());
        //noinspection PointlessArithmeticExpression
        imgData = ByteBuffer.allocateDirect(4 * 1 * 224 * 224 * 3);
        imgData.order(ByteOrder.nativeOrder());
        imgData = bitmapToByteBuffer(bitmap);
        interpreter.run(imgData, labelProbArray);
        interpreter.close();
        int index = getMaxIndex(labelProbArray,labelList.size());
        output = labelList.get(index);
        runOnUiThread(() -> textView.setText(output));
    }

    private int getMaxIndex(float[][] arr,int size) {

        // Method to obtain the label with highest probability
        int maxInd = 0;
        float max = arr[0][0];
        for (int i = 0; i<1; i++) {
            for (int j = 0; j<size; j++) {
                if(arr[i][j] > max) {
                    maxInd = j;
                    max = arr[i][j];
                }
            }
        }
        return maxInd;
    }

    private ByteBuffer bitmapToByteBuffer(Bitmap bitmap) {

        // Method to create ByteBuffer from Bitmap
        imgData.rewind();
        bitmap.getPixels(intValues,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
        int pixel = 0;
        for (int i=0; i<224 ; ++i){
            for (int j=0; j<224 ; ++j){
                final int val = intValues[pixel++];
                imgData.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                imgData.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                imgData.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
            }
        }
        return imgData;
    }

    private MappedByteBuffer loadModelFile() throws IOException {

        // Method to include the Tflite model
        AssetFileDescriptor fileDescriptor = assetManager.openFd("model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {

        // Method to create Bitmap
        ImageProxy.PlaneProxy planeProxy = image.getPlanes()[0];
        ByteBuffer yBuffer = planeProxy.getBuffer();
        planeProxy = image.getPlanes()[1];
        ByteBuffer uBuffer = planeProxy.getBuffer();
        planeProxy = image.getPlanes()[2];
        ByteBuffer vBuffer = planeProxy.getBuffer();
        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();
        byte[] b = new byte[ySize + uSize + vSize];
        yBuffer.get(b,0,ySize);
        vBuffer.get(b,ySize,vSize);
        uBuffer.get(b,ySize+vSize,uSize);
        YuvImage yuvImage = new YuvImage(b, 17,image.getWidth(),image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, out);
        byte[] imageBytes = out.toByteArray();
        Bitmap bit = BitmapFactory.decodeByteArray(imageBytes,0,imageBytes.length);
        return (Bitmap.createScaledBitmap(bit,224,224,false));
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(DiseaseDetection.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            }
        }
    }
}
