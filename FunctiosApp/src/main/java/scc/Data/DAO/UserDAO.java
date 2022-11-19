package scc.Data.DAO;

import scc.Data.DTO.User;

import java.util.Arrays;

/**
 * Represents a User, as stored in the database
 */
public class UserDAO {
    private String _rid;
    private String _ts;
    private String id;
    private String name;

    private String nickname;
    private String pwd;
    private String photoId;
    //private String[] channelIds;

    public UserDAO() {
    }
    public UserDAO( User u) {
        this(u.getId(), u.getName(), u.getNickname(),u.getPwd(), u.getPhotoId());
    }
    public UserDAO(String id, String name,String nickname, String pwd, String photoId) {
        super();
        this.id = id;
        this.name = name;
        this.nickname = nickname;
        this.pwd = pwd;
        this.photoId = photoId;
        //this.channelIds = channelIds;
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
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setNickname(String nickname){this.nickname = nickname;}

    public String getNickname (){return nickname;}

    public String getPwd() {
        return pwd;
    }
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
    public String getPhotoId() {
        return photoId;
    }
    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }
    /*public String[] getChannelIds() {
        return channelIds == null ? new String[0] : channelIds ;
    }*/
    /*
    public void setChannelIds(String[] channelIds) {
        this.channelIds = channelIds;
    }*/
    public User toUser() {
        return new User( id, name, nickname ,pwd, photoId );
    }
    @Override
    public String toString() {
        return "UserDAO [_rid=" + _rid + ", _ts=" + _ts + ", id=" + id + ", name=" + name + ", nickname="+ nickname+  ", pwd=" + pwd
                + ", photoId=" + photoId  + "]";
    }

}