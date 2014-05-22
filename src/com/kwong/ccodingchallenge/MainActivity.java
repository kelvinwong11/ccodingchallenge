package com.kwong.ccodingchallenge;

import java.net.URL;
import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	ArrayList<ImageDetails> images = new ArrayList<ImageDetails>();

	private final String API_KEY = "7bed88e81a4fa3ba04f750ebbf36fd4a";
	private final String BEGINNING_OF_URL =
			"https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=";
	private final String MIDDLE_OF_URL = "&text=";
	private final String END_OF_URL = "&per_page=10&format=json";

	Button searchButton;
	EditText searchTermEditText;
	ListView listView;
	ImageAdapter imageAdapter;
	ProgressDialog loadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		listView = (ListView) findViewById(R.id.cell_list_view);
		listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				new saveFullSizedImageToSD().execute(listView.getPositionForView(arg1));
			}

		});

		searchTermEditText = (EditText) findViewById(R.id.edittext_search_term);
		loadingDialog = new ProgressDialog(getBaseContext());

		searchButton = (Button) findViewById(R.id.button_search);
		searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				images.clear();
				new getJSONData().execute(searchTermEditText.getText().toString());
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public class getJSONData extends AsyncTask<String, Void, Void> {
		@Override
		protected void onPreExecute() {
			loadingDialog = new ProgressDialog(MainActivity.this);
			loadingDialog.setTitle("Processing...");
			loadingDialog.setMessage("Please wait.");
			loadingDialog.setCancelable(false);
			loadingDialog.setIndeterminate(true);
			loadingDialog.show();
		}

		@Override
		protected Void doInBackground(String... searchTerm){
			String htmlFriendlySearchTerm = searchTerm[0].replaceAll(" ", "%20");
			String searchUrl = BEGINNING_OF_URL + API_KEY + MIDDLE_OF_URL +
					htmlFriendlySearchTerm + END_OF_URL;

			HttpClient client = new  DefaultHttpClient();
			HttpGet get = new HttpGet(searchUrl);

			ResponseHandler<String> responseHandler = new BasicResponseHandler();

			String responseBody = null;
			try{
				responseBody = client.execute(get, responseHandler);
			}catch(Exception ex) {
				ex.printStackTrace();
			}

			JSONObject jsonObject = null;
			JSONParser parser=new JSONParser();
			Object obj;

			//remove "jsonFlickrApi( ... ) from JSON data
			responseBody = responseBody.substring(14, responseBody.length()-1);
			Log.v("responseBody", responseBody);
			try {
				obj = parser.parse(responseBody);
				jsonObject=(JSONObject)obj;
			}catch(Exception ex){
				Log.v("jsonObject","Exception: " + ex.getMessage());
			}

			JSONArray arr = null;

			try {
				JSONObject j = (JSONObject) jsonObject.get("photos");
				JSONArray k = (JSONArray) j.get("photo");
				arr = k;
			}catch(Exception ex){
				Log.v("Get JSON Array","Exception: " + ex.getMessage());
			}

			for(Object imgInJSONFormat : arr) {
				ImageDetails img = new ImageDetails(
						((JSONObject)imgInJSONFormat).get("id").toString(),
						((JSONObject)imgInJSONFormat).get("secret").toString(),
						((JSONObject)imgInJSONFormat).get("server").toString(),
						((JSONObject)imgInJSONFormat).get("farm").toString(),
						((JSONObject)imgInJSONFormat).get("title").toString());
				images.add(img);
			}
			return null;
		}

		@Override
		public void onPostExecute(Void result){
			loadingDialog.dismiss();
			getThumbnailForEachImage();
		}

	}

	public void getThumbnailForEachImage(){
		for (int i = 0; i < images.size(); i ++) {
			new downloadSingleImage().execute(i);
		}
	}

	public class downloadSingleImage extends AsyncTask<Integer, Void, Void> {
		@Override
		protected Void doInBackground(Integer... params) {
			ImageDetails currentLoadingImage = images.get(params[0]);
			String bitmapURL = "http://farm" + currentLoadingImage.farmID
					+ ".staticflickr.com/" + currentLoadingImage.serverID
					+ "/" + currentLoadingImage.id
					+ "_" + currentLoadingImage.secret + "_m.jpg";
			//_t for thumbnail 100 on longest side, _m for 240 on longest, _n for 320

			Bitmap tImage = getBitmap(bitmapURL);
			currentLoadingImage.setImageBitmap(tImage);
			return null;
		}

		@Override
		public void onPostExecute(Void result){
			loadingDialog.dismiss();
			if (listView.getAdapter() == null) {
				imageAdapter = new ImageAdapter(MainActivity.this,
						R.layout.listview_custom_row, images);
				listView.setAdapter(imageAdapter);
			} else {
				imageAdapter.notifyDataSetChanged();
			}
		}

	}

	public Bitmap getBitmap(String bitmapUrl) {
		try {
			URL url = new URL(bitmapUrl);
			return BitmapFactory.decodeStream(url.openConnection().getInputStream());
		}
		catch(Exception ex) {
			return null;
		}
	}

	public class saveFullSizedImageToSD extends AsyncTask<Integer, Void, Void> {
		ImageDetails imageDetail;
		boolean success = false;
		@Override
		protected Void doInBackground(Integer... searchTerm){
			Bitmap fullSizedImage = null;
			imageDetail = images.get(searchTerm[0]);
			String searchURL = "http://farm" + imageDetail.farmID
					+ ".staticflickr.com/" + imageDetail.serverID
					+ "/" + imageDetail.id + "_" + imageDetail.secret + "_b.jpg";
			fullSizedImage = getBitmap(searchURL);

//			File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//		    File file = new File(path, imageDetail.id + ".jpg");
//
//			FileOutputStream outStream;
//			try {
//				file.createNewFile();
//				path.mkdirs();
//				outStream = new FileOutputStream(file);
//				fullSizedImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
//				outStream.flush();
//				outStream.close();
//				success = true;
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}

			try {	//saves to internal
				Images.Media.insertImage(getContentResolver(),
						fullSizedImage, imageDetail.id, imageDetail.imageDescription);
				success = true;
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (success) Toast.makeText(getBaseContext(), "Saved " +
					imageDetail.imageDescription + " to album", Toast.LENGTH_SHORT).show();
			else Toast.makeText(getBaseContext(), "Could not save image",
					Toast.LENGTH_SHORT).show();
		}
	}
}