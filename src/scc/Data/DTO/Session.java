package scc.Data.DTO;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.NoContentException;
import jakarta.ws.rs.core.Cookie;
import redis.clients.jedis.Jedis;
import scc.cache.RedisCache;

public class Session {

    private String uid;
    private String user;

    public Session(String uid, String user) {
        this.uid = uid;
        this.user = user;
    }

    public Session() {}

    public String getUid() {
        return uid;
    }

   //get user
    public String getUser() {
        return user;
    }

    public String checkCookieUser(Cookie session, String id) {
        if (session == null || session.getValue() == null) {
            return "Session null";
        }

        Session s=null;
        try (Jedis jedis = RedisCache.getCachePool().getResource()) {
            if(jedis.exists("userSession:"+id)) {
                String logged = jedis.get("userSession:" + id);
                ObjectMapper mapper = new ObjectMapper();
                s = mapper.readValue(logged, Session.class);
            }
        } catch (Exception e) {
            return "Jedis problem";

        }
        if (s == null || s.getUser() == null || s.getUser().length() == 0) {
            return "Invalid session";
        }
        if (!s.getUser().equals(id) && !s.getUid().equals(session.getValue()))
            return "Unauthorized user";

        return "ok";

    }



}
