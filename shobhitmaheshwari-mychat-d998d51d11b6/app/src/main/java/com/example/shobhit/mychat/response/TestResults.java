package com.example.shobhit.mychat.response;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/**
 * Created by Archit on 3/13/16.
 */
public class TestResults {
        @SerializedName("comments")
        @Expose
        public String comments;
        @SerializedName("restaurant_name")
        @Expose
        public String restaurantName;
        @SerializedName("timestamp")
        @Expose
        public String timestamp;
        @SerializedName("nickname")
        @Expose
        public String nickname;
        @SerializedName("id")
        @Expose
        public Integer id;

    }

