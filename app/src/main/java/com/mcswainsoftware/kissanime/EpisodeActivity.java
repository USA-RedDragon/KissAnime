package com.mcswainsoftware.kissanime;
import android.support.v7.app.*;
import android.os.*;
import android.widget.*;
import android.view.*;
import android.net.Uri;
import android.media.*;
import java.io.*;
import android.content.res.*;
import java.util.concurrent.atomic.*;
import java.util.*;
import android.content.*;

public class EpisodeActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener
{

	@Override
	public void onPrepared(MediaPlayer p1)
	{
		int videoWidth = player.getVideoWidth();
		int videoHeight = player.getVideoHeight();
		float videoProportion = (float) videoWidth/(float) videoHeight;
		int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
		int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
		float screenProportion = (float) screenWidth / (float) screenHeight;
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) videoSurface.getLayoutParams();
		if(videoProportion > screenProportion) {
			lp.width = screenWidth;
			lp.height = (int) ((float) screenWidth / videoProportion);
		} else {
			lp.width = (int) (videoProportion * (float) screenHeight);
			lp.height = screenHeight;
		}
		this.videoSurface.setLayoutParams(lp);
		player.seekTo(time);
		timer.setMax(player.getDuration());
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP){
			timer.setSecondaryProgressTintList(ColorStateList.valueOf(0xFFFFFFFF));
		}
		total.setText(""+player.getDuration());
		if(!timerThread.isAlive())
			timerThread.start();
		player.start();
	}

	@Override
	public void surfaceCreated(SurfaceHolder p1)
	{
		player.setDisplay(holder);
		player.prepareAsync();
	}

	@Override
	public void surfaceChanged(SurfaceHolder p1, int p2, int p3, int p4)
	{
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder p1)
	{
		
	}

	public int getBufferPercentage()
	{
		return percentBuffered;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer p1, int p2)
	{
		this.percentBuffered = p2;
		timer.setSecondaryProgress((p2 * player.getDuration() / 100));
	}

	@Override
	public void onCompletion(MediaPlayer p1)
	{
		//timerThread.stop();
	}

	SurfaceHolder holder;
    SurfaceView videoSurface;
    MediaPlayer player;
	Uri video;
	int time = 0, qualSelected = 0;
	int percentBuffered = 0;
	boolean pausedByMe = false, showing = false;
	ImageView play, quality, fwd, back;
	SeekBar timer;
	TextView curr, total;
	HashMap qualities;
	ArrayList<String> qualName = new ArrayList<String>();
	ArrayList<String> qualValues = new ArrayList<String>();
	Thread timerThread;
	AlertDialog qualityDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.episode);
		getWindow().setBackgroundDrawableResource(android.R.color.black);
		qualities = (HashMap<String, String>) getIntent().getExtras().getSerializable("quality");
		Iterator it = qualities.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			qualName.add((String) pair.getKey());
			qualValues.add((String) pair.getValue());
			it.remove();
		}
		video = Uri.parse(getIntent().getExtras().getString("videoUrl"));
		if(savedInstanceState != null) time = savedInstanceState.getInt("time", 0);
		timerThread = new Thread(new MediaObserver());
		videoSurface = (SurfaceView) findViewById(R.id.video);
		videoSurface.setOnClickListener(this);
		play = (ImageView) findViewById(R.id.playbtn);
		play.setOnClickListener(new ImageView.OnClickListener() {
			public void onClick(View v) {
				ImageView vi = (ImageView) v;
				if(player.isPlaying()) {
					player.pause();
					vi.setImageResource(android.R.drawable.ic_media_play);
				} else {
					player.start();
					vi.setImageResource(android.R.drawable.ic_media_pause);
				}
			}
		});
		fwd = (ImageView) findViewById(R.id.fwdbtn);
		back = (ImageView) findViewById(R.id.backbtn);
		quality = (ImageView) findViewById(R.id.quality);
		quality.setOnClickListener(new ImageView.OnClickListener() {
			public void onClick(View view) {
				AlertDialog.Builder builder = new AlertDialog.Builder(EpisodeActivity.this);
                builder.setTitle("Video Quality");
				
                builder.setSingleChoiceItems(qualName.toArray(new String[0]), qualSelected, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {


							time = player.getCurrentPosition();
							video = Uri.parse(qualValues.get(item));
							qualSelected=item;
							player.release();
							holder = videoSurface.getHolder();
							holder.addCallback(EpisodeActivity.this);

							player = new MediaPlayer();

							try {
								player.setAudioStreamType(AudioManager.STREAM_MUSIC);
								player.setDataSource(EpisodeActivity.this, video);
								player.setOnBufferingUpdateListener(EpisodeActivity.this);
								player.setOnCompletionListener(EpisodeActivity.this);
								player.setOnPreparedListener(EpisodeActivity.this);
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (SecurityException e) {
								e.printStackTrace();
							} catch (IllegalStateException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
							
							qualityDialog.dismiss();    
						}
					});
                qualityDialog = builder.create();
                qualityDialog.show();
			}
		});
		timer = (SeekBar) findViewById(R.id.time);
		timer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// Log.i("TAG", "#### onProgressChanged: " + progress);
				// update current position here
                                curr.setText(progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// Log.i("TAG", "#### onStartTrackingTouch");
				// this tells the controller to stay visible while user scrubs
				// mController.show(3600000);
				// if it is possible, pause the video
				if (player.isPlaying()) {
					player.pause();
				}
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int seekValue = seekBar.getProgress();
				player.seekTo(seekValue);
                                curr.setText(seekValue);
				// Log.i("TAG", "#### onStopTrackingTouch: " + newMinutes);
				//mController.show(3000); // = sDefaultTimeout, hide in 3 seconds
			}
		});

		curr = (TextView) findViewById(R.id.current);
		total = (TextView) findViewById(R.id.total);
		hideIcons();
	}

	@Override
	public void onClick(View p1)
	{
		if(showing)
			hideIcons();
		else
			showIcons();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		//timerThread.stop();
		player.pause();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		
		return false;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		this.time = player.getCurrentPosition();
		player.pause();
		outState.putInt("time", time);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		holder = videoSurface.getHolder();
		holder.addCallback(this);
		
		player = new MediaPlayer();
		
		try {
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setDataSource(this, video);
			player.setOnBufferingUpdateListener(this);
			player.setOnCompletionListener(this);
			player.setOnPreparedListener(this);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			this.videoSurface.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT));
			this.videoSurface.invalidate();
		} else {
			int videoWidth = player.getVideoWidth();
			int videoHeight = player.getVideoHeight();
			float videoProportion = (float) videoWidth/(float) videoHeight;
			int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
			int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
			float screenProportion = (float) screenWidth / (float) screenHeight;
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) videoSurface.getLayoutParams();
			if(videoProportion > screenProportion) {
				lp.width = screenWidth;
				lp.height = (int) ((float) screenWidth / videoProportion);
			} else {
				lp.width = (int) (videoProportion * (float) screenHeight);
				lp.height = screenHeight;
			}
			this.videoSurface.setLayoutParams(lp);
			this.videoSurface.invalidate();
		}
	}
	
	private void showIcons() {
		play.setVisibility(View.VISIBLE);
		fwd.setVisibility(View.VISIBLE);
		back.setVisibility(View.VISIBLE);
		quality.setVisibility(View.GONE);
		timer.setVisibility(View.VISIBLE);
		curr.setVisibility(View.VISIBLE);
		total.setVisibility(View.VISIBLE);
		showing = true;
	}
	
	private void hideIcons() {
		play.setVisibility(View.GONE);
		fwd.setVisibility(View.GONE);
		back.setVisibility(View.GONE);
		quality.setVisibility(View.GONE);
		timer.setVisibility(View.GONE);
		curr.setVisibility(View.GONE);
		total.setVisibility(View.GONE);
		showing = false;
	}
	
	private class MediaObserver implements Runnable {
		private AtomicBoolean stop = new AtomicBoolean(false);

		public void stop() {
			stop.set(true);
		}

		@Override
		public void run() {
			while (!stop.get()) {
				if(player != null) {
					if(player.isPlaying()) {
					runOnUiThread(new Runnable() {
						public void run() {
							timer.setProgress(player.getCurrentPosition());
							curr.setText(""+player.getCurrentPosition());
						}
					});
					}
					try {Thread.sleep(200); } catch (Exception x) {}
				}
			}
		}
	}
	
}
