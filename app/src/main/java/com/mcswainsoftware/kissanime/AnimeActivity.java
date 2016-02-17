package com.mcswainsoftware.kissanime;

import java.net.*;
import javax.net.ssl.*;
import android.os.*;
import java.io.*;
import android.widget.*;
import android.webkit.*;
import org.jsoup.nodes.*;
import org.jsoup.*;
import org.jsoup.select.*;
import android.graphics.drawable.*;
import java.util.*;
import android.content.*;
import android.view.*;
import android.support.v7.app.*;
import android.graphics.*;

public class AnimeActivity extends AppCompatActivity 
{
	private ArrayList<String> episodeList, episodeLinks;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
									 
		ListView listview = (ListView) findViewById(R.id.epList);
		TextView summary = (TextView) findViewById(R.id.summary);
		ImageView cover = (ImageView) findViewById(R.id.coverImage);
		
		setTitle(getIntent().getExtras().getString("title"));
		summary.setText(getIntent().getExtras().getString("summary"));
		episodeList = getIntent().getExtras().getStringArrayList("episodes");
		episodeLinks = getIntent().getExtras().getStringArrayList("episodeLinks");
		
		byte[] coverBytes = getIntent().getExtras().getByteArray("coverBytes");
		Bitmap bmp = BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.length);

		cover.setImageBitmap(bmp);
		
		final StableArrayAdapter adapter = new StableArrayAdapter(this,
																  android.R.layout.simple_list_item_1, episodeList);
		listview.setAdapter(adapter);

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, final View view,
										final int position, long id) {
					new StartVideoTask().execute(new String[] {episodeLinks.get(position)});
					Toast.makeText(AnimeActivity.this, episodeLinks.get(position), Toast.LENGTH_SHORT).show();
				}

			});
    }

	private class StartVideoTask extends AsyncTask<String, Void, String>
	{
		Bundle bun = new Bundle();
		String url;
		@Override
		protected String doInBackground(String[] p1)
		{
			try {
				String httpsURL = "http://desolationrom.com/episode.php?episode="+p1[0];
				
				Document doc = Jsoup.connect(httpsURL).timeout(0).get();
				HashMap<String, String> quality = new HashMap<String, String>();
				Elements video = doc.select("a");
				for(int i=0; i<video.size(); i++)  {
					quality.put(video.get(i).text(), video.get(i).attr("href"));
				}
				url = video.first().attr("href");
				bun.putSerializable("quality", quality);
				bun.putString("videoUrl", url);
			} catch (Exception x) {
				x.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			Intent intent = new Intent(AnimeActivity.this, EpisodeActivity.class);
			intent.putExtras(bun);
			startActivity(intent);
		}


	}

	private class StableArrayAdapter extends ArrayAdapter<String> {

		HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

		public StableArrayAdapter(Context context, int textViewResourceId,
								  List<String> objects) {
			super(context, textViewResourceId, objects);
			for (int i = 0; i < objects.size(); ++i) {
				mIdMap.put(objects.get(i), i);
			}
		}

		@Override
		public long getItemId(int position) {
			String item = getItem(position);
			return mIdMap.get(item);
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

	}
}
