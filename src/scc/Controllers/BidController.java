package scc.Controllers;

import com.azure.core.annotation.Get;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/rest/auction/{id}/bid")
public class BidController {
    //create Bid and list all bids for auction

    @POST
    @Path("/create")
    public void createBid() {
        //create bid
    }

    @GET()
    @Path("/list")
    public void listBids() {
        //list bids
    }
}
