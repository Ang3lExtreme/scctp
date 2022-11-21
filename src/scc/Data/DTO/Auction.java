package scc.Data.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class Auction {

    private String id;
    private String title;
    private String imageId;
    private String description;
    private String ownerId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private Date endTime;
    private float minPrice;
    private Status status;
    private String winnerId;

    public Auction(String id, String title, String description, String imageId, String ownerId, Date endTime, float minPrice) {
        super();
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageId = imageId;
        this.ownerId = ownerId;
        this.endTime = endTime;
        this.minPrice = minPrice;
    }

    public Auction(String id, String title, String description, String imageId, String ownerId, Date endTime, float minPrice, String winnerId, Status status) {
        super();
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageId = imageId;
        this.ownerId = ownerId;
        this.endTime = endTime;
        this.minPrice = minPrice;
        this.winnerId = winnerId;
        this.status = status;
    }

    public Auction(){}

    public String getAuctionId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public float getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(float minPrice) {
        this.minPrice = minPrice;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }
}
