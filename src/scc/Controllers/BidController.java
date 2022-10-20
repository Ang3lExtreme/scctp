package scc.Controllers;

import com.azure.core.annotation.Get;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import scc.Data.DAO.BidDAO;
import scc.Data.DAO.QuestionsDAO;
import scc.Data.DTO.Bid;
import scc.Data.DTO.Questions;
import scc.Database.CosmosBidDBLayer;
import scc.Database.CosmosQuestionsDBLayer;

@Path("/rest/auction/{id}/bid")
public class BidController {
    @PathParam("id")
    private String id;
    //create Bid and list all bids for auction
    private static final String CONNECTION_URL = System.getenv("COSMOS_URL");
    private static final String DB_KEY = System.getenv("COSMOSDB_KEY");

    CosmosClient cosmosClient = new CosmosClientBuilder()
            .endpoint(CONNECTION_URL)
            .key(DB_KEY)
            .buildClient();

    CosmosBidDBLayer cosmos =  new CosmosBidDBLayer(cosmosClient);


    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CosmosItemResponse<BidDAO> createBid(Bid bid) {
        //create bid
        BidDAO b = new BidDAO(bid.getAuctionId(), bid.getUserId(), bid.getValue());
        CosmosItemResponse<BidDAO> response = cosmos.putBid(b);
        return response;
    }

    @GET()
    @Path("/list")
    public Bid[] listBids() {
        //list bids
        CosmosPagedIterable<BidDAO> bids = cosmos.getBids(id);
        if (bids == null) {
            throw new NotFoundException();
        }
        Bid[] bidsList = new Bid[bids.stream().toArray().length];
        int i = 0;
        for (BidDAO b : bids) {
            bidsList[i] = new Bid(b.getAuctionId(), b.getUserId(), b.getValue());
            i++;
        }
        return bidsList;
    }
}
