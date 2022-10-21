package scc.Data.DTO;


public class Bid {
    private String auctionId;
    private String userId;
    private float value;

    public Bid(String auctionId, String userId, float value) {
        super();
        this.auctionId = auctionId;
        this.userId = userId;
        this.value = value;
    }

    public Bid(){}

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
