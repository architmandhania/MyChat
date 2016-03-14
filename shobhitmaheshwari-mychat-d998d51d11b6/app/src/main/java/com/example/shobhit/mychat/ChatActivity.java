package com.example.shobhit.mychat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.example.shobhit.mychat.response.ResponseList;
import com.example.shobhit.mychat.response.MessageDetails;
import com.example.shobhit.mychat.response.PostResult;
import com.example.shobhit.mychat.response.TestResults;

import junit.framework.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class ChatActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
	}

	private MyAdapter aa;
	private ArrayList<ListElement> aList;

	@Override
	protected void onResume(){
		Intent intent = getIntent();

		aList = new ArrayList<ListElement>();
		aa = new MyAdapter(this, R.layout.list_element, aList);
		ListView myListView = (ListView) findViewById(R.id.listView);
		myListView.setAdapter(aa);
		aa.notifyDataSetChanged();

		super.onResume();
	}

	public void getMessages(View v) {

		HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
		// set your desired log level
		logging.setLevel(HttpLoggingInterceptor.Level.BODY);
		OkHttpClient httpClient = new OkHttpClient.Builder()
				.addInterceptor(logging)
				.build();

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl("https://3-dot-what-did-i-eat.appspot.com/backend/api/")
				.addConverterFactory(GsonConverterFactory.create())	//parse Gson string
				.client(httpClient)	//add logging
				.build();

        GetInfo service = retrofit.create(GetInfo.class);

		//convert latitude and longitude into acceptable format

        Call<ResponseList> queryResponseCall =
				service.getEntry(getNickname());

		//Call retrofit asynchronously
		queryResponseCall.enqueue(new Callback <ResponseList>() {
            @Override
            public void onResponse(Response<ResponseList> response) {
               List<MessageDetails> messageList = response.body().resultList;
                aList.clear();
                ListElement lelem;
                int size = messageList.size();
                for (int i = 0; i < size; i++) {
                    lelem = new ListElement(messageList.get(i).timestamp,
                            messageList.get(i).comments,
                            messageList.get(i).nickname,
                            messageList.get(i).restaurant_name);
                }

                // We notify the ArrayList adapter that the underlying list has changed,
                // triggering a re-rendering of the list.
                aa.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Throwable t) {
                // Log error here since request failed
            }

        });
	}

	public void postMessage(View v) {

        //convert latitude and longitude into acceptable format
        String msg = getMessageToPost();

        if (msg.length() == 0) {
            //nothing to send
            return;
        }
        String comments = "This is a test comment";
        String restaurant_name = "Cicero's Pizza";
        float latitude = getMyLatitude();
        float longitude = getMyLongitude();
        String myOwnId = getMyId();
        final String nickname = getNickname();
        final String msg_id = getRandomMessageId();
        String timestamp = getCurrentTimestamp();

        //send the message
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
		// set your desired log level
		logging.setLevel(HttpLoggingInterceptor.Level.BODY);
		OkHttpClient httpClient = new OkHttpClient.Builder()
				.addInterceptor(logging)
				.build();

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl("https://3-dot-what-did-i-eat.appspot.com/backend/api/")
				.addConverterFactory(GsonConverterFactory.create())	//parse Gson string
				.client(httpClient)	//add logging
				.build();

        SendInfo service = retrofit.create(SendInfo.class);

		Call<PostResult> queryResponseCall =
				service.sendEntry(nickname, comments, restaurant_name);

        //Call retrofit asynchronously
		queryResponseCall.enqueue(new Callback<PostResult>() {
			@Override
			public void onResponse(Response<PostResult> response) {
                //process the error condition here.
			}

			@Override
			public void onFailure(Throwable t) {
				// Log error here since request failed
			}

		});
	}

    private float getMyLatitude() {
        double latitude;
        try {
            latitude = MainActivity.myLocation.getLatitude();
        } catch (Exception e) {
            Log.i(MainActivity.LOG_TAG, "Latitude threw this exception:" + e.toString() +
                    ". Returning hard coded value");
            latitude = 9.9993;
        }
        return (float)latitude;
    }

    private float getMyLongitude() {
        double longitude;
        try {
            longitude = MainActivity.myLocation.getLongitude();
        } catch (Exception e) {
            Log.i(MainActivity.LOG_TAG, "Longitude threw this exception:" + e.toString() +
                    ". Returning hard coded value");
            longitude = 10.0004;
        }
        return (float) longitude;
    }

    private String getMyId() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        return settings.getString(MainActivity.myUserIdKey, "ownid");
    }

    private String getNickname() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        return settings.getString(MainActivity.myNickname, "nickname");
    }

    private String getMessageToPost() {
        String message = ((EditText) findViewById(R.id.msgText)).getText().toString().trim();

        //clear that message in the textview
        ((EditText) findViewById(R.id.msgText)).setText("");
        return message;
    }

    private String getRandomMessageId() {
        String msg_id = (UUID.randomUUID().toString()).substring(0, 5);
        return msg_id;
    }

    private String getCurrentTimestamp() {
        //may need to change this to match the format of timestamp returned by other messages
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        return timeStamp;
    }

	public interface SendInfo {
		@GET("store_ui")
		Call<PostResult> sendEntry(
                                  @Query("nickname") String nickname,
                                  @Query("comments") String comments,
                                  @Query("restaurant_name") String restaurant_name);
	}

	public interface GetInfo {
		@GET("read_ui")
		Call<ResponseList> getEntry(
                                         @Query("nickname") String nickname
                                         );
	}
}