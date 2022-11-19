package scc.Data.DTO;


public class Bid {

    private String id;
    private String auctionId;
    private String userId;
    private float value;

    public Bid(String id, String auctionId, String userId, float value) {
        super();
        this.id = id;
        this.auctionId = auctionId;
        this.userId = userId;
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
}
