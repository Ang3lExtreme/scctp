package scc.Controllers;



import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import scc.Data.DAO.AuctionDAO;
import scc.Data.DTO.Auction;
import scc.Database.CosmosAuctionDBLayer;

@Path("/rest/auction")
public class AuctionController {
    private static final String CONNECTION_URL = "";
    private static final String DB_KEY = "";
    //create controller to create and update auctions
    CosmosClient cosmosClient = new CosmosClientBuilder()
            .endpoint(CONNECTION_URL)
            .key(DB_KEY)
            .buildClient();

    CosmosAuctionDBLayer cosmos =  new CosmosAuctionDBLayer(cosmosClient);

    @POST()
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CosmosItemResponse<AuctionDAO> createAuction(Auction auction){
        //create a AuctionDAO object
        AuctionDAO au = new AuctionDAO(auction.getAuctionId(), auction.getTitle(), auction.getDescription(),
                auction.getImageId(), auction.getOwnerId(), auction.getEndTime(), auction.getMinPrice());

        CosmosItemResponse<AuctionDAO> response = cosmos.putAuction(au);
        return response;
    }

    @PUT()
    @Path("/update/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CosmosItemResponse<AuctionDAO> updateAuction(@PathParam("id") String id, Auction auction){
        //update auction
        CosmosPagedIterable<AuctionDAO> aucDB = cosmos.getAuctionById(id);
        if (aucDB == null) {
            throw new NotFoundException();
        }

        AuctionDAO au = new AuctionDAO(auction.getAuctionId(), auction.getTitle(), auction.getDescription(),
                auction.getImageId(), auction.getOwnerId(), auction.getEndTime(), auction.getMinPrice(), auction.getWinnerId(),auction.getStatus());

        CosmosItemResponse<AuctionDAO> response = cosmos.putAuction(au);
        return response;

    }


}