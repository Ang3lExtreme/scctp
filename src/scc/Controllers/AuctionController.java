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

//testing
@Path("/auction")
public class AuctionController {
    private static final String CONNECTION_URL = "https://scc23tp1.documents.azure.com:443/";
    private static final String DB_KEY = "YpAeFIibJ97KY37FQk8j8iarptqtylUdh8rwtaU5DMc7IlDhZdzFlbt5Z7ZKr81ZkLFyv0JSK3rheRhdIcFZIw==";
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

    //get Auctions about to close
    @GET()
    @Path("/auctionsToClose")
    @Produces(MediaType.APPLICATION_JSON)
    public CosmosPagedIterable<AuctionDAO> getAuctionsToClose(){
        //get auctions to close
        return cosmos.getAuctionsToClose();
    }



}