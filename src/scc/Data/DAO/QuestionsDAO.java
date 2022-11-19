package scc.Data.DAO;

import scc.Data.DTO.Questions;

public class QuestionsDAO {
    private String _rid;
    private String _ts;
    private String id;
    private String auctionId;
    private String userId;
    private String message;

    private String reply = null;

    public QuestionsDAO(String id, String auctionId, String userId, String message) {
        super();
        this.id = id;
        this.auctionId = auctionId;
        this.userId = userId;
        this.message = message;
    }
    //constructor that takes in a DTO
    public QuestionsDAO(){

    }



    public QuestionsDAO(Questions q){
        this(q.getId(),q.getAuctionId(), q.getUserId(), q.getMessage());
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
