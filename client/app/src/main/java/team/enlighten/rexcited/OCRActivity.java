package team.enlighten.rexcited;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class OCRActivity extends AppCompatActivity {
    private static final String TAG = "OCR";
    CameraSource cameraSource;
    SurfaceView previewSurface;
    TextRecognizer textRecognizer;
    CameraDevice camera;
    CameraCaptureSession session;
    int captureFormat;
    Size captureSize;
    FloatingActionButton btnCapture, btnAccept, btnRetry;
    CameraManager cm;
    String recognized;

    public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {
        OcrDetectorProcessor() {
        }

        @Override
        public void release() {

        }

        @Override
        public void receiveDetections(Detector.Detections<TextBlock> detections) {
            SparseArray<TextBlock> items = detections.getDetectedItems();
            for (int i = 0; i < items.size(); ++i) {
                TextBlock item = items.valueAt(i);
                if (item != null && item.getValue() != null) {
                    Log.d("Processor", "Text detected! " + item.getValue());
                }
            }
        }
    }

    private void previewSurfaceReady() {
        List<Surface> surfaces = new LinkedList<Surface>();
        surfaces.add(previewSurface.getHolder().getSurface());
        try {
            camera.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        builder.addTarget(previewSurface.getHolder().getSurface());
                        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                        builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
                        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
                        CaptureRequest request = builder.build();
                        session.setRepeatingRequest(request, new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                super.onCaptureCompleted(session, request, result);
                            }
                        }, null);
                        previewSurface.setClickable(true);
                        btnCapture.setEnabled(true);
                        OCRActivity.this.session = session;
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        Context context = getApplicationContext();
        btnAccept.setVisibility(View.INVISIBLE);
        btnRetry.setVisibility(View.INVISIBLE);
        btnCapture.setVisibility(View.VISIBLE);
        btnCapture.setEnabled(false);
        btnCapture.bringToFront();
        previewSurface.setClickable(false);

        textRecognizer = new TextRecognizer.Builder(context).build();
        textRecognizer.setProcessor(new OcrDetectorProcessor());

        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Log.e(TAG, "low storage");
            }
        }

        try {
            for (String id : cm.getCameraIdList()) {
                CameraCharacteristics characteristic = cm.getCameraCharacteristics(id);
                if (characteristic.get(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_BACK) {
                    captureFormat = ImageFormat.JPEG;
                    captureSize = characteristic.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(captureFormat)[0];
                    if (captureSize.getWidth() < captureSize.getHeight()) {
                        captureSize = new Size(captureSize.getHeight(), captureSize.getWidth());
                    }
                    cm.openCamera(id, new CameraDevice.StateCallback() {
                        @Override
                        public void onOpened(final CameraDevice camera) {
                            OCRActivity.this.camera = camera;
                            if (previewSurface.getHolder().getSurface() != null)
                                previewSurfaceReady();
                            else {
                                previewSurface.getHolder().addCallback(new SurfaceHolder.Callback() {
                                    @Override
                                    public void surfaceCreated(SurfaceHolder holder) {
                                        previewSurfaceReady();
                                    }

                                    @Override
                                    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                                    }

                                    @Override
                                    public void surfaceDestroyed(SurfaceHolder holder) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onDisconnected(@NonNull CameraDevice camera) {

                        }

                        @Override
                        public void onError(@NonNull CameraDevice camera, int error) {

                        }
                    }, null);

                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
        }
    }

    private void triggerAF() {
        if (session == null)
            return;
        try {
            session.stopRepeating();
            final CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(previewSurface.getHolder().getSurface());
            builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
            builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
            session.capture(builder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
                    try {
                        session.setRepeatingRequest(builder.build(), new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                super.onCaptureCompleted(session, request, result);
                            }
                        }, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void capture() {
        btnCapture.setEnabled(false);
        final ImageReader imageReader = ImageReader.newInstance(captureSize.getWidth(), captureSize.getHeight(), captureFormat, 1);
        List<Surface> surfaces = new LinkedList<Surface>();
        surfaces.add(imageReader.getSurface());
        try {
            if (session != null) {
                session.stopRepeating();
                session.close();
                session = null;
            }

            camera.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(final CameraCaptureSession session) {
                    CaptureRequest.Builder builder = null;
                    try {
                        builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                        builder.addTarget(imageReader.getSurface());
                        CaptureRequest request = builder.build();
                        session.capture(request, new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                Image image = imageReader.acquireLatestImage();
                                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                                byte[] data = new byte[buffer.capacity()];
                                buffer.get(data);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                                Frame frame = new Frame.Builder()
                                        .setBitmap(bitmap)
                                        .build();
                                recognized = "";
                                SparseArray<TextBlock> items = textRecognizer.detect(frame);
                                for (int i = 0; i < items.size(); ++i) {
                                    TextBlock item = items.valueAt(i);
                                    if (item != null && item.getValue() != null)
                                        recognized = recognized + item.getValue().replaceAll("\n", " ") + "\n\n";
                                }

                                previewSurface.setClickable(false);
                                btnCapture.setVisibility(View.INVISIBLE);
                                btnAccept.setVisibility(View.VISIBLE);
                                btnRetry.setVisibility(View.VISIBLE);
                                ((View) btnRetry.getParent()).bringToFront();
                            }
                        }, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        previewSurface = (SurfaceView) findViewById(R.id.ocr_preview);
        btnCapture = (FloatingActionButton) findViewById(R.id.btn_capture);
        btnAccept = (FloatingActionButton) findViewById(R.id.btn_accept);
        btnRetry = (FloatingActionButton) findViewById(R.id.btn_retry);

        cm = (CameraManager) getSystemService(CAMERA_SERVICE);

        previewSurface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerAF();
            }
        });
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capture();
            }
        });
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.close();
                session = null;
                camera.close();
                camera = null;
                Intent intent = new Intent(getApplicationContext(), EditFileActivity.class);
                recognized = recognized.replaceAll("\n", " ");
                for (; ; ) {
                    String it = recognized.replaceAll("  ", " ");
                    if (it.equals(recognized))
                        break;
                    recognized = it;
                }
                intent.putExtra("content", recognized);
                startActivity(intent);
                finish();
            }
        });
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPreview();
            }
        });

        previewSurface.setClickable(false);
        btnCapture.setEnabled(false);
        btnAccept.setVisibility(View.INVISIBLE);
        btnRetry.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA
            }, 0);
        } else
            startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (session != null) {
            session.close();
            session = null;
        }
        if (camera != null) {
            camera.close();
            camera = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        startPreview();
    }
}
