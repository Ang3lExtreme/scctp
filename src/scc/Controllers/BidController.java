package scc.Controllers;

import com.azure.core.annotation.Get;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import redis.clients.jedis.Jedis;
import scc.Data.DAO.AuctionDAO;
import scc.Data.DAO.BidDAO;
import scc.Data.DAO.QuestionsDAO;
import scc.Data.DAO.UserDAO;
import scc.Data.DTO.Auction;
import scc.Data.DTO.Bid;
import scc.Data.DTO.Questions;
import scc.Data.DTO.Session;
import scc.Database.CosmosAuctionDBLayer;
import scc.Database.CosmosBidDBLayer;
import scc.Database.CosmosQuestionsDBLayer;
import scc.Database.CosmosUserDBLayer;
import scc.cache.RedisCache;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static scc.mgt.AzureManagement.USE_CACHE;

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

    private Jedis jedis;
    private synchronized void initCache() {
        if(jedis != null)
            return;
        jedis = RedisCache.getCachePool().getResource();
    }

    CosmosBidDBLayer cosmos =  new CosmosBidDBLayer(cosmosClient);
    CosmosAuctionDBLayer cosmosAuction = new CosmosAuctionDBLayer(cosmosClient);

    CosmosUserDBLayer cosmosUser = new CosmosUserDBLayer(cosmosClient);

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Bid createBid(@CookieParam("scc:session") Cookie session, Bid bid) throws JsonProcessingException {
        initCache();
        AuctionDAO auctionDAO;
        if(!bid.getAuctionId().equals(id)) {
            throw new WebApplicationException("Auction id does not match", 400);
        }
        //create bid
        BidDAO b = new BidDAO(bid.getId(),bid.getAuctionId(), bid.getUserId(), bid.getTime(),bid.getValue());

        Session s = new Session();
        String res = s.checkCookieUser(session, bid.getUserId());
        if(!"ok".equals(res))
            throw new WebApplicationException(res, Response.Status.UNAUTHORIZED);

        if(true) {   //!(USE_CACHE && jedis.exists("auc:" + id))
            //id auction dont exist
            CosmosPagedIterable<AuctionDAO> auction = cosmosAuction.getAuctionById(id);
            if (!auction.iterator().hasNext()) {
                throw new WebApplicationException("Auction does not exist", 404);
            }
            auctionDAO = auction.iterator().next();



        } else {
            String get = jedis.get("auc:" + id);
            ObjectMapper mapper = new ObjectMapper();
            auctionDAO = mapper.readValue(get, AuctionDAO.class);

        }

        if(!(USE_CACHE && jedis.exists("user:" + b.getUserId()))) {
            //check if user is exist
            CosmosPagedIterable<UserDAO> userDAO = cosmosUser.getUserById(b.getUserId());
            if (!userDAO.iterator().hasNext()) {
                throw new WebApplicationException("User does not exist", 404);
            }
        }

        //make AuctionDAO to AuctionDTO
       Auction auctionDTO = new Auction(auctionDAO.getId(), auctionDAO.getTitle(), auctionDAO.getDescription(),
               auctionDAO.getImageId(), auctionDAO.getOwnerId(), auctionDAO.getEndTime(), auctionDAO.getMinPrice(),"",auctionDAO.getStatus());

        if(!(USE_CACHE && jedis.exists("bid:" + bid.getId()))) {
            //if bid exists, return error
            CosmosPagedIterable<BidDAO> bidDAO = cosmos.getBidById(bid.getId(), bid.getAuctionId());
            if (bidDAO.iterator().hasNext()) {
                throw new WebApplicationException("Bid already exists", 409);
            }
        }

        verifyBid(auctionDTO,bid);

        CosmosItemResponse<BidDAO> response = cosmos.putBid(b);

        if(USE_CACHE) {
            ObjectMapper mapper = new ObjectMapper();
            jedis.set("bid:" + bid.getId(), mapper.writeValueAsString(bid));
        }

        return bid;
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
            bidList.add(new Bid(bid.getId(), bid.getAuctionId(), bid.getUserId(), bid.getTime(),bid.getValue()));
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

        if (lastBid == null) {
            return null;
        }

        Bid bid = new Bid(lastBid.getId(), lastBid.getAuctionId(), lastBid.getUserId(),new Date(),lastBid.getValue());
        return bid;

    }
}
