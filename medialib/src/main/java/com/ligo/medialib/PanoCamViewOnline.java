package com.ligo.medialib;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;

import java.nio.ByteBuffer;

/**
 * @author huangxy
 * @date 2018/3/21 14:55.
 */

public class PanoCamViewOnline extends GLSurfaceView implements ScaleGestureDetector.OnScaleGestureListener,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {
    private static final String TAG = PanoCamViewOnline.class.getSimpleName();
    private MediaPlayLib mMediaPlayLib;
    public boolean isInit;
    private byte[] mYuvData, mHardYuvData;
    private com.ligo.medialib.opengl.VideoRenderYuv mVideoRender;
    private Surface decoderSurface;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    private MediaInfoCallback infoCallback;

    private Context mContext;

    private AudioTrack mAudioTrack;
    private boolean audioisinit = false;
    private String mUrl;
    private int mWidth;
    private int mHeight;
    private byte[] mSps;
    private byte[] mPPs;
    private int buf_size;
    private byte[] mSpspps;

    public interface MediaInfoCallback {
        void onInfo(States state, String info);

        void onUpdateFrame(byte[] data, int width, int height, int type);

        void onScreenShot(boolean sucess, String url);
    }

    public interface SingleTapUpListener {
        void onSingleTap();
    }

    public MediaInfoCallback getInfoCallback() {
        return infoCallback;
    }

    public void setInfoCallback(MediaInfoCallback infoCallback) {
        this.infoCallback = infoCallback;
    }

    public SingleTapUpListener mSingleTapUpListener;

    public void setSingleTapUpListener(SingleTapUpListener singleTapUpListener) {
        mSingleTapUpListener = singleTapUpListener;
    }

    public enum States {
        STATUS_STOP(0),
        STATUS_PLAY(1),
        STATUS_PAUSE(2),
        STATUS_ERROR(3);
        int value;

        public int getValue() {
            return value;
        }

        States(int value) {
            this.value = value;
        }

        public static States valueOf(int value) {
            switch (value) {
                case 0:
                    return STATUS_STOP;
                case 1:
                    return STATUS_PLAY;
                case 2:
                    return STATUS_PAUSE;
                case 3:
                    return STATUS_ERROR;
                default:
                    return STATUS_STOP;
            }
        }
    }


    public void onChangeShowType(int type) {
        mVideoRender.onChangeShowType(type);
    }

    public PanoCamViewOnline(Context context) {
        this(context, null);
    }

    public PanoCamViewOnline(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(context);
    }

    private void init(Context context) {
        if (isInit) {
            return;
        }
        mMediaPlayLib = new MediaPlayLib();
        mMediaPlayLib.init();
//        mMediaPlayLib.ChangeDecodec(0);
        mMediaPlayLib.nativeSetListener(listener);
        setEGLContextClientVersion(2);
        mVideoRender = new com.ligo.medialib.opengl.VideoRenderYuv(context);
        setRenderer(mVideoRender);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        scaleGestureDetector = new ScaleGestureDetector(context, this);
        gestureDetector = new GestureDetector(context, this);
        gestureDetector.setOnDoubleTapListener(this);
        isInit = true;
    }

    public void reInit() {
        if (isInit) {
            return;
        }
        mMediaPlayLib = new MediaPlayLib();
        mMediaPlayLib.init();
        mMediaPlayLib.nativeSetListener(listener);
        isInit = true;
    }

    private MediaCodec decoder;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void initDecoder(int width, int height, String type) {
        decoder = null;
        try {
            decoder = MediaCodec.createDecoderByType(type);
//            int[] colorFormats = decoder.getCodecInfo().getCapabilitiesForType(type).colorFormats;
//            for (int i = 0; i < colorFormats.length; i++) {
//                Log.e(TAG, "initDecoder: " + colorFormats[i] + "\n");
//            }
            //初始化编码器
            final MediaFormat mediaformat = MediaFormat.createVideoFormat(type, width, height);
//            mediaformat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 14000000);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mediaformat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            }
            if (type.equals("video/avc")) {
                mediaformat.setByteBuffer("csd-0", ByteBuffer.wrap(mSps));
                mediaformat.setByteBuffer("csd-1", ByteBuffer.wrap(mPPs));
            }
            decoder.configure(mediaformat, null, null, 0);
            decoder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isMediaInit() {
        return (mMediaPlayLib != null);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();
        mVideoRender.onScale(scaleFactor);
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.scaleGestureDetector.onTouchEvent(event);
        this.gestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        mVideoRender.onDown(event);
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        int w = this.getWidth();
        int h = this.getHeight();
        mVideoRender.onScroll(w, h, e1, e2, distanceX, distanceY);
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        if (mSingleTapUpListener != null) {
            mSingleTapUpListener.onSingleTap();
        }
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        mVideoRender.onDoubleTap(event);
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        return true;
    }

    private MediaPlayLib.MediaInfoListener listener = new MediaPlayLib.MediaInfoListener() {
        @Override
        public void onInfoUpdate(int state, String info) {
            if (infoCallback != null) {
                PanoCamViewOnline.States mystate = PanoCamViewOnline.States.valueOf(state);
                infoCallback.onInfo(mystate, info);
            }

        }

        @Override
        public void onUpdateAudioFrame(byte[] data, int size) {
//            Log.e(TAG, "onUpdateAudioFrame");
            if (audioisinit && mAudioTrack != null) {
                mAudioTrack.write(data, 0, size);
            }

        }

        @Override
        public void onUpdateFrame(int size, int width, int height, int type) {

            if (mYuvData == null) {
                return;
            }
//            int decodec = mMediaPlayLib.GetDecodec();
//            if (decodec == 0) {
//                //硬解
//                offerDecoder(mYuvData, size);
//            }
            if (mVideoRender != null) {
                mVideoRender.setYuv420pData(mYuvData, width, height);
            }
            if (infoCallback != null) {
                infoCallback.onUpdateFrame(mYuvData, width, height, type);
            }

        }

        @Override
        public void update264Frame(byte[] data, int size, int width, int height, int type) {
            String spsHead = String.format("%02x%02x%02x%02x%02x", data[0], data[1], data[2], data[3], data[4]);
            if (!hasSps) {
                if ("0000000167".equals(spsHead)) {
                    offerDecoder(data, size, width, height);
                    hasSps = true;
                    return;
                } else {
                    return;
                }
            }
            if (offerDecoder(data, size, width, height)) {
                if (mVideoRender != null) {
                    mVideoRender.setYuv420pData(mHardYuvData, alignWidth, alignHeight);
                }
                if (infoCallback != null) {
                    infoCallback.onUpdateFrame(mHardYuvData, alignWidth, alignHeight, type);
                }
            }

        }

        @Override
        public void onScreenshotData(byte[] data, int width, int height, String path) {

        }

    };
    private int alignWidth, alignHeight;
    private boolean hasSps = false;

    private boolean offerDecoder(byte[] input, int size, int videoWidth, int videoHeight) {
        if (decoder == null) {
            return false;
        }
        boolean result = false;
        try {
            int inputBufferIndex = decoder.dequeueInputBuffer(100);
            if (inputBufferIndex >= 0) {
                ByteBuffer[] inputBuffers = decoder.getInputBuffers();
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.rewind();
                inputBuffer.put(input, 0, size);
                //计算pts
                decoder.queueInputBuffer(inputBufferIndex, 0, size, 0, 0);
            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 0);
            while (outputBufferIndex >= 0) {
                ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
                Log.e(TAG, "offerDecoder: bufferInfo.size = " + bufferInfo.size);
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                if (mHardYuvData == null || mHardYuvData.length < bufferInfo.size) {
                    mHardYuvData = new byte[bufferInfo.size];
                }
                outputBuffer.get(mHardYuvData);
                decoder.releaseOutputBuffer(outputBufferIndex, true);
                outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 0);
                result = outputBufferIndex < 0;
                MediaFormat format = decoder.getOutputFormat();
                int color_format = format.getInteger(MediaFormat.KEY_COLOR_FORMAT);
                Log.e(TAG, "offerDecoder: color_format = " + color_format);
                Log.e(TAG, "offerDecoder: width = " + format.getInteger(MediaFormat.KEY_WIDTH) + ",height = " + format.getInteger(MediaFormat.KEY_HEIGHT));
                switch (color_format) {
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV411Planar:
                        break;
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV411PackedPlanar:
                        break;
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
                        break;
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                        yuv420spToYuv420P(mHardYuvData, format.getInteger(MediaFormat.KEY_WIDTH), format.getInteger(MediaFormat.KEY_HEIGHT));
                        break;
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
                        break;
                    case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                    default:
                        yuv420spToYuv420P(mHardYuvData, format.getInteger(MediaFormat.KEY_WIDTH), format.getInteger(MediaFormat.KEY_HEIGHT));
                        break;
                }

//                int width = format.getInteger(MediaFormat.KEY_WIDTH);
//                if (format.containsKey("crop-left") && format.containsKey("crop-right")) {
//                    width = format.getInteger("crop-right") + 1 - format.getInteger("crop-left");
//                }
//                int height = format.getInteger(MediaFormat.KEY_HEIGHT);
//                if (format.containsKey("crop-top") && format.containsKey("crop-bottom")) {
//                    height = format.getInteger("crop-bottom") + 1 - format.getInteger("crop-top");
//                }
//                Log.e(TAG, "offerDecoder: width=" + width + ",height = " + height);
                if (alignWidth * alignHeight == 0) {
                    //解码后数据对齐的宽高，在有些设备上会返回0
                    int keyStride = format.getInteger("stride");
                    int keyStrideHeight = format.getInteger("slice-height");
                    // 当对齐后高度返回0的时候，分两种情况，如果对齐后宽度有给值，
                    // 则只需要计算高度从16字节对齐到128字节对齐这几种情况下哪个值跟对齐后宽度相乘再乘3/2等于对齐后大小，
                    // 如果计算不出则默认等于视频宽高。
                    // 当对齐后宽度也返回0，这时候也要对宽度做对齐处理，原理同上
                    alignWidth = keyStride;
                    alignHeight = keyStrideHeight;
                    if (alignHeight == 0) {
                        if (alignWidth == 0) {
                            align:
                            for (int w = 16; w <= 128; w = w << 1) {
                                for (int h = 16; h <= w; h = h << 1) {
                                    alignWidth = ((videoWidth - 1) / w + 1) * w;
                                    alignHeight = ((videoHeight - 1) / h + 1) * h;
                                    int size0 = alignWidth * alignHeight * 3 / 2;
                                    if (size0 == bufferInfo.size) {
                                        break align;
                                    }
                                }
                            }
                        } else {
                            for (int h = 16; h <= 128; h = h << 1) {
                                alignHeight = ((videoHeight - 1) / h + 1) * h;
                                int size1 = alignWidth * alignHeight * 3 / 2;
                                if (size1 == bufferInfo.size) {
                                    break;
                                }
                            }
                        }
                        int size2 = alignWidth * alignHeight * 3 / 2;
                        if (size2 != bufferInfo.size) {
                            alignWidth = videoWidth;
                            alignHeight = videoHeight;
                        }
                    }

                    int size3 = videoWidth * videoHeight * 3 / 2;
                    if (size3 == bufferInfo.size) {
                        alignWidth = videoWidth;
                        alignHeight = videoHeight;
                    }
//                    Log.e(TAG, "offerDecoder: alignWidth = " + alignWidth + ",alignHeight = " + alignHeight);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return result;
    }

    private byte[] yuv420spToYuv420P(byte[] yuv420spData, int width, int height) {
        long startTime = System.currentTimeMillis();
//        byte[] yuv420pData = new byte[width * height * 3 / 2];
        int ySize = width * height;
        System.arraycopy(yuv420spData, 0, mHardYuvData, 0, ySize);   //拷贝 Y 分量

        for (int j = 0, i = 0; j < ySize / 2; j += 2, i++) {
            mHardYuvData[ySize + i] = yuv420spData[ySize + j];   //U 分量
            mHardYuvData[ySize * 5 / 4 + i] = yuv420spData[ySize + j + 1];   //V 分量
        }
        Log.e(TAG, "yuv420spToYuv420P: " + (System.currentTimeMillis() - startTime));
        return mHardYuvData;
    }

    public int getCurrent() {
        if (mMediaPlayLib != null) {
            return mMediaPlayLib.nativeCurrent();
        }
        return 0;
    }

    public int getDuration() {
        if (mMediaPlayLib != null) {
            return mMediaPlayLib.nativeDuration() * 1000;
        }
        return 0;
    }

    public void seek(int millisecond) {
        if (mMediaPlayLib != null) {
            mMediaPlayLib.nativeSeek(millisecond / 1000);
        }
    }

    public void setUrl(String url) {
        mUrl = url;
        mMediaPlayLib.setUrl(mUrl);
    }

    public void changeDecodec(int type) {
        if (mMediaPlayLib != null) {
            type = 1;
            mMediaPlayLib.ChangeDecodec(type);
        }
    }

    public int getMediaWidth() {
        return mWidth;
    }

    public int getMediaHeight() {
        return mHeight;
    }

    public synchronized void startPlay(int type) {
        if (mMediaPlayLib != null) {
            hasSps = false;
            alignHeight = 0;
            alignWidth = 0;
//            mMediaPlayLib.nativeStopPlay();
            byte[] medieInfo = mMediaPlayLib.nativeStartPlay(type);
            if (medieInfo == null) {
                if (infoCallback != null) {
                    infoCallback.onInfo(States.STATUS_ERROR, "开启流失败");
                }
                return;
            }
            mWidth = Packet.byteArrayToInt_Little(medieInfo, 0);
            mHeight = Packet.byteArrayToInt_Little(medieInfo, 4);
//            Log.e(TAG, "startPlay: avCodecId = " + avCodecId);
            String frameType = "video/avc";
            int avCodecId = Packet.byteArrayToInt_Little(medieInfo, 20);
            if (avCodecId == 8) {
                frameType = "video/mjpeg";
            }
            int spsppsLenght = Packet.byteArrayToInt_Little(medieInfo, 12);
            if (spsppsLenght != 0) {
                mSpspps = new byte[spsppsLenght];
                System.arraycopy(medieInfo, 56, mSpspps, 0, spsppsLenght);
                int spsLength = Packet.byteArrayToInt_Little(medieInfo, 48);
                int ppsLength = Packet.byteArrayToInt_Little(medieInfo, 52);
                mSps = new byte[spsLength + 4];
                System.arraycopy(mSpspps, 0, mSps, 0, mSps.length);
                mPPs = new byte[ppsLength + 4];
                System.arraycopy(mSpspps, mSps.length, mPPs, 0, mPPs.length);
                initDecoder(mWidth, mHeight, frameType);
            }
            mYuvData = new byte[mWidth * mHeight * 3 / 2];
            mMediaPlayLib.nativeSetFrameBuffer(mYuvData);

//            mVideoRender.setFrameInfo(mWidth, mHeight);
            mVideoRender.initVertexData(mWidth, mHeight);
            audioisinit = false;
            audioisinit = initAudio(medieInfo);
        }
    }


    public void stopPlay() {
        if (mMediaPlayLib != null) {
            mMediaPlayLib.nativeStopPlay();
        }
    }

    public void pause() {
        if (mMediaPlayLib != null) {
            mMediaPlayLib.nativePause();
        }
    }

    public void resume() {
        if (mMediaPlayLib != null) {
            mMediaPlayLib.nativeResume();
        }
    }

    public interface OnChangeListener {

        void onLoadComplete();

        void onError(String errorMessage);

        void onEnd();
    }

    private OnChangeListener mOnChangeListener;

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        mOnChangeListener = onChangeListener;
    }

    public boolean isPlaying() {
        return mMediaPlayLib != null && mMediaPlayLib.getCurrentState() == States.STATUS_PLAY.getValue();
    }

    public boolean isPause() {
        return mMediaPlayLib != null && mMediaPlayLib.getCurrentState() == States.STATUS_PAUSE.getValue();
    }

    public boolean isStop() {
        return mMediaPlayLib != null && mMediaPlayLib.getCurrentState() == States.STATUS_STOP.getValue();
    }

    public void changePlayer() {
        isInit = false;
        if (mMediaPlayLib != null) {
            mMediaPlayLib.release();
            mMediaPlayLib = null;
        }
        if (mYuvData != null) {
            mYuvData = null;
        }
        if (mAudioTrack != null && audioisinit) {
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }

    public void release() {
        isInit = false;
        try {
            if (decoder != null) {
                decoder.stop();
                decoder.release();
                decoder = null;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        if (mMediaPlayLib != null) {
            mMediaPlayLib.release();
            mMediaPlayLib = null;
        }
        if (decoderSurface != null) {
            decoderSurface.release();
            decoderSurface = null;
        }
        if (mVideoRender != null) {
            mVideoRender = null;
        }
        if (mYuvData != null) {
            mYuvData = null;
        }
        if (mHardYuvData != null) {
            mHardYuvData = null;
        }
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }

    public boolean initAudio(byte[] mediaInfo) {
        if (mediaInfo == null) {
//            Log.e(TAG, "mediaInfo = null");
            return false;
        }
//        Log.e(TAG, "initAudio mediaInfo.length = " + mediaInfo.length);

        int audio_sample_rate = (int) ((mediaInfo[28 + 0] & 0xFF)
                | ((mediaInfo[28 + 1] & 0xFF) << 8)
                | ((mediaInfo[28 + 2] & 0xFF) << 16)
                | ((mediaInfo[28 + 3] & 0xFF) << 24));
//        Log.e(TAG, "audio_sample_rate = " + audio_sample_rate);
        int audio_channels = (int) ((mediaInfo[36 + 0] & 0xFF)
                | ((mediaInfo[36 + 1] & 0xFF) << 8)
                | ((mediaInfo[36 + 2] & 0xFF) << 16)
                | ((mediaInfo[36 + 3] & 0xFF) << 24));
        //  Log.e(TAG, "audio_sample_rate = " + audio_sample_rate);
//        Log.e(TAG, "audio_channels = " + audio_channels);

        int new_audio_channels = 0;
        if (audio_channels == 1) {
            new_audio_channels = 2;
        } else if (audio_channels == 2) {
            new_audio_channels = 3;
        }
//        Log.e(TAG, "new_audio_channels = " + new_audio_channels);

        try {
            this.buf_size = AudioTrack.getMinBufferSize(audio_sample_rate, new_audio_channels, AudioFormat.ENCODING_PCM_16BIT);
            this.mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, audio_sample_rate, new_audio_channels,// 设置输出声道为双声道立体声
                    AudioFormat.ENCODING_PCM_16BIT, this.buf_size * 4, AudioTrack.MODE_STREAM);
            mAudioTrack.play();
        } catch (Exception excetion) {
            excetion.printStackTrace();
            return false;
        }
        return true;
    }


}

