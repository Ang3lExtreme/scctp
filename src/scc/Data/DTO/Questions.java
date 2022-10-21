package scc.Data.DTO;


public class Questions {
private String auctionId;
    private String userId;
    private String message;

    public Questions(String auctionId, String userId, String message) {
        super();
        this.auctionId = auctionId;
        this.userId = userId;
        this.message = message;
    }

    public Questions(){}

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
