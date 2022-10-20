package scc.Controllers;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import scc.Data.DAO.UserDAO;
import scc.Data.DTO.User;
import scc.Database.CosmosUserDBLayer;
import scc.utils.Hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Path("/rest/user")
public class UserController {
    private static final String CONNECTION_URL = "https://scc51162.documents.azure.com:443/";
    private static final String DB_KEY = "oGgO8ROa9AhuIjtiosedfGKBjUZzHQ351Jv9TKVP3HFv5iqnyPC8SV2OiWE1aZggOwhA9Qo67oCgECTspO7OPg==";
    private static final String DB_NAME = "scc51162db";
    private static final String HASHCODE = "SHA-256";

    CosmosClient cosmosClient = new CosmosClientBuilder()
            .endpoint(CONNECTION_URL)
            .key(DB_KEY)
            .buildClient();

    CosmosUserDBLayer cosmos = new CosmosUserDBLayer(cosmosClient);

    @POST()
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CosmosItemResponse<UserDAO> writeUser(User user) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(HASHCODE);
        messageDigest.update(user.getPwd().getBytes());
        String passHashed = new String(messageDigest.digest());

        UserDAO u = new UserDAO(user.getId(), user.getName(), passHashed, user.getPhotoId(),null);

        CosmosItemResponse<UserDAO> response = cosmos.putUser(u);

        return response;

    }

    @GET()
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public CosmosPagedIterable<UserDAO> getUser(@PathParam("id") String id) {
        CosmosPagedIterable<UserDAO> user = cosmos.getUserById(id);

        if(user == null){
            throw new NotFoundException();
        }
        return user;
    }

    @DELETE()
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public CosmosPagedIterable<UserDAO> delUser(@PathParam("id") String id) {
        CosmosPagedIterable<UserDAO> user = cosmos.getUserById(id);

        if(user == null){
            throw new NotFoundException();
        }
        //else delete user
        CosmosItemResponse<Object> response = cosmos.delUserById(id);
        return user;
    }

    @PUT()
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
      public CosmosItemResponse<UserDAO> updateUser(@PathParam("id") String id, User user) throws NoSuchAlgorithmException {
            CosmosPagedIterable<UserDAO> userDB = cosmos.getUserById(id);

            if(userDB == null){
                throw new NotFoundException();
            }
            //else update user
            Hash messageDigest = new Hash();
            String passHashed = new String(messageDigest.digest(user.getPwd().getBytes()));

            UserDAO u = new UserDAO(user.getId(), user.getName(), user.getNickname(),passHashed, user.getPhotoId());

            CosmosItemResponse<UserDAO> response = cosmos.putUser(u);

            return response;
        }


}