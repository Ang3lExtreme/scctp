package scc.Data.DAO;


import scc.Data.DTO.Bid;

public class BidDAO {
    private String _rid;
    private String _ts;
    private String id;
    private String auctionId;
    private String userId;
    private float value;

    public BidDAO(String id, String auctionId, String userId, float value) {
        super();
        this.id = id;
        this.auctionId = auctionId;
        this.userId = userId;
        this.value = value;
    }

    public BidDAO(){}


    public BidDAO(Bid b){
        this(b.getId(),b.getAuctionId(), b.getUserId(), b.getValue());
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

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }





}
