package scc.Controllers;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import scc.Data.DAO.AuctionDAO;
import scc.Data.DAO.UserDAO;
import scc.Data.DTO.Auction;
import scc.Data.DTO.Login;
import scc.Data.DTO.Session;
import scc.Data.DTO.User;
import scc.Database.CosmosAuctionDBLayer;
import scc.Database.CosmosUserDBLayer;
import scc.utils.Hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Path("/user")
public class UserController {
    private static final String CONNECTION_URL = System.getenv("COSMOSDB_URL");
    private static final String DB_KEY = System.getenv("COSMOSDB_KEY");
    private static final String HASHCODE = "SHA-256";


    //endpoint cannot be null
    CosmosClient cosmosClient = new CosmosClientBuilder()
            .endpoint(CONNECTION_URL)
            .key(DB_KEY)
            .buildClient();

    CosmosUserDBLayer cosmos = new CosmosUserDBLayer(cosmosClient);
    CosmosAuctionDBLayer cosmosAuction = new CosmosAuctionDBLayer(cosmosClient);

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
    public Response auth(Login user) throws NoSuchAlgorithmException {
        CosmosPagedIterable<UserDAO> userDB = cosmos.getUserByNickname(user.getUser());
        if(!userDB.iterator().hasNext()){
            throw new WebApplicationException("User not found", 404);
        }
        UserDAO u = userDB.iterator().next();
        //hash password
        MessageDigest messageDigest = MessageDigest.getInstance(HASHCODE);
        messageDigest.update(user.getPwd().getBytes());
        String passHashed = new String(messageDigest.digest());

        if(!u.getPwd().equals(passHashed)){
            throw new WebApplicationException("Wrong password", 401);
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

        //  RedisLayer.getInstance().putSession(new Session(uid, user.getUser()));
        return Response.ok().cookie(cookie).build();

    }


}