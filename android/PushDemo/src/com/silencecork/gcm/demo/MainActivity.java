package com.silencecork.gcm.demo;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private TextView mRegId;
	private ProgressDialog mProgress;
	
	// this id can be retrieved from Google API Console
	private static final String SENDER_ID = "";
	private static final String API_SERVER = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mRegId = (TextView) findViewById(R.id.reg_id);

		String currentRegId = getGcmRegId();
		if (TextUtils.isEmpty(currentRegId)) {
			registration();
		} else {
			mRegId.setText(currentRegId);
		}
	}

	public void registration() {
		mProgress = ProgressDialog.show(this, null, "Please wait", true, false);
		GCMRegistrationTask task = new GCMRegistrationTask();
		task.execute();
	}

	public String getGcmRegId() {
		return PreferenceManager.getDefaultSharedPreferences(this).getString(
				"registration_id", null);
	}

	public void saveGcmRegId(String result) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this)
				.edit();
		editor.putString("registration_id", result);
		editor.commit();
	}
	
	/**
	 * This task can help you to s
	 * @author Justin
	 *
	 */
	private class GCMRegistrationTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			Log.d(TAG, "Registering");
			GoogleCloudMessaging gcm = GoogleCloudMessaging
					.getInstance(getApplicationContext());
			try {
				return gcm.register(SENDER_ID);
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				Toast.makeText(getApplicationContext(), "registered with GCM",
						Toast.LENGTH_LONG).show();
				mRegId.setText(result);
				saveGcmRegId(result);
				sendRegIdToServer(result);
			}
			if (mProgress != null) {
				mProgress.dismiss();
			}
		}
		
		private void sendRegIdToServer(String result) {
			try {
				JSONObject json = new JSONObject();
				json.put("user", "justin");
				json.put("type", "android");
				json.put("token", result);
				JsonObjectRequest request = new JsonObjectRequest(
						Request.Method.POST,
						API_SERVER + "/subscribe",
						json, mCompleteListener, mErrorListener);
				String contentType = request.getBodyContentType();
				Log.d(TAG, "Send To Server " + contentType);
				NetworkManager.getInstance(MainActivity.this).request(null,
						request);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	private Listener<JSONObject> mCompleteListener = new Listener<JSONObject>() {

		@Override
		public void onResponse(JSONObject json) {
			Log.d(TAG, "onResponse " + json);
		}
	};

	public ErrorListener mErrorListener = new ErrorListener() {

		@Override
		public void onErrorResponse(VolleyError err) {
			Log.e(TAG, "onError " + err);
		}
	};
}
