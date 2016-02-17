package com.mcswainsoftware.kissanime;
import android.content.*;
import android.graphics.drawable.*;
import android.os.*;
import android.support.v7.app.*;
import android.view.*;
import android.widget.*;
import com.mcswainsoftware.kissanime.*;
import java.io.*;
import java.net.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import java.util.*;
import android.graphics.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		new InitTask().execute();
    }
	
	private class InitTask extends AsyncTask<String, Void, String>
	{
		Bundle bun = new Bundle();
		@Override
		protected String doInBackground(String[] p1)
		{
			try {
				
				
				String httpsURL = "http://desolationrom.com/kissanime.php?page=Anime/Fairy-Tail-Dub";
				
				Document doc = Jsoup.connect(httpsURL).timeout(0).get();
				ArrayList<String> episodeList = new ArrayList<String>();
				ArrayList<String> episodeLinks = new ArrayList<String>();
				Element img = doc.select("div.barContent > div > img").first();
				URL url = new URL("http://desolationrom.com/kissanime.php?page="+img.attr("src").replace("https://kissanime.to/", ""));
				InputStream content = (InputStream) url.getContent();
				
				String summary = doc.select("div.barContent > div > p").get(4).text().toString();
				String title = doc.select("a.bigChar").first().text().toString();
				Elements eps = doc.select("table.listing > tbody > tr > td > a");
				for(int i=0; i<eps.size(); i++) {
					episodeLinks.add(eps.get(i).attr("href").toString().replaceFirst("/", ""));
					episodeList.add(eps.get(i).text().toString());
				}
				
				Bitmap cover = BitmapFactory.decodeStream(content);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				cover.compress(Bitmap.CompressFormat.PNG, 100, baos); 
				byte[] coverBytes = baos.toByteArray();
				
				bun.putString("title", title);
				bun.putString("summary", summary);
				bun.putStringArrayList("episodeLinks", episodeLinks);
				bun.putStringArrayList("episodes", episodeList);
				bun.putByteArray("coverBytes", coverBytes);
				
			} catch (Exception x) {
				x.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			Intent intent = new Intent(MainActivity.this, AnimeActivity.class);
			intent.putExtras(bun);
			MainActivity.this.startActivity(intent);
			MainActivity.this.finish();
		}

		
	}
}
