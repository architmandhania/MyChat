package com.example.shobhit.mychat.response;

/**
 * Created by Archit on 3/13/16.
 */
    import java.util.ArrayList;
    import java.util.List;
    import com.google.gson.annotations.Expose;
    import com.google.gson.annotations.SerializedName;

    public class GetMessageResponse {

        @SerializedName("result_list")
        @Expose
        public List<MessageDetails> resultList = new ArrayList<MessageDetails>();
        @SerializedName("result")
        @Expose
        public String result;

    }
