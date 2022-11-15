package scc.Data.DTO;

import java.util.Date;

public class Auction {
    private String id;
    private String title;
    private String description;
    private String imageId;
    private String ownerId;
    private Date endTime;
    private float minPrice;
    private String winnerId = null;
    private Status status = Status.OPEN;

    public Auction(String id, String title, String description, String imageId, String ownerId, String endTime, float minPrice) {
        super();
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageId = imageId;
        this.ownerId = ownerId;
        //parse endTime to Date
        this.endTime = new Date(endTime);
        this.minPrice = minPrice;

    }
    //constructor including winnerId and status
    public Auction(String id, String title, String description, String imageId, String ownerId, String endTime, float minPrice, String winnerId, Status status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageId = imageId;
        this.ownerId = ownerId;
        this.endTime = new Date(endTime);
        this.minPrice = minPrice;
        this.winnerId = winnerId;
        this.status = status;
    }

    public Auction(){

    }

    public String getAuctionId() {
        return id;
    }

    public void setAuctionId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
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

    public void setEndTime(String endTime) {
        this.endTime = new Date(endTime);
    }

    public float getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(float minPrice) {
        this.minPrice = minPrice;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
