package com.example.shobhit.mychat.response;

import java.util.ArrayList;
        import java.util.List;
        import com.google.gson.annotations.Expose;
        import com.google.gson.annotations.SerializedName;

//@Generated("org.jsonschema2pojo")
public class GetMessageResponse {

    @SerializedName("result_list")
    @Expose
    public List<ChatDetails> resultList = new ArrayList<ChatDetails>();
    @SerializedName("result")
    @Expose
    public String result;

}
