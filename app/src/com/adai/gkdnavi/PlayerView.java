package com.adai.gkdnavi;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.adai.gkdnavi.utils.LogUtils;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.AndroidUtil;
import org.videolan.vlc.media.MediaWrapper;
import org.videolan.vlc.media.MediaWrapperList;
import org.videolan.vlc.util.VLCInstance;
import org.videolan.vlc.util.VLCOptions;
import org.videolan.vlc.util.WeakHandler;

public class PlayerView extends FrameLayout implements IVideoPlayer, IVLCVout.Callback, LibVLC.HardwareAccelerationError {

    private static final String TAG = "PlayerView";

    public interface OnChangeListener {

        void onBufferChanged(float buffer);

        void onLoadComplete();

        void onError();

        void onPlayerError();

        void onStartPlay();

        void onPlayNothing();

        void onEnd();

    }

    private int lastSurfaceWidth = 0;
    private int lastSurfaceHeight = 0;
    private OnSurfaceSizeChangeListener mOnSurfaceSizeChangeListener;

    public interface OnSurfaceSizeChangeListener {
        void onSizeChanged(int width, int height);
    }

    public void setSurfaceSizeChangeListener(OnSurfaceSizeChangeListener surfaceSizeChangeListener) {
        mOnSurfaceSizeChangeListener = surfaceSizeChangeListener;
    }

    public static final int SURFACE_BEST_FIT = 0;
    public static final int SURFACE_FIT_HORIZONTAL = 1;
    public static final int SURFACE_FIT_VERTICAL = 2;
    public static final int SURFACE_FILL = 3;
    public static final int SURFACE_16_9 = 4;
    public static final int SURFACE_4_3 = 5;
    public static final int SURFACE_ORIGINAL = 6;
    private int mCurrentSize = SURFACE_BEST_FIT;

    public void setCurrentSize(int size) {
        mCurrentSize = size;
    }

    private LibVLC mLibVLC;

    private MediaWrapperList mMediaList = new MediaWrapperList();
    private MediaPlayer mMediaPlayer;
    private boolean mParsed = false;
    private boolean mSeekable = false;
    private boolean mPausable = false;
    private boolean mIsAudioTrack = false;
    private boolean mHasHdmiAudio = false;

    // Whether fallback from HW acceleration to SW decoding was done.
    private boolean mDisabledHardwareAcceleration = false;

    public boolean ismDisabledHardwareAcceleration() {
        return mDisabledHardwareAcceleration;
    }

    public void setmDisabledHardwareAcceleration(boolean mDisabledHardwareAcceleration) {
        this.mDisabledHardwareAcceleration = mDisabledHardwareAcceleration;
    }

    private int mPreviousHardwareAccelerationMode;

    private SurfaceView mSurface;
    //private SurfaceView mSubtitlesSurface;

    private SurfaceHolder mSurfaceHolder;
    //private SurfaceHolder mSubtitlesSurfaceHolder;

    private FrameLayout mSurfaceFrame;

    // size of the video
    private int mVideoHeight;
    private int mVideoWidth;
    private int mVideoVisibleHeight;
    private int mVideoVisibleWidth;
    private int mSarNum;
    private int mSarDen;

    private Handler mHandler;
    private OnChangeListener mOnChangeListener;
    private boolean mCanSeek = true;
//	private boolean isPause=false;

    private String url;

//	public boolean isPause() {
//		return !mMediaPlayer.isPlaying();
//	}

//	public void setPause(boolean isPause) {
//		this.isPause = isPause;
//	}

    public PlayerView(Context context) {
        super(context);
        init();
    }

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void initPlayer(String url) {
//		try {
//			mLibVLC.updateSize(getContext().getApplicationContext());
//		} catch (LibVlcException e) {
//			throw new RuntimeException("PlayerView Init Failed");
//		}
//		mLibVLC.getMediaList().clear();
//		mLibVLC.getMediaList().add(url);
        clearMedia();
        this.url = url;
        MediaWrapper media = new MediaWrapper(AndroidUtil.LocationToUri(url));
        append(media);
    }


    public void append(MediaWrapper media) {
        mMediaList.add(media);
    }

    private void clearMedia() {
        mMediaList.clear();
    }

    public SurfaceView getSurface() {
        return mSurface;
    }

    private void init() {
//		try {
//			mLibVLC = LibVLC.getExistingInstance();
//			if (mLibVLC == null) {
//				mLibVLC = LibVLC.getInstance();
//			}
//		} catch (LibVlcException e) {
//			throw new RuntimeException("PlayerView Init Failed");
//		}


        LayoutInflater.from(getContext()).inflate(R.layout.view_player, this);
        mHandler = new Handler();

        //video view
        mSurface = (SurfaceView) findViewById(R.id.player_surface);
        mSurfaceHolder = mSurface.getHolder();
//		mSurfaceHolder.addCallback(mSurfaceCallback);
//		mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);

        //Subtitles view
        //mSubtitlesSurface = (SurfaceView) findViewById(R.id.subtitles_surface);
        //mSubtitlesSurfaceHolder = mSubtitlesSurface.getHolder();
        //mSubtitlesSurface.setZOrderMediaOverlay(true);
        //mSubtitlesSurfaceHolder.setFormat(PixelFormat.RGBA_8888);
        //mSubtitlesSurfaceHolder.addCallback(mSubtitlesSurfaceCallback);

        mSurfaceFrame = (FrameLayout) findViewById(R.id.player_surface_frame);

        mMediaPlayer = newMediaPlayer();
    }
//	private final SurfaceHolder.Callback mSurfaceCallback = new Callback() {
//		@Override
//		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//			if (format == PixelFormat.RGBX_8888)
//				Log.d(TAG, "Pixel format is RGBX_8888");
//			else if (format == PixelFormat.RGB_565)
//				Log.d(TAG, "Pixel format is RGB_565");
//			else if (format == ImageFormat.YV12)
//				Log.d(TAG, "Pixel format is YV12");
//			else
//				Log.d(TAG, "Pixel format is other/unknown");
//			if (mLibVLC != null) {
//				mLibVLC.attachSurface(holder.getSurface(), PlayerView.this);
//			}
//		}
//
//		@Override
//		public void surfaceCreated(SurfaceHolder holder) {
//		}
//
//		@Override
//		public void surfaceDestroyed(SurfaceHolder holder) {
//			if (mLibVLC != null)
//				mLibVLC.detachSurface();
//		}
//	};

//	@SuppressWarnings("unused")
//	private final SurfaceHolder.Callback mSubtitlesSurfaceCallback = new Callback() {
//		@Override
//		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//			if (mLibVLC != null)
//				mLibVLC.attachSubtitlesSurface(holder.getSurface());
//		}
//
//		@Override
//		public void surfaceCreated(SurfaceHolder holder) {
//		}
//
//		@Override
//		public void surfaceDestroyed(SurfaceHolder holder) {
//			if (mLibVLC != null)
//				mLibVLC.detachSubtitlesSurface();
//		}
//	};

    @Override
    public void setSurfaceSize(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den) {
        if (width * height == 0) {
            return;
        }
        LogUtils.e("width = " + width + ",height = " + height + ",visible_width =" + visible_width + ",visible_height=" + visible_height);
        // store video size
        mVideoHeight = height;
        mVideoWidth = width;
        mVideoVisibleHeight = visible_height;
        mVideoVisibleWidth = visible_width;
        mSarNum = sar_num;
        mSarDen = sar_den;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                changeSurfaceSize();
            }
        });
    }

    public void setOnChangeListener(OnChangeListener listener) {
        mOnChangeListener = listener;
    }

    public void changeSurfaceSize() {
        int sw;
        int sh;
        // get screen size
        sw = getWidth();
        sh = getHeight();
        LogUtils.e("sw =  " + sw + ",sh = " + sh);
        double dw = sw, dh = sh;
        if (mCurrentSize != SURFACE_FILL) {
            boolean isPortrait;
            isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
            if (sw > sh && isPortrait || sw < sh && !isPortrait) {
                dw = sh;
                dh = sw;
            }
        }
        // sanity check
        if (dw * dh == 0 || mVideoWidth * mVideoHeight == 0) {
            Log.e(TAG, "Invalid surface size");
            return;
        }
        // compute the aspect ratio
        double ar, vw;
        if (mSarDen == mSarNum) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth;
            ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * (double) mSarNum / mSarDen;
            ar = vw / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double dar = dw / dh;//宽高比

        switch (mCurrentSize) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_FIT_HORIZONTAL:
                dh = dw / ar;
                break;
            case SURFACE_FIT_VERTICAL:
                dw = dh * ar;
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoVisibleHeight;
                dw = vw;
                break;
        }

        SurfaceView surface;
        //SurfaceView subtitlesSurface;
        SurfaceHolder surfaceHolder;
        //SurfaceHolder subtitlesSurfaceHolder;
        FrameLayout surfaceFrame;

        surface = mSurface;
        //subtitlesSurface = mSubtitlesSurface;
        surfaceHolder = mSurfaceHolder;
        //subtitlesSurfaceHolder = mSubtitlesSurfaceHolder;
        surfaceFrame = mSurfaceFrame;

        // force surface buffer size
        surfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
        //subtitlesSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);

        // set display size
        android.view.ViewGroup.LayoutParams lp = surface.getLayoutParams();
        lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
        surface.setLayoutParams(lp);
        //subtitlesSurface.setLayoutParams(lp);
        if (mOnSurfaceSizeChangeListener != null) {
            if (lp.width != lastSurfaceWidth || lp.height != lastSurfaceHeight) {
                lastSurfaceHeight = lp.height;
                lastSurfaceWidth = lp.width;
                mOnSurfaceSizeChangeListener.onSizeChanged(lastSurfaceWidth, lastSurfaceHeight);
            }
        }
        // set frame size (crop if necessary)
        lp = surfaceFrame.getLayoutParams();
        LogUtils.e("dw = " + dw + ",dh = " + dh);
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);
        surfaceFrame.setLayoutParams(lp);

        surface.invalidate();
        //subtitlesSurface.invalidate();

    }

    @Override
    public void eventHardwareAccelerationError() {
//		EventHandler em = EventHandler.getInstance();
//		em.callback(EventHandler.HardwareAccelerationError, new Bundle());
//		mMediaPlayer.stop();
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putInt("event", EventHandler.HardwareAccelerationError);
        msg.setData(data);
        eventHandler.sendMessage(msg);
    }

    private void handleHardwareAccelerationError() {
//		mLibVLC.stop();
        mMediaPlayer.stop();
        mDisabledHardwareAcceleration = true;
//		mPreviousHardwareAccelerationMode = mLibVLC.getHardwareAcceleration();
//		mLibVLC.setHardwareAcceleration(LibVLC.HW_ACCELERATION_DISABLED);
        start();
    }

    public void releaseMediaPlayer() {
        stop();
        mMediaPlayer.release();
    }

    public void restartMediaPlayer() {
        stop();
        mMediaPlayer.release();
        mMediaPlayer = newMediaPlayer();
        /* TODO RESUME */
    }

    public void start() {
//		mLibVLC.eventVideoPlayerActivityCreated(true);
//		EventHandler.getInstance().addHandler(eventHandler);
//		mLibVLC.playIndex(0);
//		mSurface.setKeepScreenOn(true);
//
//		/*
//		 * WARNING: hack to avoid a crash in mediacodec on KitKat. Disable
//		 * hardware acceleration if the media has a ts extension.
//		 */
//		if (LibVlcUtil.isKitKatOrLater()) {
//			String locationLC = url.toLowerCase(Locale.ENGLISH);
//			if (locationLC.endsWith(".ts") || locationLC.endsWith(".tts") || locationLC.endsWith(".m2t") || locationLC.endsWith(".mts")
//					|| locationLC.endsWith(".m2ts")) {
//				mDisabledHardwareAcceleration = true;
//				mPreviousHardwareAccelerationMode = mLibVLC.getHardwareAcceleration();
//				mLibVLC.setHardwareAcceleration(LibVLC.HW_ACCELERATION_DISABLED);
//			}
//		}
//		
//		//关闭硬件加速
//		mDisabledHardwareAcceleration = true;
//		mPreviousHardwareAccelerationMode = mLibVLC.getHardwareAcceleration();
//		mLibVLC.setHardwareAcceleration(LibVLC.HW_ACCELERATION_DISABLED);
//		if(isPause)return;
        playIndex(0, 1);
    }

    ;

    public boolean isVideoPlaying() {
        return mMediaPlayer.getVLCVout().areViewsAttached();
    }

    /**
     * Play a media from the media list (playlist)
     *
     * @param index The index of the media
     * @param flags LibVLC.MEDIA_* flags
     */
    public void playIndex(int index, int flags) {
        if (mMediaList.size() == 0) {
            Log.w(TAG, "Warning: empty media list, nothing to play !");
            return;
        }

        String mrl = mMediaList.getMRL(index);
        if (mrl == null)
            return;
        final MediaWrapper mw = mMediaList.getMedia(index);
        if (mw == null)
            return;
        if (mw.getType() == MediaWrapper.TYPE_VIDEO && isVideoPlaying())
            mw.addFlags(MediaWrapper.MEDIA_VIDEO);

        /* Pausable and seekable are true by default */
        mParsed = false;
        mPausable = mSeekable = true;
        final Media media = new Media(VLCInstance.get(), mw.getUri());
//        if(mDisabledHardwareAcceleration){
//        	//关闭硬件加速
//        	mw.addFlags(MediaWrapper.MEDIA_NO_HWACCEL);
//        }
        mw.addFlags(MediaWrapper.MEDIA_NO_HWACCEL);
        VLCOptions.setMediaOptions(media, getContext(), flags | mw.getFlags());
        media.setEventListener(mMediaListener);
        mMediaPlayer.setMedia(media);
        media.release();
//        if (mw .getType() != MediaWrapper.TYPE_VIDEO || mw.hasFlag(MediaWrapper.MEDIA_FORCE_AUDIO) || isVideoPlaying()) {
        mMediaPlayer.setEqualizer(VLCOptions.getEqualizer(getContext()));
        mMediaPlayer.setVideoTitleDisplay(MediaPlayer.Position.Disable, 0);
        mMediaPlayer.setEventListener(mMediaPlayerListener);
        mMediaPlayer.play();

//        } 
    }

    private final Media.EventListener mMediaListener = new Media.EventListener() {
        @Override
        public void onEvent(Media.Event event) {
            switch (event.type) {
                case Media.Event.MetaChanged:
                    /* Update Meta if file is already parsed */
                    if (!mParsed)
                        break;
                    Log.i(TAG, "Media.Event.MetaChanged: " + event.getMetaId());

                case Media.Event.ParsedChanged:
                    Log.i(TAG, "Media.Event.ParsedChanged");
                    final MediaWrapper mw = mMediaList.getMedia(0);
                    if (mw != null)
                        mw.updateMeta(mMediaPlayer);
                    mParsed = true;
                    break;

            }
        }
    };

    private final MediaPlayer.EventListener mMediaPlayerListener = new MediaPlayer.EventListener() {

        @Override
        public void onEvent(MediaPlayer.Event event) {
            switch (event.type) {
                case MediaPlayer.Event.Playing:

                    Log.i(TAG, "MediaPlayer.Event.Playing");

                    if (mOnChangeListener != null) {
                        mOnChangeListener.onLoadComplete();
                    }

                    break;
                case MediaPlayer.Event.Paused:
                    Log.i(TAG, "MediaPlayer.Event.Paused");

                    break;
                case MediaPlayer.Event.Stopped:
                    Log.i(TAG, "MediaPlayer.Event.Stopped");

                    break;
                case MediaPlayer.Event.EndReached:
                    Log.i(TAG, "MediaPlayer.Event.EndReached");
                    if (mOnChangeListener != null) {
                        mOnChangeListener.onEnd();
                    }
                    break;
                case MediaPlayer.Event.EncounteredError:
                    if (mOnChangeListener != null) {
                        //playerView.mOnChangeListener.onError();
                        mOnChangeListener.onPlayerError();
                    }
                    break;
                case MediaPlayer.Event.TimeChanged:
                    break;
                case MediaPlayer.Event.PositionChanged:
                    if (mCanSeek) {
                        mCanSeek = true;
                    }
                    break;
                case MediaPlayer.Event.Vout:
                    int count = event.getVoutCount();
                    System.out.println("vout......_________________________________________");
                    break;
                case MediaPlayer.Event.ESAdded:

                    break;
                case MediaPlayer.Event.ESDeleted:
                    break;
                case MediaPlayer.Event.PausableChanged:
                    mPausable = event.getPausable();
                    break;
                case MediaPlayer.Event.SeekableChanged:
                    mSeekable = event.getSeekable();
                    break;
                case MediaPlayer.Event.Opening:
                    if (mOnChangeListener != null) {
                        mOnChangeListener.onStartPlay();
                    }
                    break;
            }

        }
    };

    public void play() {
//		mLibVLC.play();
        mSurface.setKeepScreenOn(false);
        mMediaPlayer.play();
    }

    public void pause() {
//		mLibVLC.pause();
        mSurface.setKeepScreenOn(false);
        mMediaPlayer.pause();
    }

    public void stop() {
//		mLibVLC.stop();
        mSurface.setKeepScreenOn(false);
        EventHandler em = EventHandler.getInstance();
//		em.removeHandler(eventHandler);
        // MediaCodec opaque direct rendering should not be used anymore since there is no surface to attach.
//		mLibVLC.eventVideoPlayerActivityCreated(false);
//		if (mDisabledHardwareAcceleration) {
//			mLibVLC.setHardwareAcceleration(mPreviousHardwareAccelerationMode);
//		}
//		mLibVLC.destroy();


        if (mMediaPlayer == null)
            return;
        final Media media = mMediaPlayer.getMedia();
        if (media != null) {
            media.setEventListener(null);
            mMediaPlayer.setEventListener(null);

//	            if(mMediaPlayer.isPlaying())
            mMediaPlayer.stop();
            mMediaPlayer.setMedia(null);
            media.release();
        }

    }

    public long getTime() {
//		return mLibVLC.getTime();
        return mMediaPlayer.getTime();
    }

    public long getLength() {
        return mMediaPlayer.getLength();
    }

    public void setTime(long time) {
        mMediaPlayer.setTime(time);
    }

//	public void setNetWorkCache(int time) {
//		mMediaPlayer.setNetworkCaching(time);
//	}

    public Uri pathToUrl(String path) {
        return AndroidUtil.PathToUri(path);
    }

    public boolean canSeekable() {
        return mCanSeek;
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public boolean isSeekable() {
        return mMediaPlayer.isSeekable();
    }

    public int getPlayerState() {
        return mMediaPlayer.getPlayerState();
    }

    public int getVolume() {
        return mMediaPlayer.getVolume();
    }

    public void setVolume(int volume) {
        mMediaPlayer.setVolume(volume);
    }

    public void seek(int delta) {
        // unseekable stream
        if (mMediaPlayer.getLength() <= 0 || !mCanSeek)
            return;

        long position = mMediaPlayer.getTime() + delta;
        if (position < 0)
            position = 0;
        mMediaPlayer.setTime(position);
    }

    public void seek(long position, float length) {
        if (length == 0f)
            mMediaPlayer.setTime(position);
        else
            mMediaPlayer.setPosition(position / length);
    }

    private final Handler eventHandler = new VideoPlayerHandler(this);

    private static class VideoPlayerHandler extends WeakHandler<PlayerView> {
        public VideoPlayerHandler(PlayerView owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            PlayerView playerView = getOwner();
            if (playerView == null)
                return;

            switch (msg.getData().getInt("event")) {
                case EventHandler.MediaPlayerNothingSpecial:
                    if (playerView.mOnChangeListener != null) {
                        playerView.mOnChangeListener.onPlayNothing();
                    }
                    break;
                case EventHandler.MediaPlayerOpening:
                    //################
                    if (playerView.mOnChangeListener != null) {
                        playerView.mOnChangeListener.onStartPlay();
                    }
                    break;
                case EventHandler.MediaParsedChanged:
                    Log.d(TAG, "MediaParsedChanged");
                    break;
                case EventHandler.MediaPlayerPlaying:
                    Log.d(TAG, "MediaPlayerPlaying");
                    if (playerView.mOnChangeListener != null) {
                        playerView.mOnChangeListener.onLoadComplete();
                    }
                    break;
                case EventHandler.MediaPlayerPaused:
                    Log.d(TAG, "MediaPlayerPaused");
                    break;
                case EventHandler.MediaPlayerStopped:
                    Log.d(TAG, "MediaPlayerStopped");
                    break;
                case EventHandler.MediaPlayerEndReached:
                    Log.d(TAG, "MediaPlayerEndReached");
                    if (playerView.mOnChangeListener != null) {
                        playerView.mOnChangeListener.onEnd();
                    }
                    break;
                case EventHandler.MediaPlayerVout:
                    break;
                case EventHandler.MediaPlayerPositionChanged:
                    if (!playerView.mCanSeek) {
                        playerView.mCanSeek = true;
                    }
                    break;
                case EventHandler.MediaPlayerEncounteredError:
                    Log.d(TAG, "MediaPlayerEncounteredError");
                    if (playerView.mOnChangeListener != null) {
                        //playerView.mOnChangeListener.onError();
                        playerView.mOnChangeListener.onPlayerError();
                    }
                    break;
                case EventHandler.HardwareAccelerationError:
                    Log.d(TAG, "HardwareAccelerationError");
                    playerView.stop();
                    if (playerView.mOnChangeListener != null && playerView.mDisabledHardwareAcceleration) {
                        playerView.mOnChangeListener.onError();
                    } else {
                        playerView.mDisabledHardwareAcceleration = true;
                        playerView.start();
                    }
                    break;
                case EventHandler.MediaPlayerTimeChanged:
                    // avoid useless error logs
                    break;
                case EventHandler.MediaPlayerBuffering:
                    Log.d(TAG, "MediaPlayerBuffering");
                    if (playerView.mOnChangeListener != null) {
                        playerView.mOnChangeListener.onBufferChanged(msg.getData().getFloat("data"));
                    }
                    break;
                default:
                    Log.d(TAG, String.format("Event not handled (0x%x)", msg.getData().getInt("event")));
                    break;
            }
        }
    }

    ;

    private static LibVLC LibVLC() {
        return VLCInstance.get();
    }

    private MediaPlayer newMediaPlayer() {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        LibVLC().setOnHardwareAccelerationError(this);
        final MediaPlayer mp = new MediaPlayer(LibVLC());
        final String aout = VLCOptions.getAout(pref);
        if (mp.setAudioOutput(aout) && aout.equals("android_audiotrack")) {
            mIsAudioTrack = true;
            if (mHasHdmiAudio)
                mp.setAudioOutputDevice("hdmi");
        } else
            mIsAudioTrack = false;
        mp.getVLCVout().addCallback(this);
        mp.getVLCVout().setVideoView(mSurface);
        mp.getVLCVout().attachViews();

        return mp;
    }

    private void changeSurfaceLayout() {
        int sw;
        int sh;

        // get screen size

        sw = getWidth();
        sh = getHeight();


        double dw = sw, dh = sh;
        boolean isPortrait;

        isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh;
            dh = sw;
        }

        // sanity check
        if (dw * dh == 0 || mVideoWidth * mVideoHeight == 0) {
            Log.e(TAG, "Invalid surface size");
            return;
        }

        // compute the aspect ratio
        double ar, vw;
        if (mSarDen == mSarNum) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth;
            ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * (double) mSarNum / mSarDen;
            ar = vw / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double dar = dw / dh;

        switch (mCurrentSize) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_FIT_HORIZONTAL:
                dh = dw / ar;
                break;
            case SURFACE_FIT_VERTICAL:
                dw = dh * ar;
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoVisibleHeight;
                dw = vw;
                break;
        }


        // set display size
        LayoutParams lp = (LayoutParams) mSurface.getLayoutParams();
        lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
        mSurface.setLayoutParams(lp);

        // set frame size (crop if necessary)
        lp = (LayoutParams) mSurfaceFrame.getLayoutParams();
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);
        if (mOnSurfaceSizeChangeListener != null) {
            if (lp.width != lastSurfaceWidth || lp.height != lastSurfaceHeight) {
                lastSurfaceHeight = lp.height;
                lastSurfaceWidth = lp.width;
                mOnSurfaceSizeChangeListener.onSizeChanged(lastSurfaceWidth, lastSurfaceHeight);
            }
        }
        mSurfaceFrame.setLayoutParams(lp);

        mSurface.invalidate();

    }

    @Override
    public void onNewLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum,
                            int sarDen) {
        // TODO Auto-generated method stub
        if (width * height == 0)
            return;
        LogUtils.e("width" + width + ",height" + height + ",visibleWidth" + visibleWidth + " + visibleHeight + " + visibleHeight + ",sarNum=" + sarNum + ",sarDen" + sarDen);
        // store video size
        mVideoWidth = width;
        mVideoHeight = height;
        mVideoVisibleWidth = visibleWidth;
        mVideoVisibleHeight = visibleHeight;
        mSarNum = sarNum;
        mSarDen = sarDen;
//        changeSurfaceLayout();
        changeSurfaceSize();
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {
        // TODO Auto-generated method stub
//		restartMediaPlayer();
    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {
        // TODO Auto-generated method stub
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            stop();
        }
        vlcVout.detachViews();
    }
}
