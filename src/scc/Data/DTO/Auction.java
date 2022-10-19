package scc.Data.DTO;

public class Auction {
    private String auctionId;
    private String title;
    private String description;
    private String imageId;
    private String ownerId;
    private String endTime;
    private float minPrice;
    private String winnerId = null;
    private Status status = Status.OPEN;

    public Auction(String auctionId, String title, String description, String imageId, String ownerId, String endTime, float minPrice) {
        super();
        this.auctionId = auctionId;
        this.title = title;
        this.description = description;
        this.imageId = imageId;
        this.ownerId = ownerId;
        this.endTime = endTime;
        this.minPrice = minPrice;

    }

    public Auction(){

    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
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

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
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
