package com.vakils.uploadimagetophpserver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

	private Button button;
	private String encoded_string, image_name;
	private Bitmap bitmap;
	private File file;
	private Uri file_uri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

			int hasWritPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
					Manifest.permission.WRITE_EXTERNAL_STORAGE);

			if (hasWritPermission != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
			}
		}

		button = (Button) findViewById(R.id.start);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				getFileUri();
				i.putExtra(MediaStore.EXTRA_OUTPUT, file_uri);
				startActivityForResult(i, 10);
			}
		});
	}

	private void getFileUri() {
		image_name = "testing123.jpg";
		file = new File(Environment.getExternalStoragePublicDirectory(Environment
				.DIRECTORY_PICTURES)
				+ File.separator + image_name
		);

		file_uri = Uri.fromFile(file);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 10 && resultCode == RESULT_OK) {
			new Encode_image().execute();
		}
	}

	private class Encode_image extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... voids) {

			bitmap = BitmapFactory.decodeFile(file_uri.getPath());
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

			byte[] array = stream.toByteArray();
			encoded_string = Base64.encodeToString(array, 0);
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			makeRequest();
		}
	}

	private void makeRequest() {
		RequestQueue requestQueue = Volley.newRequestQueue(this);
		StringRequest request = new StringRequest(Request.Method.POST, "http://bayer" +
				".vakilspremedia.com/BayerWebService.asmx/UserProfilePic",
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						Toast.makeText(MainActivity.this, "" + response, Toast.LENGTH_SHORT)
								.show();

					}
				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

			}
		}) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				HashMap<String, String> map = new HashMap<>();
				map.put("BayerID", "35");
				map.put("ProfilepicAndroid", encoded_string);
				map.put("Profilepic", image_name);

				return map;
			}
		};
		requestQueue.add(request);
	}
}
