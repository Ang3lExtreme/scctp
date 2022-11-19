package scc.Data.DTO;


public class Questions {

    private String id;
private String auctionId;
    private String userId;
    private String message;

    private String reply = null;

    public Questions(String id,String auctionId, String userId, String message) {
        super();
        this.id = id;
        this.auctionId = auctionId;
        this.userId = userId;
        this.message = message;
    }

    public Questions(String id,String auctionId, String userId, String message, String reply) {
        super();
        this.id = id;
        this.auctionId = auctionId;
        this.userId = userId;
        this.message = message;
        this.reply = reply;
    }

    public Questions(){}

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
