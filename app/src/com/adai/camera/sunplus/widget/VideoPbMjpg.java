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
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.adai.camera.product.ISunplusCamera;
import com.adai.camera.sunplus.SDKAPI.VideoPlayback;
import com.adai.camera.sunplus.tool.ScaleTool;
import com.icatch.wificam.customer.ICatchWificamVideoPlayback;
import com.icatch.wificam.customer.exception.IchAudioStreamClosedException;
import com.icatch.wificam.customer.exception.IchBufferTooSmallException;
import com.icatch.wificam.customer.exception.IchCameraModeException;
import com.icatch.wificam.customer.exception.IchInvalidArgumentException;
import com.icatch.wificam.customer.exception.IchInvalidSessionException;
import com.icatch.wificam.customer.exception.IchPbStreamPausedException;
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
public class VideoPbMjpg extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "VideoPbMjpg";
    private int frameWidth;
    private int frameHeight;
    private byte[] pixelBuf;
    private ByteBuffer bmpBuf;
    private Rect drawFrameRect;
    private Bitmap videoFrameBitmap;
    private VideoPlayback videoPlayback = VideoPlayback.getInstance();
    private ICatchWificamVideoPlayback videoPb;
    private int myWidth;
    private int myHeight;
    public final int ADJUST_LAYOUT_VIDEOPB = 1;
    private SurfaceHolder holder;
    private VideoThread mySurfaceViewThread;
    private boolean hasSurface = false;
    private AudioThread audioThread;
    private boolean hasInit = false;
    private AudioTrack audioTrack;
    private VideoPbUpdateBarLitener videoPbUpdateBarLitener;
    private boolean videoThreadDone = false;
    private ScaleTool.ScaleType mScaleType = ScaleTool.ScaleType.AUTO;

    public VideoPbMjpg(Context context, AttributeSet attrs) {
        super(context, attrs);

        // JIRA ICOM-2029 Begin Add by b.jiang 2015-10-09;
        this.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // TODO Auto-generated method stub
                if (videoThreadDone) {
                    reDrawBitmap();
                }
            }
        });
        // JIRA ICOM-2029 End Add by b.jiang 2015-10-09;
    }

    private boolean init() {
        frameWidth = 0;
        frameHeight = 0;
        // play之后还不能立马获取到frame 宽高，sdk获取到一帧才会获取到宽高;
        // 采用延时获取宽高，三次延时可以获取到;
        // JIRA ICOM-1912 Start:Add by b.jiang 2015-08-28
        videoThreadDone = false;
        long lastTime = System.currentTimeMillis();
        while (frameWidth == 0 || frameHeight == 0) {
            frameWidth = videoPlayback.getVideoFormat().getVideoW();
            frameHeight = videoPlayback.getVideoFormat().getVideoH();
            try {
                Thread.sleep(33);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
            if (System.currentTimeMillis() - lastTime > 2000) {
                break;
            }
        }
        if (frameWidth == 0 || frameHeight == 0) {
            return false;
        }
        // JIRA ICOM-1912 End:Add by b.jiang 2015-08-28
        pixelBuf = new byte[frameWidth * frameHeight * 4];
        bmpBuf = ByteBuffer.wrap(pixelBuf);

        // Trigger onDraw with those initialize parameters
        videoFrameBitmap = Bitmap.createBitmap(frameWidth, frameHeight, Config.ARGB_8888);
        // lastVideoFrameBitmap = Bitmap.createBitmap(frameWidth, frameHeight,
        // Config.ARGB_8888);
        drawFrameRect = new Rect(0, 0, frameWidth, frameHeight);
        holder = this.getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.RGBA_8888);
        hasInit = true;
        myWidth = 0;
        myHeight = 0;
        return true;
    }

    public boolean start(ISunplusCamera mCamera, Handler handler) {

        videoPb = mCamera.getVideoPlaybackClint();
        if (!hasInit) {
            if (!init()) {
                return false;
            }
        }

        // 创建和启动图像更新线程
        if (mySurfaceViewThread == null) {
            mySurfaceViewThread = new VideoThread();
            if (hasSurface) {
                mySurfaceViewThread.start();
            }
        }
        // 启动音频线程
        if (audioThread == null) {
            audioThread = new AudioThread();
            audioThread.start();
        }
        return true;
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
        if (holder != null) {
            holder.removeCallback(this);
        }
        hasInit = false;
        return true;
    }

    private class VideoThread extends Thread {
        VideoThread() {
            super();
            videoThreadDone = false;
        }

        @Override
        public void run() {
            SurfaceHolder surfaceHolder = holder;
            ICatchFrameBuffer buffer = new ICatchFrameBuffer(frameWidth * frameHeight * 4);
            buffer.setBuffer(pixelBuf);
            boolean temp = false;
            while (!videoThreadDone) {
                // ICOM-1913 ICOM-1911 End add by b.jiang 2015-08-28
                // 当宽高改变时，重新绘制画面;
                if (myWidth != getWidth() || myHeight != getHeight()) {
                    if (getWidth() > 0 || getHeight() > 0) {
                        myWidth = getWidth();
                        myHeight = getHeight();
                        if (videoFrameBitmap != null) {
                            Canvas canvas = surfaceHolder.lockCanvas();
                            drawFrameRect = ScaleTool.getScaledPosition(frameWidth, frameHeight, getWidth(), getHeight(), mScaleType);
                            canvas.drawBitmap(videoFrameBitmap, null, drawFrameRect, null);
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        }
                    }
                }
                // ICOM-1913 ICOM-1911 End add by b.jiang 2015-08-28

                temp = false;
                try {
                    // Log.e(TAG, "[Error] -- Preview: ",
                    // "start getNextVideoFrame");
                    temp = videoPb.getNextVideoFrame(buffer);
                } catch (IchSocketException e) {
                    Log.e(TAG, "[Error] -- VideoPbMjpg: IchSocketException");
                    // need to close preview get next video frame

                    e.printStackTrace();
                    return;
                } catch (IchBufferTooSmallException e) {
                    Log.e(TAG, "[Error] -- VideoPbMjpg: IchBufferTooSmallException");

                    e.printStackTrace();
                    return;
                } catch (IchCameraModeException e) {
                    Log.e(TAG, "[Error] -- VideoPbMjpg: IchCameraModeException");

                    e.printStackTrace();
                    return;
                } catch (IchInvalidSessionException e) {
                    Log.e(TAG, "[Error] -- VideoPbMjpg: IchInvalidSessionException");

                    e.printStackTrace();
                    return;
                } catch (IchTryAgainException e) {
                    // Log.e(TAG, "[Error] -- VideoPbMjpg: ",
                    // "IchTryAgainException");

                    // Log.d("1111","IchTryAgainException!");
                    e.printStackTrace();
                } catch (IchStreamNotRunningException e) {
                    Log.e(TAG, "[Error] -- VideoPbMjpg: IchStreamNotRunningException");

                    e.printStackTrace();
                    return;
                } catch (IchInvalidArgumentException e) {
                    Log.e(TAG, "[Error] -- VideoPbMjpg: IchInvalidArgumentException");

                    e.printStackTrace();
                    return;
                } catch (IchVideoStreamClosedException e) {
                    Log.e(TAG, "[Error] -- VideoPbMjpg: IchVideoStreamClosedException");

                    e.printStackTrace();
                    return;
                } catch (IchPbStreamPausedException e) {

                    Log.e(TAG, "[Error] -- VideoPbMjpg: IchPbStreamPausedException");
                    e.printStackTrace();
                }
                // Log.e(TAG, "[Normal] -- PreviewStream: ",
                // "end getNextVideoFrame retValue =" + retValue);
                if (temp == false) {
                    continue;
                }
                if (buffer == null) {
                    Log.e(TAG, "[Error] -- VideoPbMjpg: buffer == null\n");
                    continue;
                }
                bmpBuf.rewind();
                videoFrameBitmap.copyPixelsFromBuffer(bmpBuf);
                // 锁定surface，并返回到要绘图的Canvas
                Canvas canvas = surfaceHolder.lockCanvas();
                drawFrameRect = ScaleTool.getScaledPosition(frameWidth, frameHeight, getWidth(), getHeight(), mScaleType);
                try {
                    canvas.drawBitmap(videoFrameBitmap, null, drawFrameRect, null);
                    // // 解锁Canvas，并渲染当前图像
                    surfaceHolder.unlockCanvasAndPost(canvas);

                    // JIRA ICOM-1889 Start change by b.jiang 20150821

                    if (videoPbUpdateBarLitener != null)
                        videoPbUpdateBarLitener.updateBar(buffer.getPresentationTime());

                } catch (Exception ignored) {
                    Log.e(TAG, "run: ignored=" + ignored.getMessage());
                }
                // JIRA ICOM-1889 End change by b.jiang 20150821
            }
        }

        public void requestExitAndWait() {
            // 把这个线程标记为完成，并合并到主程序线程
            videoThreadDone = true;
//            try {
//                join();
//            } catch (InterruptedException ex) {
//            }
        }
    }

    private class AudioThread extends Thread {
        private boolean done = false;

        public void run() {
            Log.e(TAG, "[Normal] -- PbVideoActivitystart AudioThread.....");
            if (videoPlayback.containsAudioStream() == false) {
                return;
            }
            ICatchAudioFormat audioFormat = videoPlayback.getAudioFormat();
            int bufferSize = AudioTrack.getMinBufferSize(audioFormat.getFrequency(), audioFormat.getNChannels() == 2 ? AudioFormat.CHANNEL_IN_STEREO
                    : AudioFormat.CHANNEL_IN_LEFT, audioFormat.getSampleBits() == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT);

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, audioFormat.getFrequency(), audioFormat.getNChannels() == 2 ? AudioFormat.CHANNEL_IN_STEREO
                    : AudioFormat.CHANNEL_IN_LEFT, audioFormat.getSampleBits() == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT,
                    bufferSize, AudioTrack.MODE_STREAM);
            Log.e(TAG, "1111start AudioTrack play");
            audioTrack.play();
            byte[] audioBuffer = new byte[1024 * 50];
            ICatchFrameBuffer icatchBuffer = new ICatchFrameBuffer();
            icatchBuffer.setBuffer(audioBuffer);
            boolean temp = false;
            Log.e(TAG, "[Normal] -- getNextAudioFramestart getNextAudioFrame");
            while (!done) {
                temp = false;
                try {
                    temp = videoPb.getNextAudioFrame(icatchBuffer);
                } catch (IchSocketException e) {
                    Log.e(TAG, "[Error] -- AudioThread: videoPb.getNextAudioFrame IchSocketException");

                    e.printStackTrace();
                    return;
                } catch (IchCameraModeException e) {
                    Log.e(TAG, "[Error] -- AudioThread: videoPb.getNextAudioFrame IchCameraModeException");

                    e.printStackTrace();
                    return;
                } catch (IchInvalidSessionException e) {
                    Log.e(TAG, "[Error] -- AudioThread: videoPb.getNextAudioFrame IchInvalidSessionException");

                    e.printStackTrace();
                    return;
                } catch (IchStreamNotRunningException e) {
                    Log.e(TAG, "[Error] -- AudioThread: videoPb.getNextAudioFrame IchStreamNotRunningException");

                    e.printStackTrace();
                    return;
                } catch (IchBufferTooSmallException e) {

                    Log.e(TAG, "[Error] -- AudioThread: videoPb.getNextAudioFrame IchBufferTooSmallException");
                    e.printStackTrace();
                    return;
                } catch (IchTryAgainException e) {

                    // Log.e(TAG, "[Error] -- AudioThread: ",
                    // "videoPb.getNextAudioFrame IchTryAgainException");
                    e.printStackTrace();
                    continue;
                } catch (IchInvalidArgumentException e) {

                    Log.e(TAG, "[Error] -- AudioThread: videoPb.getNextAudioFrame IchInvalidArgumentException");
                    e.printStackTrace();
                    return;
                } catch (IchAudioStreamClosedException | IchPbStreamPausedException e) {

                    // Log.e(TAG, "[Error] -- AudioThread: ",
                    // "videoPb.getNextAudioFrame IchAudioStreamClosedException");
                    e.printStackTrace();
                }
                if (!temp) {
                    // Log.e(TAG, "[Error] -- AudioThread: ",
                    // "failed to getNextAudioFrame");
                    continue;
                } else {
                    // Log.e(TAG, "[Normal] -- AudioThread: ",
                    // "success to getNextAudioFrame");
                }
                if (icatchBuffer == null) {
                    Log.e(TAG, "[Normal] -- AudioThreadbuffer == null");
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
//            try {
//                join();
//            } catch (InterruptedException ex) {
//            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, "testsurfaceCreated");
        hasSurface = true;
        if (mySurfaceViewThread != null) {
            if (!mySurfaceViewThread.isAlive()) {
                mySurfaceViewThread.start();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "testsurfaceDestroyed");
        hasSurface = false;
        stop();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.e(TAG, "test surfaceChanged");
        if (videoFrameBitmap != null) {
            Canvas canvas = holder.lockCanvas();
            drawFrameRect = ScaleTool.getScaledPosition(frameWidth, frameHeight, getWidth(), getHeight(), mScaleType);
            canvas.drawBitmap(videoFrameBitmap, null, drawFrameRect, null);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    public void destorySurface() {
        hasSurface = false;
    }

    public interface VideoPbUpdateBarLitener {
        void updateBar(double pts);
    }

    public void addVideoPbUpdateBarLitener(VideoPbUpdateBarLitener videoPbUpdateBarLitener) {
        // h264ImageLitener.isShown();
        this.videoPbUpdateBarLitener = videoPbUpdateBarLitener;
    }

    public void reDrawBitmap() {
        SurfaceHolder surfaceHolder = holder;
        Log.e(TAG, "test reDrawBitmap videoFrameBitmap=" + videoFrameBitmap);
        Log.e(TAG, "test reDrawBitmap Width=" + getWidth() + " Height=" + getHeight());

        int lenght = videoFrameBitmap.getByteCount();

        Log.e(TAG, "test videoFrameBitmap lenght=" + lenght + "pixelBuf.length =" + pixelBuf.length);
        if (videoFrameBitmap != null) {
            Canvas canvas = surfaceHolder.lockCanvas();
            if (canvas == null) {
                return;
            }
            drawFrameRect = ScaleTool.getScaledPosition(frameWidth, frameHeight, getWidth(), getHeight(), mScaleType);
            canvas.drawBitmap(videoFrameBitmap, null, drawFrameRect, null);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }
}
