package org.opencv.samples.imagemanipulations;

import java.util.Arrays;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ImageManipulationsActivity extends Activity implements CvCameraViewListener2,OnSeekBarChangeListener {
    private static final String  TAG                 = "OCVSample::Activity";

    public static final int      VIEW_MODE_RGBA      = 0;
    public static final int      VIEW_MODE_ZOOM      = 5;

    private MenuItem             mItemPreviewRGBA;
    private MenuItem             mItemPreviewZoom;

    private CameraBridgeViewBase mOpenCvCameraView;
    private Size				 mSize0;

    private Mat                  mIntermediateMat;
    private Mat					 mIntermediateMat1;
    private int                  mHistSizeNum = 25;


    public static int           viewMode = VIEW_MODE_ZOOM;

    private SeekBar sb;
    private SeekBar sb1;
    private SeekBar sb2;
    private TextView tv;
    
    private int threshold;
    private int threshold1;
    private int threshold2;
    
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ImageManipulationsActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.image_manipulations_surface_view);
        
        sb = (SeekBar) findViewById(R.id.sb);
        sb1 = (SeekBar) findViewById(R.id.sb1);
        sb2 = (SeekBar) findViewById(R.id.sb2);
        tv = (TextView) findViewById(R.id.tv);
        tv.setText("current value is"+sb.getProgress());
        
        
        sb.setOnSeekBarChangeListener(this);
        sb1.setOnSeekBarChangeListener(this);
        sb2.setOnSeekBarChangeListener(this);
        
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.image_manipulations_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }
    
    
    public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
    	tv.setText(" 1 is"+sb.getProgress()+" 2 is"+sb1.getProgress()+" 3 is"+sb2.getProgress());
    	threshold = sb.getProgress();
    	threshold1 = sb1.getProgress();
    	threshold2 = sb2.getProgress();
    }

    
    public void onStartTrackingTouch(SeekBar seekBar) {};
    
    public void onStopTrackingTouch(SeekBar seekBar) {};
    
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
    	
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }



    public void onCameraViewStarted(int width, int height) {
        mIntermediateMat = new Mat();
        mIntermediateMat1 = new Mat();
        mSize0 = new Size();
    }

    public void onCameraViewStopped() {
        // Explicitly deallocate Mats
        if (mIntermediateMat != null)
            mIntermediateMat.release();

        mIntermediateMat = null;
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();
        Size sizeRgba = rgba.size();

        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;

        switch (ImageManipulationsActivity.viewMode) {
        case ImageManipulationsActivity.VIEW_MODE_RGBA:
            break;
            
        case ImageManipulationsActivity.VIEW_MODE_ZOOM:
            Mat zoomCorner = rgba.submat(0, rows, 0, cols/2);            
            
            Mat mZoomWindow = rgba.submat(0, rows, cols / 2, cols);
            Imgproc.resize(mZoomWindow, zoomCorner, zoomCorner.size());
            //Imgproc.Canny(mZoomWindow, mIntermediateMat, (double) threshold, 90);
            //Imgproc.cvtColor(mIntermediateMat, mZoomWindow, Imgproc.COLOR_GRAY2BGRA, 4);
            //Imgproc.threshold(mZoomWindow, mZoomWindow, (double) threshold, 255, Imgproc.THRESH_BINARY);
            //Imgproc.cvtColor(mIntermediateMat, mZoomWindow, Imgproc.COLOR_GRAY2RGBA, 4);
            Imgproc.cvtColor(mZoomWindow, mIntermediateMat1, Imgproc.COLOR_RGBA2BGR, 3);
            Imgproc.cvtColor(mIntermediateMat1, mIntermediateMat1, Imgproc.COLOR_BGR2GRAY, 2);
            //Imgproc.cvtColor(mIntermediateMat1, mIntermediateMat1, Imgproc.COLOR_BGR2RGBA, 4);
            
            //Core.extractChannel(mIntermediateMat1, mIntermediateMat1, 1);
            
            //Imgproc.cvtColor(mIntermediateMat1, mZoomWindow, Imgproc.COLOR_GRAY2BGRA, 4);
            
            //Core.extractChannel(mZoomWindow, mIntermediateMat, 1);
            Imgproc.threshold(mIntermediateMat1, mIntermediateMat1, (double) threshold, 255, Imgproc.THRESH_TOZERO_INV);
            //Imgproc.threshold(mIntermediateMat1, mIntermediateMat1, (double) threshold1, 255, Imgproc.THRESH_TOZERO);
            Imgproc.threshold(mIntermediateMat1, mIntermediateMat1, (double) threshold2, 255, Imgproc.THRESH_BINARY);
            
            //there need to do resize, linear
            
            Imgproc.cvtColor(mIntermediateMat1, mZoomWindow, Imgproc.COLOR_GRAY2BGRA, 4);
            Imgproc.resize(mZoomWindow, mIntermediateMat, mSize0, 0.1, 0.1, Imgproc.INTER_NEAREST);
            Imgproc.resize(mIntermediateMat, mZoomWindow, mZoomWindow.size(), 0., 0., Imgproc.INTER_NEAREST);
            
            Size wsize = mZoomWindow.size();
            Core.rectangle(mZoomWindow, new Point(1, 1), new Point(wsize.width - 2, wsize.height - 2), new Scalar(255, 0, 0, 255), 2);
            zoomCorner.release();
            mZoomWindow.release();
            break;

        }

        return rgba;
    }
}
