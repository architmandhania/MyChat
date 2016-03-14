package com.example.shobhit.mychat;

/**
 * Created by shobhit on 1/24/16.
 */
public class ListElement {
    ListElement() {};

    ListElement(String v_timestamp, String v_comments, String v_nickname, String v_restaurant_name) {
        timestamp = v_timestamp;
        comments = v_comments;
        nickname = v_nickname;
        restaurant_name = v_restaurant_name;
    }


    public String timestamp;
    public String comments;
    public String nickname;
    public String restaurant_name;
}
