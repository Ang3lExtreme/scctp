package scc.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Cookie;
import redis.clients.jedis.Jedis;
import scc.Data.DTO.Session;
import scc.cache.RedisCache;

import javax.ws.rs.NotAuthorizedException;

public class CheckCookie {

    public CheckCookie() {
    }

    public Session checkCookieUser(Cookie session, String id) throws NotAuthorizedException {

        if (session == null || session.getValue() == null) {
            throw new NotAuthorizedException("No session initialized");
        }
        Session s;
        try (Jedis jedis = RedisCache.getCachePool().getResource()) {
            String logged = jedis.get("user:"+id);
            ObjectMapper mapper = new ObjectMapper();
            s = mapper.readValue(logged, Session.class);
        } catch (Exception e) {
            throw new NotAuthorizedException("No valid session initialized");

        }
        if (s == null || s.getUser() == null || s.getUser().length() == 0) {
            throw new NotAuthorizedException("No valid session initialized");
        }
        if (!s.getUser().equals(id) && !s.getUser().equals("admin"))
            throw new NotAuthorizedException("Invalid user: " + s.getUser());


        return s;

    }


}