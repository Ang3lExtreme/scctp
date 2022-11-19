package scc.Controllers;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import redis.clients.jedis.Jedis;
import scc.Data.DAO.AuctionDAO;
import scc.Data.DAO.UserDAO;
import scc.Data.DTO.Auction;
import scc.Data.DTO.Login;
import scc.Data.DTO.Session;
import scc.Data.DTO.User;
import scc.Database.CosmosAuctionDBLayer;
import scc.Database.CosmosUserDBLayer;
import scc.cache.RedisCache;
import scc.utils.Hash;

import static jakarta.ws.rs.core.Response.Status.*;
import static scc.mgt.AzureManagement.USE_CACHE;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static scc.mgt.AzureManagement.CREATE_REDIS;

@Path("/user")
public class UserController {
    private static final String CONNECTION_URL = System.getenv("COSMOSDB_URL");
    private static final String DB_KEY = System.getenv("COSMOSDB_KEY");
    private static final String HASHCODE = "SHA-256";
    private Jedis jedis;

    //endpoint cannot be null
    private CosmosClient cosmosClient = new CosmosClientBuilder()
            .endpoint(CONNECTION_URL)
            .key(DB_KEY)
            .buildClient();

    private synchronized void initCache() {
        if(jedis != null)
            return;
        jedis = RedisCache.getCachePool().getResource();
    }

    private CosmosUserDBLayer cosmos = new CosmosUserDBLayer(cosmosClient);
    private CosmosAuctionDBLayer cosmosAuction = new CosmosAuctionDBLayer(cosmosClient);

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User writeUser(User user) throws NoSuchAlgorithmException {

        //get user first by id
        CosmosPagedIterable<UserDAO> userDAO = cosmos.getUserById(user.getId());
        //if user exists, return error
        if(userDAO.iterator().hasNext()){
            throw new WebApplicationException("User already exists", 409);
        }

        //if user have same nickname, return error
        CosmosPagedIterable<UserDAO> userDAO2 = cosmos.getUserByNickname(user.getNickname());
        if(userDAO2.iterator().hasNext()){
            throw new WebApplicationException("Nickname already exists", 409);
        }

        MessageDigest messageDigest = MessageDigest.getInstance(HASHCODE);
        messageDigest.update(user.getPwd().getBytes());

        String passHashed = new String(messageDigest.digest());
        user.setPwd(passHashed);
        UserDAO u = new UserDAO(user.getId(), user.getName(), user.getNickname() ,user.getPwd(), user.getPhotoId());

        CosmosItemResponse<UserDAO> response = cosmos.putUser(u);

        return user;

    }


    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(@PathParam("id") String id) {
        CosmosPagedIterable<UserDAO> user = cosmos.getUserById(id);
        if(!user.iterator().hasNext()){
            throw new WebApplicationException("User not found", 404);
        }

        UserDAO userDAO = user.iterator().next();
        User u = new User(userDAO.getId(), userDAO.getName(), userDAO.getNickname(), userDAO.getPwd(), userDAO.getPhotoId());

        return u;
    }

    @DELETE()
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public CosmosPagedIterable<UserDAO> delUser(@PathParam("id") String id) {
        CosmosPagedIterable<UserDAO> user = cosmos.getUserById(id);

        if(!user.iterator().hasNext()){
            throw new WebApplicationException("User not found", 404);
        }
        //else delete user
        CosmosItemResponse<Object> response = cosmos.delUserById(id);
        return user;
    }

    @PUT()
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
      public User updateUser(@PathParam("id") String id, User user) throws NoSuchAlgorithmException {
            CosmosPagedIterable<UserDAO> userDB = cosmos.getUserById(id);

        if(!userDB.iterator().hasNext()){
            throw new WebApplicationException("User not found", 404);
        }

        //if User user have different nickname, check if nickname is already taken
        if(!userDB.iterator().next().getNickname().equals(user.getNickname())){
            CosmosPagedIterable<UserDAO> userDAO2 = cosmos.getUserByNickname(user.getNickname());
            if(userDAO2.iterator().hasNext()){
                throw new WebApplicationException("Nickname already exists", 409);
            }
        }


        MessageDigest messageDigest = MessageDigest.getInstance(HASHCODE);
        messageDigest.update(user.getPwd().getBytes());

        String passHashed = new String(messageDigest.digest());
        user.setPwd(passHashed);

        UserDAO u = new UserDAO(user.getId(), user.getName(), user.getNickname(),user.getPwd(), user.getPhotoId());

        CosmosItemResponse<UserDAO> response = cosmos.updateUser(u);

            return user;
        }

    @GET
    @Path("/auctions/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    //get auctions by user id
    public List<Auction> getAuctionsByUser(@PathParam("id") String id) {
        CosmosPagedIterable<UserDAO> user = cosmos.getUserById(id);

        CosmosPagedIterable<AuctionDAO> auctions;
        if(!user.iterator().hasNext()){
            throw new WebApplicationException("User not found", 404);
        } else {
            auctions = cosmosAuction.getAuctionsOfUser(id);
        }
        //put auctions in list
        List<Auction> auctionList = new ArrayList<>();
        for(AuctionDAO auction : auctions){
            auctionList.add(new Auction(auction.getId(), auction.getTitle(), auction.getDescription(),
                    auction.getImageId(), auction.getOwnerId(), auction.getEndTime().toString(), auction.getMinPrice(), auction.getWinnerId(), auction.getStatus()));
        }
        return auctionList;

    }

    //authenticate user

    @POST
    @Path("/auth")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response auth(Login login) throws NoSuchAlgorithmException, JsonProcessingException {
        String user = login.getUser();
        String pwd = login.getPwd();
        if(user == null || user.equals("") || pwd == null || pwd.equals(""))
            return Response.status(NO_CONTENT).entity("Invalid user login").build();
        initCache();
        CosmosPagedIterable<UserDAO> userDB = cosmos.getUserByNickname(user);
        if(!userDB.iterator().hasNext()){
            return Response.status(NOT_FOUND).entity("User not found").build();
        }
        UserDAO u = userDB.iterator().next();
        //hash password
        MessageDigest messageDigest = MessageDigest.getInstance(HASHCODE);
        messageDigest.update(pwd.getBytes());
        String passHashed = new String(messageDigest.digest());

        if(!u.getPwd().equals(passHashed)){
            return Response.status(FORBIDDEN).entity("Wrong password").build();
        }

        String uid = UUID.randomUUID().toString();
        NewCookie cookie = new NewCookie.Builder("scc:session")
                .value(uid)
                .path("/")
                .comment("sessionid")
                .maxAge(3600)
                .secure(false)
                .httpOnly(true)
                .build();

        Session s = new Session(uid, user);
        ObjectMapper mapper = new ObjectMapper();
        jedis.set("user:" + u.getId(), mapper.writeValueAsString(s));

        return Response.ok().cookie(cookie).build();

    }
}