package scc.Security;

import jakarta.ws.rs.core.Cookie;
import scc.Data.DTO.Session;

import javax.ws.rs.NotAuthorizedException;

public class CheckCookie {

    public CheckCookie() {
    }

    public Session checkCookieUser(Cookie session, String id) throws NotAuthorizedException {

        if (session == null || session.getValue() == null) {
            throw new NotAuthorizedException("No session initialized");
        }
        Session s = null;
        /*try {
            s = RedisLayer.getInstance().getSession(session.getValue());

        } catch (CacheExeption e) {
            throw new NotAuthorizedException("No valid session initialized");

        }*/
        if (s == null || s.getUser() == null || s.getUser().length() == 0) {
            throw new NotAuthorizedException("No valid session initialized");
        }
        if (!s.getUser().equals(id) && !s.getUser().equals("admin"))
            throw new NotAuthorizedException("Invalid user: " + s.getUser());


        return s;

    }


}