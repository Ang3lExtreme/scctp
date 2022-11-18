package scc.Data.DTO;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NoContentException;
import redis.clients.jedis.Jedis;
import scc.cache.RedisCache;

public class Session {

    private String uid;
    private String user;

    public Session(String uid, String user) {
        this.uid = uid;
        this.user = user;
    }

    public String getUid() {
        return uid;
    }

   //get user
    public String getUser() {
        return user;
    }

    public static Session checkCookieUser(Cookie session, String id) throws Exception {

        if (session == null || session.getValue() == null) {
            throw new NotFoundException("No session initialized");
        }
        Session s = null;
        try (Jedis jedis = RedisCache.getCachePool().getResource()) {
            if(jedis.exists("user:"+id)) {
                String logged = jedis.get("user:" + id);
                ObjectMapper mapper = new ObjectMapper();
                s = mapper.readValue(logged, Session.class);
            }
        } catch (Exception e) {
            throw new NoContentException("No Redis cache available");

        }
        if (s == null || s.getUser() == null || s.getUser().length() == 0) {
            throw new NotFoundException("No valid session initialized");
        }
        if (!s.getUser().equals(id) && !s.getUser().equals("admin"))
            throw new NotAuthorizedException("Invalid user: " + s.getUser());

        return s;

    }



}
