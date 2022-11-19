package scc.Database;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import scc.Data.DAO.AuctionDAO;

import java.sql.Timestamp;
import java.util.Date;

public class CosmosAuctionDBLayer {
    private static final String CONNECTION_URL = System.getenv("COSMOSDB_URL");
    private static final String DB_KEY = System.getenv("COSMOSDB_KEY");
    private static final String DB_NAME = System.getenv("COSMOSDB_DATABASE");
    //create database layer to create and update auctions


    private static CosmosAuctionDBLayer instance;

    public static synchronized CosmosAuctionDBLayer getInstance() {
        if (instance != null) {
            return instance;
        }

        CosmosClient client = new CosmosClientBuilder()
                .endpoint(CONNECTION_URL)
                .key(DB_KEY)
                //.directMode()
                .gatewayMode()
                // replace by .directMode() for better performance
                .consistencyLevel(ConsistencyLevel.SESSION)
                .connectionSharingAcrossClientsEnabled(true)
                .contentResponseOnWriteEnabled(true)
                .buildClient();

        instance = new CosmosAuctionDBLayer(client);
        return instance;
    }

    private CosmosClient client;
    private CosmosDatabase db;
    private CosmosContainer auctions;

    public CosmosAuctionDBLayer(CosmosClient client) {
        this.client = client;
    }

    private synchronized void init() {
        if (db != null) {
            return;
        }
        db = client.getDatabase(DB_NAME);
        auctions = db.getContainer("auction");
    }

    public CosmosPagedIterable<AuctionDAO> getAuctionsById(String id){
        init();
        return auctions.queryItems("SELECT * FROM auctions a WHERE a.id = '" + id + "'", new CosmosQueryRequestOptions(), AuctionDAO.class);
    }

    public CosmosItemResponse<AuctionDAO> putAuction(AuctionDAO auction) {
        init();
        return auctions.createItem(auction);
    }

    public CosmosItemResponse<AuctionDAO> updateAuction(AuctionDAO auction) {
        init();
        return auctions.upsertItem(auction);
    }

    public  CosmosPagedIterable<AuctionDAO> getAuctionById(String id){
        init();
        return auctions.queryItems("SELECT * FROM auctions a WHERE a.id = '" + id + "'", new CosmosQueryRequestOptions(), AuctionDAO.class);
    }

    public CosmosPagedIterable<AuctionDAO> getAuctions(){
        init();
        return auctions.queryItems("SELECT * FROM auctions", new CosmosQueryRequestOptions(), AuctionDAO.class);
    }

    public CosmosPagedIterable<AuctionDAO> getAuctionsOfUser(String id){
        init();
        //get all auctions where the user is the seller

        return auctions.queryItems("SELECT * FROM auctions a WHERE a.ownerId = '" + id + "'", new CosmosQueryRequestOptions(), AuctionDAO.class);
    }

    public void close() {
        client.close();
    }


    public CosmosPagedIterable<AuctionDAO> getAuctionsToClose() {
        init();
        //get all auctions that are less than 1 hour away from  and are not closed


        Timestamp timestamp = new Timestamp(System.currentTimeMillis()+3600000);
        return auctions.queryItems("SELECT * FROM c WHERE c.endTime < " + timestamp.getTime() + " AND c.status = 'OPEN'", new CosmosQueryRequestOptions(), AuctionDAO.class);

        }
}
