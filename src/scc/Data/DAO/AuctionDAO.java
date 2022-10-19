package scc.Data.DAO;

import scc.Data.DTO.Auction;
import scc.Data.DTO.Status;

//based ib Action and UserDAO classes
public class AuctionDAO {
    private String _rid;
    private String _ts;
    private String auctionId;
    private String title;
    private String description;
    private String imageId;
    private String ownerId;
    private String endTime;
    private float minPrice;
    private String winnerId = null;
    private Status status = Status.OPEN;

    public AuctionDAO() {
    }
    public AuctionDAO( Auction a) {
        this(a.getAuctionId(), a.getTitle(), a.getDescription(),a.getImageId(), a.getOwnerId(), a.getEndTime(), a.getMinPrice());
    }
    public AuctionDAO(String auctionId, String title, String description, String imageId, String ownerId, String endTime, float minPrice) {
        super();
        this.auctionId = auctionId;
        this.title = title;
        this.description = description;
        this.imageId = imageId;
        this.ownerId = ownerId;
        this.endTime = endTime;
        this.minPrice = minPrice;

    }
    //constroctor including winnerId and status
    public AuctionDAO(String auctionId, String title, String description, String imageId, String ownerId, String endTime, float minPrice, String winnerId, Status status) {
        this.auctionId = auctionId;
        this.title = title;
        this.description = description;
        this.imageId = imageId;
        this.ownerId = ownerId;
        this.endTime = endTime;
        this.minPrice = minPrice;
        this.winnerId = winnerId;
        this.status = status;
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
    public String get_rid() {
        return _rid;
    }
    public void set_rid(String _rid) {
        this._rid = _rid;
    }
    public String get_ts() {
        return _ts;
    }
    public void set_ts(String _ts) {
        this._ts = _ts;
    }







}
