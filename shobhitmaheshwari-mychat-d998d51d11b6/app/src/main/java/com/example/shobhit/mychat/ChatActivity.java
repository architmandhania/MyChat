package com.example.shobhit.mychat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.shobhit.mychat.response.GetMessageResponse;
import com.example.shobhit.mychat.response.MessageDetails;
import com.example.shobhit.mychat.response.PostResult;
import com.example.shobhit.mychat.response.TestResults;

import junit.framework.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.io.IOException;

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
    public static String LOG_TAG = "MyChat ";
    private int count = 0;
    private int list_size = 0;

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
		//ListView myListView = (ListView) findViewById(R.id.listView);
		//myListView.setAdapter(aa);
		aa.notifyDataSetChanged();

		super.onResume();
	}

	public void getMessages(View v) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
		// set your desired log level
		logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
		OkHttpClient httpClient = new OkHttpClient.Builder()
				.addInterceptor(logging)
				.build();

		Retrofit retrofit = new Retrofit.Builder()
                //.baseUrl("https://luca-teaching.appspot.com/localmessages/default/")
                .baseUrl("https://3-dot-what-did-i-eat.appspot.com/backend/api/")
				.addConverterFactory(GsonConverterFactory.create())	//parse Gson string
				.client(httpClient)	//add logging
				.build();

        GetInfo service = retrofit.create(GetInfo.class);

        Call<GetMessageResponse> queryResponseCall = service.getEntry(getNickname());

        //Call retrofit asynchronously
        Log.i(LOG_TAG, "we are about to enqueue onResponse for nickname: " + getNickname());
		queryResponseCall.enqueue(new Callback<GetMessageResponse>() {
            @Override
            public void onResponse(Response<GetMessageResponse> response) {
                Log.i(LOG_TAG, "we were in on response");
                List<MessageDetails> messageList = response.body().resultList;
                Log.i(LOG_TAG, messageList.get(0).comments);

                aList.clear();
                ListElement lelem;
                list_size = messageList.size();
                int size = messageList.size();
                for (int i = 0; i < size; i++) {
                    lelem = new ListElement(messageList.get(i).timestamp,
                            messageList.get(i).comments,
                            messageList.get(i).nickname,
                            messageList.get(i).restaurant_name);
                    aList.add(lelem);
                }

                // We notify the ArrayList adapter that the underlying list has changed,
                // triggering a re-rendering of the list.
                aa.notifyDataSetChanged();


                TextView tmp = (TextView) findViewById(R.id.v_nickname);
                tmp.setText("Nickname: " + messageList.get(0).nickname);
                tmp.setVisibility(View.VISIBLE);

                tmp = (TextView) findViewById(R.id.v_place);
                tmp.setText("Place: " + messageList.get(0).restaurant_name);
                tmp.setVisibility(View.VISIBLE);

                tmp = (TextView) findViewById(R.id.v_comment);
                tmp.setText("Comment: " + messageList.get(0).comments);
                tmp.setVisibility(View.VISIBLE);

                count ++;

            }

            @Override
            public void onFailure(Throwable t) {
                // Log error here since request failed
                Log.i(LOG_TAG, "**** onFailure() called for getInfo. Investigate..*****");
                Log.i(LOG_TAG, t.getMessage());
            }

        });
	}
    public void nextReview( View v){
        if (count < list_size){
            TextView tmp = (TextView) findViewById(R.id.v_nickname);
            tmp.setText("Nickname: " + aList.get(count).nickname);
            tmp.setVisibility(View.VISIBLE);

            tmp = (TextView) findViewById(R.id.v_place);
            tmp.setText("Place: " + aList.get(count).restaurant_name);
            tmp.setVisibility(View.VISIBLE);

            tmp = (TextView) findViewById(R.id.v_comment);
            tmp.setText("Comment: " + aList.get(count).comments);
            tmp.setVisibility(View.VISIBLE);

        }else return;
    }

	public void postMessage(View v) {

        //convert latitude and longitude into acceptable format
        String msg = getMessageToPost();

        if (msg.length() == 0) {
            //nothing to send
            return;
        }
        String comments = msg;
        String restaurant_name = getRestaurant();
        final String nickname = getNickname();

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
                Log.i(LOG_TAG, "we were in onResponse for PostResult http call");
			}

			@Override
			public void onFailure(Throwable t) {
				// Log error here since request failed
                Log.i(LOG_TAG, "**** onFailure() called for PostResult. Investigate..*****");
                Log.i(LOG_TAG, t.getMessage());
			}

		});
	}

    public void fillEntry(){
        TextView tmp = (TextView) findViewById(R.id.v_nickname);
        tmp.setText("Nickname: Khoa");
        tmp.setVisibility(View.VISIBLE);

        tmp = (TextView) findViewById(R.id.v_place);
        tmp.setText("Place: UCSC");
        tmp.setVisibility(View.VISIBLE);

        tmp = (TextView) findViewById(R.id.v_comment);
        tmp.setText("Comment: This place is really cool!");
        tmp.setVisibility(View.VISIBLE);
    }

    private String getNickname() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        return settings.getString(MainActivity.myNickname, "nickname");
    }

    private String getRestaurant() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        return settings.getString(MainActivity.myRestaurant, "restaurant");
    }

    private String getMessageToPost() {
        String message = ((EditText) findViewById(R.id.msgText)).getText().toString().trim();

        //clear that message in the textview
        ((EditText) findViewById(R.id.msgText)).setText("");
        return message;
    }


    public void restart(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        //finish();
    }

	public interface SendInfo {
		@GET("store_ui")
		Call<PostResult> sendEntry(@Query("nickname") String nickname,
                                  @Query("comments") String comments,
                                  @Query("restaurant_name") String restaurant_name);
	}

	public interface GetInfo {
		@GET("read_ui")
		Call<GetMessageResponse> getEntry(@Query("nickname") String nickname);
	}
}