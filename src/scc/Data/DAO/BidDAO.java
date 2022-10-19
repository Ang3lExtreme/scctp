package scc.Data.DAO;

public class BidDAO {
    private String _rid;
    private String _ts;
    private String auctionId;
    private String userId;
    private float value;

    public BidDAO(String _rid, String _ts, String auctionId, String userId, float value) {
        this._rid = _rid;
        this._ts = _ts;
        this.auctionId = auctionId;
        this.userId = userId;
        this.value = value;
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
