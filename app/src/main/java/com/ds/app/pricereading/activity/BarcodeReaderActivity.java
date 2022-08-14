package com.ds.app.pricereading.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.math.MathUtils;

import com.ds.app.pricereading.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.Arrays;
import java.util.List;

public class BarcodeReaderActivity extends AppCompatActivity {

    public static final int RESULT_CODE_MAIN_OK = 1;
    public static final int RESULT_CODE_MAIN_KO = 2;

    public static final String EXTRA_KEY_MAIN_OUTPUT_MESSAGE = "message";
    public static final String EXTRA_KEY_MAIN_OUTPUT_BARCODE = "barcode";

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startCamera() {

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        String cameraId;
        CameraCharacteristics cameraCharacteristics;

        if ((cameraId = getBackCamera(cameraManager)) == null
                || (cameraCharacteristics = getCameraCharacteristics(cameraManager, cameraId)) == null) {
            setResultAndExit(null);
            return;
        }

        Rect sensorSize = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        Rect zoomRegion = createZoomRegion(sensorSize, cameraCharacteristics);

        ImageReader imageReader = ImageReader.newInstance(
                zoomRegion.width(),
                zoomRegion.height(),
                ImageFormat.JPEG,
                2
        );
        imageReader.setOnImageAvailableListener(createImageAvailableListener(), null);

        captureImageThread.setImageReader(imageReader);

        SurfaceView surfaceView = findViewById(R.id.temp_surfaceview2);
        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(createSurfaceHolderCallback());
        holder.setFixedSize(zoomRegion.width(), zoomRegion.height());

        try {
            cameraManager
                    .openCamera(
                            cameraId,
                            createCameraDeviceStateCallback(
                                    surfaceView,
                                    imageReader,
                                    zoomRegion
                            ),
                            null
                    );
        } catch (Exception e) {
            setResultAndExit(e);
        }

    }

    private SurfaceHolder.Callback createSurfaceHolderCallback() {
        return new SurfaceHolder.Callback() {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            }

        };
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private Rect createZoomRegion(Rect sensorSize, CameraCharacteristics cameraCharacteristics) {
        if (sensorSize != null) {

            Float value = cameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
            Float maxZoom = value == null || value < DEFAULT_ZOOM_FACTOR ? DEFAULT_ZOOM_FACTOR : value;
            float newZoom = MathUtils.clamp(2f, DEFAULT_ZOOM_FACTOR, maxZoom);

            int centerX = sensorSize.width() / 2;
            int centerY = sensorSize.height() / 2;
            int deltaX = (int) (0.5f * sensorSize.width() / newZoom);
            int deltaY = (int) (0.5f * sensorSize.height() / newZoom);

            Rect cropRegion = new Rect();
            cropRegion.set(
                    centerX - deltaX,
                    centerY - deltaY,
                    centerX + deltaX,
                    centerY + deltaY
            );

            return cropRegion;

        } else {
            return sensorSize;
        }
    }

    private void setResultAndExit(Exception e) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_MESSAGE, "Errore tecnico: Impossibile leggere il codice a barre");
        setResult(RESULT_CODE_MAIN_KO, intent);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_reader_layout);

        captureImageThread = new CaptureImageThread();
        captureImageThread.start();

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            } else {
                startCamera();
            }
        } catch (Exception e) {
            setResultAndExit(e);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startCameraCaptureSession(
            CameraDevice cameraDevice,
            Surface previewSurface,
            ImageReader imageReader,
            Rect cropRegion
    ) throws CameraAccessException {

        try {
            cameraDevice
                    .createCaptureSession(
                            Arrays.asList(previewSurface, imageReader.getSurface()),
                            createCameraCaptureSessionStateCallback(previewSurface, cropRegion, cameraDevice),
                            null
                    );

        } catch (Exception e) {
            setResultAndExit(e);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private String getBackCamera(CameraManager cameraManager) {
        String cameraId = null;
        try {
            String[] cameraIds = cameraManager.getCameraIdList();
            for (int i = 0; i < cameraIds.length; i++) {
                String tempId = cameraIds[i];
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(tempId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = tempId;
                    break;
                }
            }
        } catch (Exception e) {

        }
        return cameraId;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        startCamera();

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private CameraCaptureSession.StateCallback createCameraCaptureSessionStateCallback(
            Surface previewSurface,
            Rect cropRegion,
            CameraDevice cameraDevice
    ) {
        return new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                try {

                    CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(
                            CameraDevice.TEMPLATE_PREVIEW
                    );
                    captureRequestBuilder.addTarget(previewSurface);
                    // captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                    // captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
                    captureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, cropRegion);

                    CaptureRequest captureRequest = captureRequestBuilder.build();

                    cameraCaptureSession
                            .setRepeatingRequest(
                                    captureRequest,
                                    null,
                                    null
                            );

                    captureImageThread.setCameraCaptureSession(cameraCaptureSession);

                } catch (Exception e) {
                    setResultAndExit(e);
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                setResultAndExit(null);
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private CameraDevice.StateCallback createCameraDeviceStateCallback(
            SurfaceView surfaceView,
            ImageReader imageReader,
            Rect cropRegion
    ) {
        return new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                try {
                    startCameraCaptureSession(
                            camera,
                            surfaceView.getHolder().getSurface(),
                            imageReader,
                            cropRegion
                    );
                } catch (Exception e) {
                    setResultAndExit(e);
                }
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                camera.close();
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                camera.close();
                setResultAndExit(null);
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private CameraCharacteristics getCameraCharacteristics(CameraManager cameraManager, String cameraId) {
        try {
            return cameraManager.getCameraCharacteristics(cameraId);
        } catch (Exception e) {
            return null;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startCaptureRequest(
            ImageReader imageReader,
            CameraCaptureSession cameraCaptureSession
    ) {
        try {
            CaptureRequest.Builder takePictureBuilder = cameraCaptureSession
                    .getDevice()
                    .createCaptureRequest(
                            CameraDevice.TEMPLATE_STILL_CAPTURE
                    );
            takePictureBuilder.addTarget(imageReader.getSurface());
            cameraCaptureSession.capture(
                    takePictureBuilder.build(),
                    new CameraCaptureSession.CaptureCallback() {

                        public void onCaptureStarted(CameraCaptureSession session,
                                                     CaptureRequest request, long timestamp, long frameNumber) {
                        }

                        public void onCapturePartial(CameraCaptureSession session,
                                                     CaptureRequest request, CaptureResult result) {
                        }

                        public void onCaptureProgressed(CameraCaptureSession session,
                                                        CaptureRequest request, CaptureResult partialResult) {
                        }

                        public void onCaptureFailed(CameraCaptureSession session,
                                                    CaptureRequest request, CaptureFailure failure) {
                        }

                    },
                    null
            );
        } catch (CameraAccessException e) {
            setResultAndExit(e);
        }
    }

    private ImageReader.OnImageAvailableListener createImageAvailableListener() {
        return new ImageReader.OnImageAvailableListener() {
            public void onImageAvailable(ImageReader reader) {
                try (Image image = reader.acquireNextImage()) {
                    Image.Plane[] planes = image.getPlanes();
                    if (planes.length > 0) {
                        // ByteBuffer buffer = planes[0].getBuffer();
                        // byte[] data = new byte[buffer.remaining()];
                        // buffer.get(data);
                        processImage(image);
                    } else {
                        captureImageThread.setTimer(500);
                    }
                }
            }
        };
    }

    private void processImage(Image image) {

        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_EAN_13,
                                Barcode.FORMAT_EAN_8
                        )
                        .build();

        BarcodeScanner scanner = BarcodeScanning.getClient(options);
        scanner
                .process(InputImage.fromMediaImage(image, 0))
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodeList) {
                        if (!barcodeList.isEmpty()) {

                            String barcode = barcodeList.get(0).getRawValue();

                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_KEY_MAIN_OUTPUT_BARCODE, barcode);

                            setResult(RESULT_CODE_MAIN_OK, intent);
                            finish();

                        } else {
                            captureImageThread.setTimer(500);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });

    }

    private CaptureImageThread captureImageThread;

    private static final float DEFAULT_ZOOM_FACTOR = 2.0f;

    public class CaptureImageThread extends Thread {

        public void run() {
            Looper.prepare();

            handler = new Handler(Looper.myLooper()) {
                @RequiresApi(api = Build.VERSION_CODES.M)
                public void handleMessage(Message message) {
                    try {
                        if (imageReader != null && cameraCaptureSession != null) {
                            startCaptureRequest(imageReader, cameraCaptureSession);
                        } else {
                            setTimer(500);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            setTimer(500);

            Looper.loop();
        }

        public void setImageReader(ImageReader imageReader) {
            this.imageReader = imageReader;
        }

        public void setCameraCaptureSession(CameraCaptureSession cameraCaptureSession) {
            this.cameraCaptureSession = cameraCaptureSession;
        }

        public void clearDependencies() {
            imageReader = null;
            cameraCaptureSession = null;
        }

        public void setTimer(int i) {
            Message msg = new Message();
            handler.sendMessageDelayed(msg, i);
        }

        private Handler handler;
        private ImageReader imageReader;
        private CameraCaptureSession cameraCaptureSession;

    }

}