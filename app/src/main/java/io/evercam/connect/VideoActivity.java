package io.evercam.connect;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import io.evercam.connect.db.Camera;
import io.evercam.connect.db.CameraOperation;
import io.evercam.connect.helper.Constants;
import io.evercam.connect.helper.CustomedDialog;
import io.evercam.connect.helper.PropertyReader;
import io.evercam.connect.helper.ResourceHelper;

public class VideoActivity extends Activity implements SurfaceHolder.Callback,IVideoPlayer
{
	private final static String TAG = "evercamdiscover-VideoActivity";

	private Camera camera;

	private static List<MediaURL> mediaUrls = null;
	private static int mrlIndex = -1;
	private String mrlPlaying = null;
	private boolean showImagesVideo = false;

	// display surface
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private ProgressView progressView = null;

	// media player
	private LibVLC libvlc;
	private int mVideoWidth;
	private int mVideoHeight;
	private final static int videoSizeChanged = -1;

	// Screen view change variables
	private int screen_width, screen_height;
	private int media_width = 0, media_height = 0;
	private boolean landscape;

	private RelativeLayout imageViewLayout;
	private ImageView imageView;
	private ImageView mediaPlayerView;

	private long downloadStartCount = 0;
	private long downloadEndCount = 0;
	private BrowseImages imageThread;
	private boolean isProgressShowing = true;
	static boolean enableLogs = true;

	// image tasks and thread variables
	private int sleepIntervalMinTime = 201; // interval between two requests of
											// images
	private int intervalAdjustment = 1; // how much milli seconds to increment
										// or decrement on image failure or
										// success
	private int sleepInterval = sleepIntervalMinTime + 290; // starting image
															// interval
	private boolean startDownloading = false; // start making requests soon
												// after the image is received
												// first time. Until first image
												// is not received, do not make
												// requests
	private static long latestStartImageTime = 0; // time of the latest request
													// that has been made
	private boolean isFirstImageReceived = false;
	private boolean isFirstImageEnded = false;

	private int successiveFailureCount = 0; // how much successive image
											// requests have failed
	private Boolean isShowingFailureMessage = false;

	private static String startingCameraID;
	private int defaultCameraIndex;

	private static String imageURL = "";

	private boolean paused = false;

	private Animation fadeInAnimation = null;

	private boolean end = false; // whether to end this activity or not

	private Handler handler = new MyHandler(this);
	private PropertyReader propertyReader;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);
			getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			setDisplayOriention();
			setContentView(R.layout.activity_video);

			propertyReader = new PropertyReader(getApplicationContext());
			if (propertyReader.isPropertyExist(PropertyReader.KEY_BUG_SENSE))
			{
				String bugSenseCode = propertyReader.getPropertyStr(PropertyReader.KEY_BUG_SENSE);
				BugSenseHandler.initAndStartSession(VideoActivity.this, bugSenseCode);
			}

			EvercamDiscover.sendScreenAnalytics(this, getString(R.string.screen_video));

			loadCameraFromDatabase();

			initialPageElements();

			launchPlayer();
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString(), e);
			BugSenseHandler.sendException(e);
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		this.paused = false;
	}

	// When activity gets focused again
	@Override
	public void onRestart()
	{
		try
		{
			super.onRestart();
			paused = false;
			end = false;
			mrlPlaying = null;

			createPlayer(getCurrentMRL());
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString());
			BugSenseHandler.sendException(e);
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
		this.paused = true;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		BugSenseHandler.startSession(this);
	}

	@Override
	public void onStop()
	{
		super.onStop();
		releasePlayer();

		if (imageThread != null)
		{
			this.paused = true;
		}
		this.finish();

		BugSenseHandler.closeSession(this);
	}

	public void launchPlayer()
	{
		showImagesVideo = false;
		if (imageThread != null && imageThread.getStatus() != AsyncTask.Status.RUNNING)
		{
			imageThread.cancel(true);
		}
		imageThread = null;

		mrlPlaying = null;
		setCameraForPlaying(VideoActivity.this, camera);

		createPlayer(getCurrentMRL());
	}

	private void loadCameraFromDatabase()
	{
		CameraOperation cameraOperation = new CameraOperation(getApplicationContext());
		Bundle extras = getIntent().getExtras();
		String ipString = extras.getString(Constants.BUNDLE_KEY_IP);
		String ssid = extras.getString(Constants.BUNDLE_KEY_SSID);
		camera = cameraOperation.getCamera(ipString, ssid);
	}

	private void setCameraForPlaying(Context context, Camera camera)
	{
		try
		{
			showImagesVideo = false;

			downloadStartCount = 0;
			downloadEndCount = 0;
			isProgressShowing = false;

			startDownloading = false;
			latestStartImageTime = 0;
			isFirstImageReceived = false;
			isFirstImageEnded = false;
			successiveFailureCount = 0;
			isShowingFailureMessage = false;

			mediaPlayerView.setVisibility(View.GONE);

			paused = false;
			end = false;

			surfaceView.setVisibility(View.GONE);
			imageView.setVisibility(View.VISIBLE);
			showProgressView();

			if (camera.getSsid().equals("sample"))
			{
				imageURL = ResourceHelper.getExternalHttpURL(camera) + camera.getJpg();
				mrlPlaying = ResourceHelper.getExternalFullRtspURL(camera);
			}
			else
			{
				imageURL = ResourceHelper.getInternalHttpURL(camera) + camera.getJpg();
				mrlPlaying = ResourceHelper.getInternalFullRtspURL(camera);
			}
			mediaUrls = new ArrayList<MediaURL>();
			mrlIndex = -1;

			if (mrlPlaying != null)
			{
				addUrlIfValid(mrlPlaying);
				mrlIndex = 0;
				mrlPlaying = null;
			}
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
		}
	}

	private void addUrlIfValid(String url)
	{
		try
		{
			if (url == null
					|| url.trim().length() < 10
					|| !(url.startsWith(Constants.PREFIX_HTTP) || url
							.startsWith(Constants.PREFIX_RTSP))) return;

			MediaURL localMRL = new MediaURL(url);

			if (!mediaUrls.contains(localMRL))
			{
				mediaUrls.add(localMRL);
			}

			mrlIndex = 0;
		}
		catch (Exception e)
		{
			Log.e(TAG, e.toString() + "::" + Log.getStackTraceString(e));
		}
	}

	public void loadImageFromCache()
	{
		startDownloading = true;
	}

	private void startMediaPlayerAnimation()
	{
		if (fadeInAnimation != null)
		{
			fadeInAnimation.cancel();
			fadeInAnimation.reset();

			mediaPlayerView.clearAnimation();
		}

		fadeInAnimation = AnimationUtils.loadAnimation(VideoActivity.this, R.layout.fadein);

		fadeInAnimation.setAnimationListener(new Animation.AnimationListener(){
			@Override
			public void onAnimationStart(Animation animation)
			{
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{

				if (!paused) mediaPlayerView.setVisibility(View.GONE);
				else mediaPlayerView.setVisibility(View.VISIBLE);

				int orientation = VideoActivity.this.getResources().getConfiguration().orientation;
				if (!paused && orientation == Configuration.ORIENTATION_LANDSCAPE)
				{
					// VideoActivity.this.getActionBar().hide();
				}
			}
		});

		mediaPlayerView.startAnimation(fadeInAnimation);
	}

	private boolean isCurrentMRLValid()
	{
		if (mrlIndex < 0 || mrlIndex >= mediaUrls.size() || mediaUrls.size() == 0)
		{
			return false;
		}
		return true;
	}

	private boolean isNextMRLValid()
	{
		if (mrlIndex + 1 >= mediaUrls.size() || mediaUrls.size() == 0) return false;
		return true;
	}

	private String getCurrentMRL()
	{
		if (isCurrentMRLValid()) return mediaUrls.get(mrlIndex).url;
		return null;
	}

	private String getNextMRL()
	{
		if (isNextMRLValid()) return mediaUrls.get(++mrlIndex).url;
		return null;
	}

	/*************
	 * Surface
	 *************/
	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder)
	{
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceholder, int format, int width, int height)
	{
		if (libvlc != null) libvlc.attachSurface(surfaceHolder.getSurface(), this);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceholder)
	{
	}

	private void setSize(int width, int height)
	{
		mVideoWidth = width;
		mVideoHeight = height;
		if (mVideoWidth * mVideoHeight <= 1) return;

		int w = getWindow().getDecorView().getWidth();
		int h = getWindow().getDecorView().getHeight();

		boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
		if (w > h && isPortrait || w < h && !isPortrait)
		{
			int i = w;
			w = h;
			h = i;
		}

		float videoAR = (float) mVideoWidth / (float) mVideoHeight;
		float screenAR = (float) w / (float) h;

		if (screenAR < videoAR) h = (int) (w / videoAR);
		else w = (int) (h * videoAR);

		// force surface buffer size
		surfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);

		// set display size
		LayoutParams lp = surfaceView.getLayoutParams();
		lp.width = w;
		lp.height = h;
		surfaceView.setLayoutParams(lp);
		surfaceView.invalidate();
	}

	@Override
	public void setSurfaceSize(int width, int height, int visible_width, int visible_height,
			int sar_num, int sar_den)
	{
		Message msg = Message.obtain(handler, videoSizeChanged, width, height);
		msg.sendToTarget();
	}

	/*************
	 * Player
	 *************/
	private void createPlayer(String media)
	{
		releasePlayer();
		try
		{
			if (!media.isEmpty())
			{
				if (!camera.getSsid().equals("sample"))
				{
					showToast(getString(R.string.connecting) + media);
				}
				else
				{
					showToast(getString(R.string.connecting));
				}
			}

			// Create a new media player
			libvlc = LibVLC.getInstance();
			libvlc.setSubtitlesEncoding("");
			libvlc.setAout(LibVLC.AOUT_OPENSLES);
			libvlc.setTimeStretching(false);
			libvlc.setChroma("RV32");
			libvlc.setVerboseMode(true);
			LibVLC.restart(this);
			EventHandler.getInstance().addHandler(handler);
			surfaceHolder.setFormat(PixelFormat.RGBX_8888);
			surfaceHolder.setKeepScreenOn(true);
			MediaList list = libvlc.getMediaList();
			list.clear();
			list.add(new Media(libvlc, LibVLC.PathToURI(media)), false);
			libvlc.playIndex(0);
		}
		catch (Exception e)
		{
			Toast.makeText(this, "Error connecting! " + media + " ::::: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
		catch (Error e)
		{
			Log.e(TAG, e.getMessage());
		}
	}

	private void releasePlayer()
	{
		try
		{
			if (libvlc == null) return;
			EventHandler.getInstance().removeHandler(handler);
			libvlc.stop();
			libvlc.detachSurface();
			libvlc.closeAout();
			libvlc.destroy();
			libvlc = null;

			mVideoWidth = 0;
			mVideoHeight = 0;
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getMessage());
		}
	}

	private void restartPlay(String media)
	{
		if (libvlc == null) return;

		try
		{
			libvlc.stop();

			if (media.length() > 0)
			{
				if (!camera.getSsid().equals("sample"))
				{
					showToast(getString(R.string.reconnecting) + media);
				}
				else
				{
					showToast(getString(R.string.reconnecting));
				}
			}

			libvlc.getMediaList().clear();
			libvlc.playMRL(media);
		}
		catch (Exception e)
		{
			Toast.makeText(this, "Error reconnecting! " + media + " ::::: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	private void pausePlayer()
	{
		if (libvlc == null) return;
		libvlc.pause();
	}

	private void stopPlayer()
	{
		if (libvlc == null) return;
		libvlc.stop();
	}

	private void playPlayer()
	{
		if (libvlc == null) return;
		libvlc.play();
	}

	public void setImageAttributesAndLoadImage()
	{
		try
		{
			isFirstImageReceived = false;
			isFirstImageEnded = false;

			mediaPlayerView.setVisibility(View.GONE);

			startDownloading = false;
			this.paused = false;
			this.end = false;
			this.isShowingFailureMessage = false;
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getMessage(), e);
			BugSenseHandler.sendException(e);
		}
	}

	// when screen gets rotated
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		try
		{
			super.onConfigurationChanged(newConfig);

			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			int orientation = newConfig.orientation;
			if (orientation == Configuration.ORIENTATION_PORTRAIT)
			{
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
						WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
				landscape = false;
				// this.getActionBar().show();
			}
			else
			{
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
						WindowManager.LayoutParams.FLAG_FULLSCREEN);
				landscape = true;

				// if (!paused && !end && !isProgressShowing)
				// this.getActionBar().hide();
				// else this.getActionBar().show();
			}

			this.invalidateOptionsMenu();

			mVideoWidth = surfaceView.getWidth();
			mVideoHeight = surfaceView.getHeight();
			// - this.getActionBar().getHeight();
			setSize(mVideoWidth, mVideoHeight);

		}
		catch (Exception e)
		{
			BugSenseHandler.sendException(e);
		}
	}

	// resize the activity if screen gets rotated
	public void resize(int imageHieght, int imageWidth)
	{
		int w = landscape ? screen_height : screen_width;
		int h = landscape ? screen_width : screen_height;

		// If we have the media, calculate best scaling inside bounds.
		if (imageWidth > 0 && imageHieght > 0)
		{
			final float max_w = w;
			final float max_h = h;
			float temp_w = imageWidth;
			float temp_h = imageHieght;
			float factor = max_w / temp_w;
			temp_w *= factor;
			temp_h *= factor;

			// If we went above the height limit, scale down.
			if (temp_h > max_h)
			{
				factor = max_h / temp_h;
				temp_w *= factor;
				temp_h *= factor;
			}

			w = (int) temp_w;
			h = (int) temp_h;
		}
		media_height = h;
		media_width = w;
	}

	private void showToast(String text)
	{
		Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.show();
	}

	private void showMediaFailureDialog()
	{
		CustomedDialog.getAlertDialog(this, getString(R.string.msg_unable_to_play),
				getString(R.string.msg_video_not_avaliable), new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
						VideoActivity.this.finish();
					}
				}).show();
		isShowingFailureMessage = true;
		showImagesVideo = false;
	}

	private void setDisplayOriention()
	{
		int orientation = this.getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		}
		else
		{
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

	private void initialPageElements()
	{
		imageViewLayout = (RelativeLayout) this.findViewById(R.id.video_layout);
		imageView = (ImageView) this.findViewById(R.id.video_image_view);
		mediaPlayerView = (ImageView) this.findViewById(R.id.video_image_player);

		surfaceView = (SurfaceView) findViewById(R.id.video_surface_view);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);

		progressView = ((ProgressView) imageViewLayout.findViewById(R.id.video_progress_spinner));

		progressView.canvasColor = Color.TRANSPARENT;

		isProgressShowing = true;
		progressView.setVisibility(View.VISIBLE);

		mediaPlayerView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				if (end)
				{
					Toast.makeText(VideoActivity.this, R.string.msg_try_again, Toast.LENGTH_SHORT)
							.show();
					return;
				}
				if (isProgressShowing) return;
				if (paused) // video is currently paused. Now we need to
							// resume it.
				{
					showProgressView();

					mediaPlayerView.setImageBitmap(null);
					mediaPlayerView.setVisibility(View.VISIBLE);
					mediaPlayerView.setImageResource(android.R.drawable.ic_media_pause);

					startMediaPlayerAnimation();

					restartPlay(mrlPlaying);
					paused = false;
				}
				else
				// video is currently playing. Now we need to pause video
				{
					mediaPlayerView.clearAnimation();
					if (fadeInAnimation != null && fadeInAnimation.hasStarted()
							&& !fadeInAnimation.hasEnded())
					{
						fadeInAnimation.cancel();
						fadeInAnimation.reset();
					}
					mediaPlayerView.setVisibility(View.VISIBLE);
					mediaPlayerView.setImageBitmap(null);
					mediaPlayerView.setImageResource(android.R.drawable.ic_media_play);

					stopPlayer();

					paused = true; // mark the images as paused. Do not stop
									// threads, but do not show the images
									// showing up
				}
			}
		});

		imageViewLayout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v)
			{
				if (end)
				{
					Toast.makeText(VideoActivity.this, R.string.msg_try_again, Toast.LENGTH_SHORT)
							.show();
					return;
				}
				if (isProgressShowing) return;

				if (!paused && !end) // video is currently playing. Now we
										// need to pause video
				{
					// VideoActivity.this.getActionBar().show();
					mediaPlayerView.setImageResource(android.R.drawable.ic_media_pause);

					mediaPlayerView.setVisibility(View.VISIBLE);

					startMediaPlayerAnimation();
				}

			}
		});

		// Get the size of the device, will be our maximum.
		Display display = getWindowManager().getDefaultDisplay();
		screen_width = display.getWidth();
		screen_height = display.getHeight();
	}

	// Hide progress view
	void hideProgressView()
	{
		imageViewLayout.findViewById(R.id.video_progress_spinner).setVisibility(View.GONE);
		isProgressShowing = false;
		isProgressShowing = false;
	}

	void showProgressView()
	{
		progressView.canvasColor = Color.TRANSPARENT;
		progressView.setVisibility(View.VISIBLE);
		isProgressShowing = true;
	}

	private void createNewImageThread()
	{
		imageThread = new BrowseImages();
		imageThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 * @return Snapshot as InputStream, possible to be null.
	 */
	public InputStream getStreamFromUrl(String url, String username, String password)
			throws UnirestException
	{
		InputStream inputStream = null;

		HttpResponse response = Unirest.get(url).basicAuth(username, password).asBinary();
		inputStream = response.getRawBody();

		return inputStream;
	}

	public class BrowseImages extends AsyncTask<String, String, String>
	{
		@Override
		protected String doInBackground(String... params)
		{
			while (!end && !isCancelled() && showImagesVideo)
			{
				try
				{
					// wait for starting
					try
					{
						while (!startDownloading)
						{
							Thread.sleep(500);
						}
					}
					catch (Exception e)
					{
						BugSenseHandler.sendException(e);
					}

					if (!paused) // if application is paused, do not send the
									// requests. Rather wait for the play
									// command
					{
						DownloadImage tasklive = new DownloadImage();

						if (downloadStartCount - downloadEndCount < 9) tasklive.executeOnExecutor(
								AsyncTask.THREAD_POOL_EXECUTOR, new String[] { imageURL });

						if (downloadStartCount - downloadEndCount > 9 && sleepInterval < 2000)
						{
							sleepInterval += intervalAdjustment;
						}
						else if (sleepInterval >= sleepIntervalMinTime)
						{
							sleepInterval -= intervalAdjustment;
						}
					}
				}
				catch (RejectedExecutionException ree)
				{
					Log.e(TAG, ree.toString() + "-::REE::-" + Log.getStackTraceString(ree));

				}
				catch (Exception ex)
				{
					downloadStartCount--;
					Log.e(TAG, ex.toString() + "-::::-" + Log.getStackTraceString(ex));
					BugSenseHandler.sendException(ex);
				}
				try
				{
					Thread.currentThread();
					Thread.sleep(sleepInterval, 10);
				}
				catch (Exception e)
				{
					Log.e(TAG, e.toString() + "--" + Log.getStackTraceString(e));
				}
			}
			return null;
		}
	}

	/*************
	 * Events
	 *************/
	private static class MyHandler extends Handler
	{
		private WeakReference<VideoActivity> videoActivity;

		public MyHandler(VideoActivity owner)
		{
			videoActivity = new WeakReference<VideoActivity>(owner);
		}

		@Override
		public void handleMessage(Message msg)
		{
			try
			{
				VideoActivity player = videoActivity.get();

				// SamplePlayer events
				if (msg.what == videoSizeChanged)
				{
					player.setSize(msg.arg1, msg.arg2);
					return;
				}

				// Libvlc events
				Bundle bundle = msg.getData();
				int event = bundle.getInt("event");

				switch (event)
				{
				case EventHandler.MediaPlayerEndReached:

					player.restartPlay(player.mrlPlaying);
					break;
				case EventHandler.MediaPlayerPlaying:

					player.surfaceView.setVisibility(View.VISIBLE);
					player.imageView.setVisibility(View.GONE);
					player.mrlPlaying = player.getCurrentMRL();

					break;

				case EventHandler.MediaPlayerPaused:
					break;

				case EventHandler.MediaPlayerStopped:
					break;

				case EventHandler.MediaPlayerEncounteredError:

					Log.v(TAG, "EventHandler.MediaPlayerEncounteredError");
					player.loadImageFromCache();

					if (player.mrlPlaying == null && player.isNextMRLValid())
					{
						player.restartPlay(player.getNextMRL());
					}
					else if (player.mrlPlaying != null)
					{
						player.restartPlay(player.mrlPlaying);
					}
					else
					{
						player.showToast(videoActivity.get().getString(R.string.msg_switch_to_jpg));
						player.showImagesVideo = true;
						player.createNewImageThread();
					}

					break;

				case EventHandler.MediaPlayerVout:
					Log.v(TAG, "EventHandler.MediaPlayerVout");
					player.hideProgressView();

					break;
				default:
					break;
				}

			}
			catch (Exception e)
			{
				Log.e(TAG, e.getMessage());
			}
		}
	}

	public static class MediaURL
	{
		public String url = "";

		public MediaURL(String url)
		{
			this.url = url;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == null) return false;
			if (obj == this) return true;
			if (!(obj instanceof MediaURL)) return false;

			MediaURL mediaURL = (MediaURL) obj;
			return this.url.equalsIgnoreCase(mediaURL.url);
		}
	}

	private class DownloadImage extends AsyncTask<String, Void, Drawable>
	{
		private long myStartImageTime;

		@Override
		protected Drawable doInBackground(String... urls)
		{
			if (!showImagesVideo) return null;
			Drawable response = null;

			for (String url : urls)
			{
				try
				{
					downloadStartCount++;
					myStartImageTime = SystemClock.uptimeMillis();

					if (!url.isEmpty())
					{
						InputStream stream = getStreamFromUrl(url, camera.getUsername(),
								camera.getPassword());
						response = Drawable.createFromStream(stream, "src");
					}
					if (response != null)
					{
						successiveFailureCount = 0;
					}
					else
					{
						successiveFailureCount++;
					}
				}

				catch (Exception e)
				{
					Log.e(TAG, "Exception get snapshot: " + e.toString() + "\r\n" + "ImageURl=["
							+ url + "]");

					successiveFailureCount++;
				} finally
				{
					downloadEndCount++;
				}
			}

			return response;
		}

		@Override
		protected void onPostExecute(Drawable result)
		{// DownloadImage Live
			try
			{
				if (!showImagesVideo) return;

				isFirstImageEnded = true;

				if (result != null && result.getIntrinsicWidth() > 0
						&& result.getIntrinsicHeight() > 0
						&& myStartImageTime >= latestStartImageTime && !paused && !end)
				{
					isFirstImageReceived = true;

					latestStartImageTime = myStartImageTime;

					if (mediaPlayerView.getVisibility() != View.VISIBLE
							&& VideoActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
					{
						// VideoActivity.this.getActionBar().hide();
					}

					if (showImagesVideo)
					{
						imageView.setImageDrawable(result);
					}

					hideProgressView();

				}
				// do not show message on local network failure request.
				else if (((!isFirstImageEnded && !isFirstImageReceived) || successiveFailureCount > 10

				)
						&& !isShowingFailureMessage
						&& myStartImageTime >= latestStartImageTime
						&& !paused && !end)
				{
					showMediaFailureDialog();
					imageThread.cancel(true);
				}
				else
				{
					if (enableLogs) Log.i(TAG, "download image discarded. ");
				}
			}
			catch (Exception e)
			{
				if (enableLogs) Log.e(TAG, "Download image on post" + e.toString());
				BugSenseHandler.sendException(e);
			}
			startDownloading = true;
		}
	}
}
