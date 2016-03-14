package com.example.shobhit.mychat;

/**
 * Created by shobhit on 1/24/16.
 */
public class ListElement {
    ListElement() {};

    ListElement(String v_timestamp, String v_msg, String v_nickname, String v_user_id, String v_msg_id) {
        timestamp = v_timestamp;
        msg = v_msg;
        nickname = v_nickname;
        user_id = v_user_id;
        msg_id = v_msg_id;
        fade = false;
        sent_by_me = false;
    }

    public String timestamp;
    public String msg;
    public String nickname;
    public String user_id;
    public String msg_id;
    public boolean fade;
    public boolean sent_by_me;
}
