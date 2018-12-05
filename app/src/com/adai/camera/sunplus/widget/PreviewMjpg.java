/**
 * Added by zhangyanhu C01012,2014-7-15
 */
package com.adai.camera.sunplus.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.adai.camera.product.ISunplusCamera;
import com.adai.camera.sunplus.SDKAPI.PreviewStream;
import com.adai.camera.sunplus.data.GlobalInfo;
import com.adai.camera.sunplus.tool.ScaleTool;
import com.icatch.wificam.customer.ICatchWificamPreview;
import com.icatch.wificam.customer.exception.IchAudioStreamClosedException;
import com.icatch.wificam.customer.exception.IchBufferTooSmallException;
import com.icatch.wificam.customer.exception.IchCameraModeException;
import com.icatch.wificam.customer.exception.IchInvalidArgumentException;
import com.icatch.wificam.customer.exception.IchInvalidSessionException;
import com.icatch.wificam.customer.exception.IchSocketException;
import com.icatch.wificam.customer.exception.IchStreamNotRunningException;
import com.icatch.wificam.customer.exception.IchTryAgainException;
import com.icatch.wificam.customer.exception.IchVideoStreamClosedException;
import com.icatch.wificam.customer.type.ICatchAudioFormat;
import com.icatch.wificam.customer.type.ICatchFrameBuffer;

import java.nio.ByteBuffer;

/**
 * Added by zhangyanhu C01012,2014-7-15
 */
public class PreviewMjpg extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = PreviewMjpg.class.getSimpleName();
    private int frameWidth;
    private int frameHeight;
    private byte[] pixelBuf;
    private ByteBuffer bmpBuf;
    private Rect drawFrameRect;
    private Bitmap videoFrameBitmap;
    private AudioTrack audioTrack;
    private PreviewStream previewStream = PreviewStream.getInstance();
    private SurfaceHolder holder;
    private VideoThread mySurfaceViewThread;
    private boolean hasSurface = false;
    private AudioThread audioThread;
    private boolean hasInit = false;
    private ICatchWificamPreview previewStreamControl;
    public ICatchWificamPreview icatchMedia;
    private ISunplusCamera mCamera;
    private ScaleTool.ScaleType mScaleType = ScaleTool.ScaleType.AUTO;

    public PreviewMjpg(Context context) {
        this(context, null);
    }

    public PreviewMjpg(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.e(TAG, "call PreviewMjpg()");
        //JIRA ICOM-2098 Begin Add by b.jiang 2015-10-19
        holder = this.getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.RGBA_8888);
        //JIRA ICOM-2098 End Add by b.jiang 2015-10-19
    }


    public void setScaleType(ScaleTool.ScaleType scaleType) {
        mScaleType = scaleType;
    }

    private void init() {
        frameWidth = previewStream.getVideoWidth(mCamera.getPreviewStreamClient());
        frameHeight = previewStream.getVideoHeigth(mCamera.getPreviewStreamClient());
        // The 'bmpBuf' container is used to get data from server
        pixelBuf = new byte[frameWidth * frameHeight * 4];
        bmpBuf = ByteBuffer.wrap(pixelBuf);

        // Trigger onDraw with those initialize parameters
        if (videoFrameBitmap == null && frameHeight * frameWidth != 0) {
            videoFrameBitmap = Bitmap.createBitmap(frameWidth, frameHeight, Config.ARGB_8888);
        }
        drawFrameRect = new Rect(0, 0, frameWidth, frameHeight);
        // 创建一个新的SurfaceHolder， 并分配这个类作为它的回调(callback)
        //JIRA ICOM-2098 Begin Delete by b.jiang 2015-10-19
//		holder = this.getHolder();
//		holder.addCallback(this);
        //JIRA ICOM-2098 End Delete by b.jiang 2015-10-19


        hasInit = true;
        previewStreamControl = mCamera.getPreviewStreamClient();
    }

    public void start(ISunplusCamera mCamera) {
        this.mCamera = mCamera;
        if (!hasInit) {
            init();
        }
        // 创建和启动图像更新线程
        if (mySurfaceViewThread == null) {

            mySurfaceViewThread = new VideoThread();
            Log.e(TAG, "PreviewMjpg: hasSurface =" + hasSurface);
            if (hasSurface) {
                mySurfaceViewThread.start();
            }
        }
        // 启动音频线程
        // JIRA ICOM-1844 Start Add by b.jiang 2015-08-14
        if (previewStream.supportAudio(mCamera.getPreviewStreamClient()) && (!GlobalInfo.forbidAudioOutput)) {
            if (audioThread == null) {
                audioThread = new AudioThread();
                audioThread.start();
            }
        }
        // JIRA ICOM-1844 End Add by b.jiang 2015-08-14
    }

    public boolean stop() {
        // 杀死图像更新线程
        if (mySurfaceViewThread != null) {
            mySurfaceViewThread.requestExitAndWait();
            mySurfaceViewThread = null;
        }
        // 杀死音频线程
        if (audioThread != null) {
            audioThread.requestExitAndWait();
            audioThread = null;
        }
//		if (holder != null) {
//			holder.removeCallback(this);
//		}
        if (videoFrameBitmap != null) {
            videoFrameBitmap.recycle();
            videoFrameBitmap = null;
        }

        hasInit = false;
        return true;
    }

    private class VideoThread extends Thread {
        private boolean done;

        VideoThread() {
            super();
            done = false;
        }

        @Override
        public void run() {
            SurfaceHolder surfaceHolder = holder;
            ICatchFrameBuffer buffer = new ICatchFrameBuffer(frameWidth * frameHeight * 4);
            buffer.setBuffer(pixelBuf);
            boolean temp = false;
//			boolean isSaveBitmapToDb = false;
            Log.e(TAG, "run: [Normal] -- PreviewMjpg: start video thread");
            while (!done) {
                temp = false;
                try {
                    // WriteLogToDevice.writeLog("[Normal] -- PreviewMjpg: ",
                    // "start getNextVideoFrame");
                    temp = previewStreamControl.getNextVideoFrame(buffer);
//					 WriteLogToDevice.writeLog("[Normal] -- PreviewMjpg: ","end getNextVideoFrame temp = " + temp);
                } catch (IchSocketException | IchBufferTooSmallException | IchInvalidSessionException | IchCameraModeException | IchStreamNotRunningException | IchVideoStreamClosedException | IchInvalidArgumentException e) {
                    e.printStackTrace();
                    return;
                } catch (IchTryAgainException e) {
                    e.printStackTrace();
                }
                if (!temp) {
                    continue;
                }
                if (buffer.getFrameSize() == 0) {
                    continue;
                }

                bmpBuf.rewind();
                videoFrameBitmap.copyPixelsFromBuffer(bmpBuf);
//				if (!isSaveBitmapToDb) {
//					if(videoFrameBitmap != null){
//						CameraSlotSQLite.getInstance().updateImage(videoFrameBitmap);
//						isSaveBitmapToDb = true;
//					}
//				}

                // 锁定surface，并返回到要绘图的Canvas
                Canvas canvas = surfaceHolder.lockCanvas();
                if (canvas == null) {
                    continue;
                }
                // // 待实现：在Canvas上绘图
                int w = getWidth();
                int h = getHeight();
                drawFrameRect = ScaleTool.getScaledPosition(frameWidth, frameHeight, w, h, mScaleType);
                canvas.drawBitmap(videoFrameBitmap, null, drawFrameRect, null);
                // // 解锁Canvas，并渲染当前图像
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
//			if (videoFrameBitmap != null) {
//				CameraSlotSQLite.getInstance().updateImage(videoFrameBitmap);
//			}
            Log.e(TAG, "run: [Normal] -- PreviewMjpg: stop video thread");
        }

        public void requestExitAndWait() {
            // 把这个线程标记为完成，并合并到主程序线程
            done = true;
            try {
                join();
            } catch (InterruptedException ex) {
            }
        }
    }

    private class AudioThread extends Thread {
        private boolean done = false;

        public void run() {
            Log.e(TAG, "[Normal] -- PreviewMjpg: Run AudioThread");
            ICatchAudioFormat audioFormat = previewStream.getAudioFormat(previewStreamControl);
            int bufferSize = AudioTrack.getMinBufferSize(audioFormat.getFrequency(), audioFormat.getNChannels() == 2 ? AudioFormat.CHANNEL_IN_STEREO
                    : AudioFormat.CHANNEL_IN_LEFT, audioFormat.getSampleBits() == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT);

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, audioFormat.getFrequency(), audioFormat.getNChannels() == 2 ? AudioFormat.CHANNEL_IN_STEREO
                    : AudioFormat.CHANNEL_IN_LEFT, audioFormat.getSampleBits() == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT,
                    bufferSize, AudioTrack.MODE_STREAM);

            audioTrack.play();
            Log.e(TAG, "[Normal] -- PreviewMjpg: Run AudioThread 3");
            byte[] audioBuffer = new byte[1024 * 50];
            ICatchFrameBuffer icatchBuffer = new ICatchFrameBuffer(1024 * 50);
            icatchBuffer.setBuffer(audioBuffer);
            boolean temp = false;
            while (!done) {
                temp = false;
                try {
                    temp = previewStreamControl.getNextAudioFrame(icatchBuffer);
                } catch (IchSocketException e) {
                    Log.e(TAG, "[Error] -- PreviewMjpg:  getNextAudioFrame IchSocketException");
                    e.printStackTrace();
                    return;
                } catch (IchBufferTooSmallException e) {
                    Log.e(TAG, "[Error] -- PreviewMjpg: getNextAudioFrame IchBufferTooSmallException");
                    e.printStackTrace();
                    return;
                } catch (IchCameraModeException e) {
                    Log.e(TAG, "[Error] -- PreviewMjpg: getNextAudioFrame IchCameraModeException");
                    e.printStackTrace();
                    return;
                } catch (IchInvalidSessionException e) {
                    Log.e(TAG, "[Error] -- PreviewMjpg: getNextAudioFrame IchInvalidSessionException");
                    e.printStackTrace();
                    return;
                } catch (IchTryAgainException e) {
                    Log.e(TAG, "[Error] -- PreviewMjpg: getNextAudioFrame IchTryAgainException");
                    e.printStackTrace();
                } catch (IchStreamNotRunningException e) {
                    Log.e(TAG, "[Error] -- PreviewMjpg: getNextAudioFrame IchStreamNotRunningException");
                    e.printStackTrace();
                    return;
                } catch (IchInvalidArgumentException e) {
                    Log.e(TAG, "[Error] -- PreviewMjpg: getNextAudioFrame IchInvalidArgumentException");
                    e.printStackTrace();
                    return;
                } catch (IchAudioStreamClosedException e) {
                    Log.e(TAG, "[Error] -- PreviewMjpg: getNextAudioFrame IchAudioStreamClosedException");
                    e.printStackTrace();

                }
                if (!temp) {
                    continue;
                }

                audioTrack.write(icatchBuffer.getBuffer(), 0, icatchBuffer.getFrameSize());
            }

            audioTrack.stop();
            audioTrack.release();
            Log.e(TAG, "[Normal] -- PreviewMjpg: stop audio thread");

        }

        public void requestExitAndWait() {
            // 把这个线程标记为完成，并合并到主程序线程
            done = true;
            try {
                join();
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, "[Normal] -- PreviewMjpg: surfaceCreated hasSurface =" + hasSurface);
        Log.e(TAG, "PreviewMjpg: surfaceCreated hasSurface =" + hasSurface);
        hasSurface = true;
        if (mySurfaceViewThread != null) {
            if (!mySurfaceViewThread.isAlive()) {
                mySurfaceViewThread.start();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "[Normal] -- PreviewMjpg:  surfaceDestroyed hasSurface =" + hasSurface);
        Log.d("1111", "PreviewMjpg: surfacesurfaceDestroyedCreated hasSurface =" + hasSurface);
        hasSurface = false;
        stop();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    }

}
