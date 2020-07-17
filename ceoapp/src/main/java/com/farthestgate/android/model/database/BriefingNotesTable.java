package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;

public class BriefingNotesTable extends Model {


    public static final String MESSAGE_ID = "MessageID";
    public static final String DATE = "Date";
    public static final String TITLE = "Title";
    public static final String CONTENT = "Content";
    public static final String CREATED_DATE = "CreatedDate";
    public static final String CEO_SHOULDER_NO = "CeoShoulderNo";
    public static final String READ_MARKED = "ReadMarked";
    public static final String SENT_TO_PUBNUB = "SentToPubNub";

    @Column(name = MESSAGE_ID)
    public String id;
    @Column(name = DATE)
    public String date;
    @Column(name = TITLE)
    public String title;
    @Column(name = CONTENT)
    public String content;
    @Column(name = CREATED_DATE)
    public long createdDate;
    @Column(name = CEO_SHOULDER_NO)
    public String ceoShoulderNo;
    @Column(name = READ_MARKED)
    public int readMarked;
    @Column(name = SENT_TO_PUBNUB)
    public int sentToPubNub;

    public int getSentToPubNub() {
        return sentToPubNub;
    }

    public void setSentToPubNub(int sentToPubNub) {
        this.sentToPubNub = sentToPubNub;
    }

    public int getReadMarked() {
        return readMarked;
    }

    public void setReadMarked(int readMarked) {
        this.readMarked = readMarked;
    }

    public BriefingNotesTable(){
        super();
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public String getCeoShoulderNo() {
        return ceoShoulderNo;
    }

    public void setCeoShoulderNo(String ceoShoulderNo) {
        this.ceoShoulderNo = ceoShoulderNo;
    }

}
