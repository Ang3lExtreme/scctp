package scc.Controllers;

import com.azure.core.annotation.Get;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import scc.Data.DAO.AuctionDAO;
import scc.Data.DAO.BidDAO;
import scc.Data.DAO.QuestionsDAO;
import scc.Data.DAO.UserDAO;
import scc.Data.DTO.Auction;
import scc.Data.DTO.Bid;
import scc.Data.DTO.Questions;
import scc.Database.CosmosAuctionDBLayer;
import scc.Database.CosmosBidDBLayer;
import scc.Database.CosmosQuestionsDBLayer;
import scc.Database.CosmosUserDBLayer;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Path("/auction/{id}/bid")
public class BidController {
    @PathParam("id")
    private String id;
    //create Bid and list all bids for auction
    private static final String CONNECTION_URL = System.getenv("COSMOSDB_URL");
    private static final String DB_KEY = System.getenv("COSMOSDB_KEY");
    CosmosClient cosmosClient = new CosmosClientBuilder()
            .endpoint(CONNECTION_URL)
            .key(DB_KEY)
            .buildClient();

    CosmosBidDBLayer cosmos =  new CosmosBidDBLayer(cosmosClient);
    CosmosAuctionDBLayer cosmosAuction = new CosmosAuctionDBLayer(cosmosClient);

    CosmosUserDBLayer cosmosUser = new CosmosUserDBLayer(cosmosClient);

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CosmosItemResponse<BidDAO> createBid(Bid bid) {
        //create bid
        BidDAO b = new BidDAO(bid.getId(),bid.getAuctionId(), bid.getUserId(), bid.getValue());

        //id auction dont exist
        CosmosPagedIterable<AuctionDAO> auction = cosmosAuction.getAuctionById(id);

        if(!auction.iterator().hasNext()){
            throw new WebApplicationException("Auction does not exist", 404);
        }

       auction = cosmosAuction.getAuctionById(b.getAuctionId());

        if(!auction.iterator().hasNext()){
            throw new WebApplicationException("Auction does not exist", 404);
        }

        //check if user is exist
        CosmosPagedIterable<UserDAO> auctionDAO = cosmosUser.getUserById(b.getUserId());
        if(!auctionDAO.iterator().hasNext()){
            throw new WebApplicationException("User does not exist", 404);
        }

        //make AuctionDAO to AuctionDTO
        Auction auctionDTO = new Auction(auction.iterator().next().getId(), auction.iterator().next().getTitle(), auction.iterator().next().getDescription(),
                auction.iterator().next().getImageId(), auction.iterator().next().getOwnerId(), auction.iterator().next().getEndTime().toString(), auction.iterator().next().getMinPrice());

        //if bid exists, return error
        CosmosPagedIterable<BidDAO> bidDAO = cosmos.getBidById(bid.getId(),bid.getAuctionId());
        if(bidDAO.iterator().hasNext()){
            throw new WebApplicationException("Bid already exists", 409);
        }

        verifyBid(auctionDTO,bid);

        CosmosItemResponse<BidDAO> response = cosmos.putBid(b);
        return response;
    }

    @GET()
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Bid> listBids() {
        CosmosPagedIterable<AuctionDAO> auction = cosmosAuction.getAuctionById(id);
        if(!auction.iterator().hasNext()){
            throw new WebApplicationException("Auction does not exist", 404);
        }
        //list bids
        CosmosPagedIterable<BidDAO> bids = cosmos.getBids(id);
        //if auction doesnt exist, return error

        List<Bid> bidList = new ArrayList<>();
        for (BidDAO bid : bids) {
            bidList.add(new Bid(bid.getId(), bid.getAuctionId(), bid.getUserId(), bid.getValue()));
        }
        return bidList;
    }

    private void verifyBid(Auction auction, Bid bid) {
        //if auction is closed or deleted, return error
        if(auction.getStatus().equals("CLOSED") || auction.getStatus().equals("DELETED")){
            throw new WebApplicationException("Auction is closed or deleted", 409);
        }
        //if bid is lower than min price, return error
        if(bid.getValue() < auction.getMinPrice()){
            throw new WebApplicationException("Bid is lower than min price", 409);
        }
        //if bid is lower than last bid, return error
        Bid lastBid = getLastBid();
        if(lastBid != null && bid.getValue() <= lastBid.getValue()){
            throw new WebApplicationException("Bid is lower or equal than last bid", 409);
        }

        //if auction has ended, return error
        if(auction.getEndTime().before(new Date())){
            throw new WebApplicationException("Auction has ended", 409);
        }

    }

   /* @GET()
    @Path("/last")
    @Produces(MediaType.APPLICATION_JSON)
    public Bid getLastBidd(String auctionId){
        //get last bid created transforming _ts to date
        CosmosPagedIterable<BidDAO> bids = cosmos.getBids(id);
        BidDAO lastBid = null;
        for (BidDAO bid : bids) {
            if(lastBid == null){
                lastBid = bid;
            }else{
                if(Integer.parseInt(bid.get_ts()) > Integer.parseInt(lastBid.get_ts())){
                    lastBid =bid;
                }
            }
        }

        Bid bid = new Bid(lastBid.getId(), lastBid.getAuctionId(), lastBid.getUserId(), lastBid.getValue());
        return bid;
    }*/

    //return bid that have lowest _ts
    private Bid getLastBid(){

        CosmosPagedIterable<BidDAO> bids = cosmos.getBids(id);
        BidDAO lastBid = null;
        for (BidDAO bid : bids) {
            if(lastBid == null){
                lastBid = bid;
            }else{
                if(Integer.parseInt(bid.get_ts()) > Integer.parseInt(lastBid.get_ts())){
                    lastBid =bid;
                }
            }
        }

        Bid bid = new Bid(lastBid.getId(), lastBid.getAuctionId(), lastBid.getUserId(), lastBid.getValue());
        return bid;

    }
}
