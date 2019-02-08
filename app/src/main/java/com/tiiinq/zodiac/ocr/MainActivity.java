package com.tiiinq.zodiac.ocr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tiiinq.zodiac.ocr.api.MiddleDB;
import com.tiiinq.zodiac.ocr.models.MatchedItems;
import com.tiiinq.zodiac.ocr.ui.camera.CameraSourcePreview;
import com.tiiinq.zodiac.ocr.ui.camera.GraphicOverlay;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";
    String controller;
    TextView textView_OFiD;
    TextView textView_ItemId;
    TextView textView_Counter;
    TextView textView_Scan;
    String OFiD = "";
    String item_Id = "";
    Date date;
    int itemCounter;
    Button button_Save;
    Button button_Cancel;
    Button button_Valid;
    Button button_Edit;
    Button button_Add;
    Button button_List;
    ArrayList<String> tab_OF = new ArrayList<>();
    ArrayList<String> controlItemArrayList = new ArrayList<>();
    MiddleDB MiddleWare = new MiddleDB();
    private static final int requestPermissionID = 101;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    MatchedItems FullItems = new MatchedItems();
    com.tiiinq.zodiac.ocr.ui.camera.CameraSource mCameraSource;
    TextRecognizer textRecognizer;


    // Intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    // Constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final String TextBlockObject = "String";

    private CameraSourcePreview mPreview;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;

    // Helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView_OFiD = findViewById(R.id.textView_OFiD);
        textView_ItemId = findViewById(R.id.textView_ItemId);
        textView_Scan = findViewById(R.id.textView_Scan);
        textView_Counter = findViewById(R.id.textView_Counter);
        button_Valid = findViewById(R.id.button_Valid);
        button_Edit = findViewById(R.id.button_Edit);
        button_Add = findViewById(R.id.button_Add);
        button_List = findViewById(R.id.button_List);
        button_Save = findViewById(R.id.button_Save);
        button_Cancel = findViewById(R.id.button_Cancel);
        tab_OF = MiddleWare.GetCalibratedItems();
        mPreview = findViewById(com.tiiinq.zodiac.ocr.R.id.preview);
        mGraphicOverlay = findViewById(com.tiiinq.zodiac.ocr.R.id.graphicOverlay);

        // A text recognizer is created to find text.  An associated processor instance
        // is set to receive the text recognition results and display graphics for each text block
        // on screen.
        textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        // read parameters from the intent used to launch the activity.
        boolean autoFocus = getIntent().getBooleanExtra(AutoFocus, false);
        boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash);
        } else {
            requestCameraPermission();
        }

        //OCR Motion utils
        gestureDetector = new GestureDetector(this, new CaptureGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        Snackbar.make(mGraphicOverlay, "Tap to capture. Pinch/Stretch to zoom",
                Snackbar.LENGTH_LONG)
                .show();

        //Start Camera and Detection
        startCameraSource();
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, com.tiiinq.zodiac.ocr.R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(com.tiiinq.zodiac.ocr.R.string.ok, listener)
                .show();
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);

        boolean c = gestureDetector.onTouchEvent(e);

        return b || c || super.onTouchEvent(e);
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the ocr detector to detect small text samples
     * at long distances.
     * <p>
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {


        if (!textRecognizer.isOperational()) {
            // Note: The first time that an app using a Vision API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any text,
            // barcodes, or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, com.tiiinq.zodiac.ocr.R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(com.tiiinq.zodiac.ocr.R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the text recognizer to detect small pieces of text.
        mCameraSource = new com.tiiinq.zodiac.ocr.ui.camera.CameraSource.Builder(getApplicationContext(), textRecognizer)
                .setFacing(com.tiiinq.zodiac.ocr.ui.camera.CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setRequestedFps(2.0f)
                .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null)
                .build();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // We have permission, so create the camerasource
            boolean autoFocus = getIntent().getBooleanExtra(AutoFocus, false);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
            createCameraSource(autoFocus, useFlash);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Multitracker sample")
                .setMessage(com.tiiinq.zodiac.ocr.R.string.no_camera_permission)
                .setPositiveButton(com.tiiinq.zodiac.ocr.R.string.ok, listener)
                .show();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {

        // Check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
        //Create the TextRecognizer

        textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {

                mGraphicOverlay.clear();
                final SparseArray<TextBlock> items = detections.getDetectedItems();

                if (items.size() != 0) {
                    FullItems.setArrayListControlItem(controlItemArrayList);
                    FullItems.setOF_Id(OFiD);
                    for (int i = 0; i < items.size(); i++) {
                        TextBlock item = items.valueAt(i);
                        OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);
                        mGraphicOverlay.add(graphic);

                        if (tab_OF.contains(item.getValue())) {
                            graphic.getsRectPaint().setColor(Color.GREEN);
                            graphic.getsTextPaint().setColor(Color.GREEN);
                            OFiD = item.getValue();
                            textView_OFiD.setText(OFiD);
                            if (textView_OFiD.getCurrentTextColor() == Color.rgb(0, 200, 0)) {
                                textView_OFiD.setTextColor(Color.rgb(0, 200, 0));
                            } else {
                                textView_OFiD.setTextColor(Color.rgb(200, 0, 0));
                            }
                        }

                        if (item.getValue().equals("ABCDE-11") || item.getValue().equals("ABCDE-23") || item.getValue().equals("FGHIJK-11") || item.getValue().equals("FGHIJK-23") || item.getValue().equals("M10X1.5")) {
                            graphic.getsRectPaint().setColor(Color.GREEN);
                            graphic.getsTextPaint().setColor(Color.GREEN);
                            item_Id = item.getValue();
                            textView_ItemId.setText(item_Id);
                            textView_ItemId.setTextColor(Color.rgb(0, 200, 0));
                        }
                    }
                    date = new Date();
                    FullItems.setDate(date);
                }
            }
        });
    }

    /**
     * onTap is called to capture the first TextBlock under the tap location and return it to
     * the Initializing Activity.
     *
     * @param rawX - the raw position of the tap
     * @param rawY - the raw position of the tap.
     * @return true if the activity is ending.
     */
    private boolean onTap(float rawX, float rawY) {
        OcrGraphic graphic = mGraphicOverlay.getGraphicAtLocation(rawX, rawY);
        TextBlock text = null;
        if (graphic != null) {
            text = graphic.getTextBlock();
            if (text != null && text.getValue() != null) {
                Intent data = new Intent();
                data.putExtra(TextBlockObject, text.getValue());
                setResult(CommonStatusCodes.SUCCESS, data);
                finish();
            } else {
                Log.d(TAG, "text data is null");
            }
        } else {
            Log.d(TAG, "no text detected");
        }
        return text != null;
    }


    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         * <p/>
         * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
         * and {@link ScaleGestureDetector#getFocusY()} will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         */
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mCameraSource.doZoom(detector.getScaleFactor());
        }
    }

    public void saveMatchedItems(View v) {

        // Add a new document with a generated ID
        db.collection("MatchedItems")
                .document("CoupleItems")
                .collection("Calib")
                .add(FullItems)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        FullItems.reset();
                        itemCounter = 0;
                        textView_OFiD.setText(getString(R.string.txt_message));
                        textView_ItemId.setText(getString(R.string.txt_message2));
                        textView_Counter.setText(R.string.counter);
                        textView_ItemId.setTextColor(Color.rgb(0, 0, 0));
                        textView_OFiD.setTextColor(Color.rgb(0, 0, 0));
                        textView_Scan.setText("SCAN OF");
                        itemCounter = 0;
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        Toast.makeText(getApplicationContext(), "Pièces bien sauvegardées", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        Toast.makeText(getApplicationContext(), "Erreur lors de la sauvegarde", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void cancelMatchedItems(View view) {
        FullItems.reset();
        textView_OFiD.setText(getString(R.string.txt_message));
        textView_ItemId.setText(getString(R.string.txt_message2));
        textView_Counter.setText(R.string.counter);
        textView_Scan.setText("SCAN OF");
        textView_ItemId.setTextColor(Color.rgb(0, 0, 0));
        textView_OFiD.setTextColor(Color.rgb(0, 0, 0));
        OFiD = "";
        item_Id = "";
        Toast.makeText(getApplicationContext(), "Sauvegarde annulée", Toast.LENGTH_LONG).show();
    }

    public void onClickBtnValid(View view) {
        //   FullItems.setOF_Id(textView_OFiD.getText().toString());
        //   FullItems.setOF_Id(textView_OFiD.getText().toString());
        textView_Scan.setText("SCAN PIECES DE CONTROLE");
        textView_OFiD.setTextColor(Color.rgb(0, 200, 0));
    }

    public void onClickBtnEdit(View view) {
        FullItems.reset();
        textView_OFiD.setText(R.string.txt_message);
        textView_OFiD.setTextColor(Color.rgb(0, 0, 0));
        textView_Scan.setText("SCAN OF");
    }

    public void onClickBtnAdd(View view) {
        controller = item_Id;
        controlItemArrayList.add(controller);
        itemCounter();
        textView_ItemId.setText(R.string.txt_message2);
        textView_ItemId.setTextColor(Color.rgb(0, 0, 0));
    }

    public void onClickBtnList(View view) {
        Intent i = new Intent(this, ListActivity.class);
        i.putExtra("key", (Serializable) FullItems);
        startActivity(i);
    }

    public void itemCounter() {
        itemCounter = itemCounter + 1;
        textView_Counter.setText("Nomnbre de pièces : " + itemCounter); //compteur
    }
}


