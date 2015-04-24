package org.opencv.samples.imagemanipulations;

import java.io.File;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

public class ImageManipulationsActivity extends Activity implements CvCameraViewListener2{
    private static final String  TAG                 = "OCVSample::Activity";

    public static final int      VIEW_MODE_RGBA      = 0;
    public static final int      VIEW_MODE_ZOOM      = 5;


    private CameraBridgeViewBase mOpenCvCameraView;
    private Size				 mSize0;

    private Mat                  mIntermediateMat;
    private Mat					 mIntermediateMat1;
    private Mat					 mHSV;
    private Mat					 mask;
    
    public static int           viewMode = VIEW_MODE_ZOOM;  
    
    private int frame = 1;
    private int [] Flag = new int [2];   
    
    private int[] App = new int[4];  
    private VideoView videoView;   
    
    MediaController  mediaController;
   
    File videoFile = new File("");

    
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
        
        Intent intent = getIntent();
        String action = intent.getAction();
        if(intent.ACTION_VIEW.equals(action))
        	videoFile = new File("/sdcard/"+getFileName(intent.getDataString()));
        videoView=(VideoView)findViewById(R.id.videoView1); 
        mediaController=(MediaController)findViewById(R.id.mediaController1);
      
        mediaController=new MediaController(this){
        	//for not hiding
            @Override
            public void hide() {}

            //for 'back' key action
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    Activity a = (Activity)getContext();
                    a.finish();
                }
                return true;
            }

        };
        	

        for(int i=0; i<App.length; i++)
        	App[i] = 0;
        
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.image_manipulations_activity_surface_view);
        mOpenCvCameraView.setCameraIndex(0);
        mOpenCvCameraView.setCvCameraViewListener(this);
        
        playVideo(videoFile);
                
    }
    
 
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
        mHSV = new Mat();
        mask = new Mat();
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
            
            Mat mZoomWindow = rgba.submat(0, rows, cols/2, cols);
            Imgproc.resize(mZoomWindow, zoomCorner, zoomCorner.size());
            Imgproc.cvtColor(mZoomWindow, mIntermediateMat1, Imgproc.COLOR_RGBA2BGR, 3);
            Imgproc.cvtColor(mIntermediateMat1, mHSV, Imgproc.COLOR_BGR2HSV, 3);
           
            Core.extractChannel(mHSV, mIntermediateMat1, 1);
        
            Imgproc.threshold(mIntermediateMat1, mIntermediateMat1, (double) 255, 255, Imgproc.THRESH_TOZERO_INV);
            Imgproc.threshold(mIntermediateMat1, mask, (double) 208, 255, Imgproc.THRESH_BINARY); 
            Imgproc.resize(mask, mIntermediateMat, mSize0, 0.1, 0.1, Imgproc.INTER_NEAREST);
            Imgproc.resize(mIntermediateMat, mask, mask.size(), 0., 0., Imgproc.INTER_NEAREST);
            
            Size msize = mask.size();
            int mheight = (int) msize.height;
            int mwidth = (int) msize.width;
            
            Mat top = new Mat();
            Mat bottom = new Mat();
            Mat right = new Mat();
            Mat left = new Mat();
            
            top = mask.submat(0, mheight/2, 0, mwidth);
            bottom = mask.submat(mheight/2, mheight, 0, mwidth);
            right = mask.submat(0, mheight, mwidth/2, mwidth);
            left = mask.submat(0, mheight, 0, mwidth/2);
            
      
           
            if(Core.countNonZero(top) > mheight*mwidth/4 && Core.countNonZero(bottom) < mheight*mwidth/9  ){
            	if(App[0]!=0)
            		Core.rectangle(zoomCorner, new Point(0, 0), new Point(msize.width, msize.height/2 ), new Scalar(255, 0, 0, 255), 2);            
            	App[0]++;
            }
        
            if(Core.countNonZero(bottom) > mheight*mwidth/4 ){
            	if(App[1]!=0)
            		Core.rectangle(zoomCorner, new Point(0, msize.height/2), new Point(msize.width, msize.height), new Scalar(255,225, 0, 255), 2);
            	App[1]++;
            }
           
            if(Core.countNonZero(right) > mheight*mwidth/4 /*&& Core.countNonZero(left) < mheight*mwidth/9*/){
   //         	if(App[2]!=0)
  //          		Core.rectangle(zoomCorner, new Point(msize.width/2, 0), new Point(msize.width, msize.height), new Scalar(0, 255, 0, 255), 2);
            	App[2]++;
            }
            
            if(Core.countNonZero(left) > mheight*mwidth/4 /*&& Core.countNonZero(right)< mheight*mwidth/9*/){
   //         	if(App[3]!=0)
    //        		Core.rectangle(zoomCorner, new Point(0, 0), new Point(msize.width/2, msize.height), new Scalar(255, 0, 255, 255), 2);
            	App[3]++;
            }
            
            switch(frame){
            case 2: for(int i=0; i<App.length; i++)
            			App[i]=0;
            		frame++;
    				break;
            case 3: for(int i=0; i<App.length; i++){
            			if(App[i] != 0){
            				Flag[0] = i;
            				frame++;
            				break;
            			}
           			}
            		break;
            case 15: switch(Flag[0]){
            			case 0: if(App[1]!=0)
            						Operation(0);
            					else
            						Operation(2);
            					break;
            			case 1: if(App[0]!=0)
            						Operation(1);
            					else
            						Operation(3);
            					break;
            			case 2: if(App[3]!=0)
            						Operation(0);
            					break;
            			case 3: if(App[2]!=0)
            						Operation(0);
            					break;
            		}
            		frame = 1;
            		for(int i=0; i<App.length; i++)
            			App[i]=0;
            		break;
            default:switch(Flag[0]){
						case 0: App[2]=App[3]=0;
								break;
						case 1: App[2]=App[3]=0;
								break;
						case 2: App[0]=App[1]=0;
								break;
						case 3: App[0]=App[1]=0;
								break;
					}   		
            		frame++;
            		 break;
            }
             
        
            Imgproc.cvtColor(mask, mZoomWindow, Imgproc.COLOR_GRAY2BGRA, 4);
            Size wsize = mZoomWindow.size();
            Core.rectangle(mZoomWindow, new Point(1, 1), new Point(wsize.width - 2, wsize.height - 2), new Scalar(255, 0, 0, 255), 2);
            zoomCorner.release();
            mZoomWindow.release();
            break;

        }

        return rgba;
    }
    
    //call other application
    public void Operation(int flag){
    	switch(flag){
    	case 0: PauseOrStart();
    			
        		break;
        		
    	case 1: stop();
    			
		        break;
    	case 2:fastForward();
    		
    		break;
    	case 3: fastBackward();
    		break;
    	}  	
    }
    
    
    public void playVideo(File videoFile){
    	if (videoFile.exists()) {  
            System.out.println("文件存在");
             
    	 
         System.out.println(videoFile.getAbsolutePath());  
         
          
         videoView.setVideoPath(videoFile.getAbsolutePath()); 
         // 设置VideView与MediaController建立关联  
         mediaController.setAnchorView(videoView);
         videoView.setMediaController(mediaController);  
         mediaController.setMediaPlayer(videoView);
       
         mediaController.requestFocus();
         // 开始播放  
         videoView.start();
         videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
             @Override
             public void onPrepared(MediaPlayer mp) {
                 videoView.start();
                 mediaController.show(900000000);
             }
         });

         //finish after playing
         videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
             @Override
             public void onCompletion(MediaPlayer mediaPlayer) {
                 finish();
             }
         });
     	 
       
    	}else {  
           Toast.makeText(this, "文件不存在", Toast.LENGTH_LONG).show();         
           }  
            	
    }
    public void stop(){
    	finish();
    }
    
    public void PauseOrStart(){
    	 
    	if(videoView.isPlaying()){
    		videoView.pause();
    	}
    	else{
    		videoView.start();
    		
    	}
    		
    }
    public void fastForward(){
    	int current = videoView.getCurrentPosition();
    	videoView.seekTo(current + 5000);
    }
    public void fastBackward(){
    	int current = videoView.getCurrentPosition();
    	if(current < 5000)
    		videoView.seekTo(0);
    	else
    		videoView.seekTo(current - 5000);
    	
    }
    public String getFileName(String pathandname){  
        
        int start=pathandname.lastIndexOf("/");    
        if(start!=-1){  
            return pathandname.substring(start+1);    
        }else{  
            return null;  
        }  
          
    }  
}
