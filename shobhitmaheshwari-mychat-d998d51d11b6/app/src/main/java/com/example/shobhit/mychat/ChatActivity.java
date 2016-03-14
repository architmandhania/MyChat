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
import android.widget.TextView;

import com.example.shobhit.mychat.response.GetMessageResponse;
import com.example.shobhit.mychat.response.ChatDetails;
import com.example.shobhit.mychat.response.PostResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.sql.Timestamp;

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
				.baseUrl("https://luca-teaching.appspot.com/localmessages/default/")
				.addConverterFactory(GsonConverterFactory.create())	//parse Gson string
				.client(httpClient)	//add logging
				.build();

        GetService service = retrofit.create(GetService.class);

		//convert latitude and longitude into acceptable format

        Call<GetMessageResponse> queryResponseCall =
				service.getChat(getMyLatitude(), getMyLongitude(), getMyId());

		//Call retrofit asynchronously
		queryResponseCall.enqueue(new Callback<GetMessageResponse>() {
            @Override
            public void onResponse(Response<GetMessageResponse> response) {
                List<ChatDetails> messageList = response.body().resultList;
                aList.clear();
                ListElement lelem;
                int size = messageList.size();
                for (int i = 0; i < size; i++) {
                    lelem = new ListElement(messageList.get(i).timestamp,
                            messageList.get(i).message,
                            messageList.get(i).nickname,
                            messageList.get(i).userId,
                            messageList.get(i).messageId);
                    lelem.fade = false;
                    lelem.sent_by_me = (messageList.get(i).userId.compareTo(getMyId()) == 0);
                    aList.add(lelem);
                }
                Collections.reverse(aList);

                // The following code could be tweaked to sort the messages as
                // per their Timestamps, however, since they are returned in
                // the sorted order already, no additional sorting is needed.
                //sort aList based on timestamps

                /*
                Collections.sort(aList, new Comparator<ListElement>() {

                    public int compare(ListElement s1, ListElement s2) {
                        Timestamp t1, t2;
                        t1 = Timestamp.valueOf(s1.timestamp);
                        t2 = Timestamp.valueOf(s2.timestamp);
                        return t1.compareTo(t2);
                    }

                });
                */

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
        float latitude = getMyLatitude();
        float longitude = getMyLongitude();
        String myOwnId = getMyId();
        final String nickname = getNickname();
        final String msg_id = getRandomMessageId();
        String timestamp = getCurrentTimestamp();
        ListElement latestMessage = new ListElement(timestamp, msg, nickname, myOwnId, msg_id);
        latestMessage.fade = true; //draw this message in the list differently
        latestMessage.sent_by_me = true;

        aList.add(latestMessage);
        aa.notifyDataSetChanged();

        //send the message
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
		// set your desired log level
		logging.setLevel(HttpLoggingInterceptor.Level.BODY);
		OkHttpClient httpClient = new OkHttpClient.Builder()
				.addInterceptor(logging)
				.build();

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl("https://luca-teaching.appspot.com/localmessages/default/")
				.addConverterFactory(GsonConverterFactory.create())	//parse Gson string
				.client(httpClient)	//add logging
				.build();

		PostService service = retrofit.create(PostService.class);

		Call<PostResult> queryResponseCall =
				service.postChat(latitude, longitude, myOwnId, nickname, msg, msg_id);

		//Call retrofit asynchronously
		queryResponseCall.enqueue(new Callback<PostResult>() {
			@Override
			public void onResponse(Response<PostResult> response) {
				String result = response.body().result;

                //the latestMessage was posted. Now updated the rendering of that message
                //by finding it using the message id
                for (int i = 0; i < aList.size(); i++) {
                    if (aList.get(i).msg_id.compareTo(msg_id) == 0) {
                        //found the message that was sent. Change the fading effect on that message
                        Log.i(MainActivity.LOG_TAG, "Found last message with id: " + msg_id);
                        aList.get(i).fade = false;
                        break;
                    }
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

	public interface PostService {
		@GET("post_message")
		Call<PostResult> postChat(@Query("lat") float latitude,
                                  @Query("lng") float longitude,
                                  @Query("user_id") String user_Id,
                                  @Query("nickname") String nickname,
                                  @Query("message") String message,
                                  @Query("message_id") String m_id);
	}

	public interface GetService {
		@GET("get_messages")
		Call<GetMessageResponse> getChat(@Query("lat") float latitude,
									    @Query("lng") float longitude,
									    @Query("user_id") String user_Id);
	}
}