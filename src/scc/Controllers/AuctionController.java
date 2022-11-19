package scc.Controllers;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import scc.Data.DAO.AuctionDAO;
import scc.Data.DAO.UserDAO;
import scc.Data.DTO.Auction;
import scc.Data.DTO.Session;
import scc.Data.DTO.Status;
import scc.Database.CosmosAuctionDBLayer;
import scc.Database.CosmosUserDBLayer;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

//testing
@Path("/auction")
public class AuctionController {

    private static final String CONNECTION_URL = System.getenv("COSMOSDB_URL");
    private static final String DB_KEY = System.getenv("COSMOSDB_KEY");

    CosmosClient cosmosClient = new CosmosClientBuilder()
            .endpoint(CONNECTION_URL)
            .key(DB_KEY)
            .buildClient();

    CosmosAuctionDBLayer cosmos =  new CosmosAuctionDBLayer(cosmosClient);
    CosmosUserDBLayer cosmosUser = new CosmosUserDBLayer(cosmosClient);

    @POST()
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Auction createAuction(Auction auction){
        //create a AuctionDAO object
        AuctionDAO au = new AuctionDAO(auction.getAuctionId(), auction.getTitle(), auction.getDescription(),
                auction.getImageId(), auction.getOwnerId(), auction.getEndTime().toString(), auction.getMinPrice());

        //if time is past, return null
        if(au.getEndTime().before(new Date())){
            throw new WebApplicationException("Cannot create auction in this time" , 409);
        }

        CosmosPagedIterable<UserDAO> user = cosmosUser.getUserById(auction.getOwnerId());
        if(!user.iterator().hasNext()){
            throw new WebApplicationException("Owner does not exist", 404);
        }

        //if auction exists, return error
        CosmosPagedIterable<AuctionDAO> auctionDAO = cosmos.getAuctionById(auction.getAuctionId());
        if(auctionDAO.iterator().hasNext()){
            throw new WebApplicationException("Auction already exists", 409);
        }

        CosmosItemResponse<AuctionDAO> response = cosmos.putAuction(au);
        return auction;
    }

    @PUT()
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Auction updateAuction(@CookieParam("scc:session") Cookie session, @PathParam("id") String id, Auction auction){
        //update auction

        CosmosPagedIterable<AuctionDAO> aucDB = cosmos.getAuctionById(id);

        if(!aucDB.iterator().hasNext()){
            throw new WebApplicationException("Auction dont exists", 404);
        }

        AuctionDAO au = new AuctionDAO(auction.getAuctionId(), auction.getTitle(), auction.getDescription(),
                auction.getImageId(), auction.getOwnerId(), auction.getEndTime().toString(), auction.getMinPrice(), auction.getWinnerId(),auction.getStatus());

        AuctionDAO auc = aucDB.iterator().next();

        Session s = new Session();
        String res = s.checkCookieUser(session, auc.getOwnerId());
        if(!"ok".equals(res))
            throw new WebApplicationException(res, Response.Status.UNAUTHORIZED);

        verifyAuction(auc, auction);
        CosmosItemResponse<AuctionDAO> response = cosmos.updateAuction(au);

        return auction;

    }

    //get Auctions about to close
    @GET()
    @Path("/auctionsToClose")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Auction> getAuctionsToClose(){
        //get auctions to close
        CosmosPagedIterable<AuctionDAO> auctions = cosmos.getAuctionsToClose();
        List<Auction> auctionList = new ArrayList<>();
        for(AuctionDAO auction : auctions){
            auctionList.add(new Auction(auction.getId(), auction.getTitle(), auction.getDescription(),
                    auction.getImageId(), auction.getOwnerId(), auction.getEndTime().toString(), auction.getMinPrice(), auction.getWinnerId(), auction.getStatus()));
        }
        //convert to Auction
        return auctionList;


    }


    private void verifyAuction(AuctionDAO auctionToEdit, Auction edit){
        //if ownerid or winnerid dont exist, return error
        CosmosPagedIterable<UserDAO> user = cosmosUser.getUserById(edit.getOwnerId());
        if(!user.iterator().hasNext()){
            throw new WebApplicationException("Owner does not exist", 404);
        }
        user = cosmosUser.getUserById(edit.getWinnerId());
        if(!user.iterator().hasNext()){
            throw new WebApplicationException("Winner does not exist", 404);
        }
        //if auction if deleted, return error
        if(auctionToEdit.getStatus() == Status.DELETED){
            throw new WebApplicationException("Auction is deleted", 409);
        }
        //if auction is closed and status is OPEN, return error
        if(auctionToEdit.getStatus() == Status.CLOSED && edit.getStatus() == Status.OPEN){
            throw new WebApplicationException("Auction is closed", 409);
        }

        //if minprice is negative, return error
        if(edit.getMinPrice() < 0){
            throw new WebApplicationException("Min price is negative", 400);
        }

        //if edit.endtime is before now, return error
        if(edit.getEndTime().before(new Date())){
            throw new WebApplicationException("End time is before now", 400);
        }
        //if edit.id is different from auctionToEdit.id, return error
        if(!edit.getAuctionId().equals(auctionToEdit.getId())){
            throw new WebApplicationException("Auction id is different", 400);
        }

    }


}