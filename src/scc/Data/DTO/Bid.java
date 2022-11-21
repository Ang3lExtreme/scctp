package scc.Data.DTO;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.text.DateFormat;
import java.util.Date;

public class Bid {

    private String id;
    private String auctionId;
    private String userId;
    private float value;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private Date time;

    public Bid(String id,String auctionId, String userId, Date time , float value) {
        super();
        this.id = id;
        this.auctionId = auctionId;
        this.userId = userId;
        this.time = time;
        this.value = value;
    }

    public Bid(){}

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}