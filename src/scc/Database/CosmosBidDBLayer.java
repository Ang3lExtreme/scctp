package scc.Database;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import scc.Data.DAO.BidDAO;

public class CosmosBidDBLayer {
    private static final String CONNECTION_URL = "";
    private static final String DB_KEY = "";
    private static final String DB_NAME = "";


    private static CosmosBidDBLayer instance;

    public static synchronized CosmosBidDBLayer getInstance() {
        if( instance != null)
            return instance;

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
        instance = new CosmosBidDBLayer( client);
        return instance;

    }
    private CosmosClient client;
    private CosmosDatabase db;
    private CosmosContainer bids;

    public CosmosBidDBLayer(CosmosClient client) {
        this.client = client;
    }

    private synchronized void init() {
        if( db != null)
            return;
        db = client.getDatabase(DB_NAME);
        bids = db.getContainer("bids");
    }

    public CosmosItemResponse<BidDAO> putBid(BidDAO bid) {
        init();
        return bids.createItem(bid);
    }

    public CosmosPagedIterable<BidDAO> getBids() {
        init();
        return bids.queryItems("SELECT * FROM bids", new CosmosQueryRequestOptions(), BidDAO.class);

    }
    public void close() {
        client.close();
    }

}
