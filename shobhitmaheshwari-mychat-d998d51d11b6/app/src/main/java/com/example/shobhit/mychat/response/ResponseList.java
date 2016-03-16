package com.example.shobhit.mychat.response;

/**
 * Created by Archit on 3/13/16.
 */
    import java.util.ArrayList;
    import java.util.List;
    import com.google.gson.annotations.Expose;
    import com.google.gson.annotations.SerializedName;

    public class ResponseList {

        @SerializedName("result_list")
        @Expose
        public List<MessageDetails> result_list = new ArrayList<MessageDetails>();
        @SerializedName("result")
        @Expose
        public String result;

    }
