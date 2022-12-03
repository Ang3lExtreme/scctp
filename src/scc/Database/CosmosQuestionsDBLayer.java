package scc.Database;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import scc.Data.DAO.QuestionsDAO;

//same as CosmosBidDBLayer
public class CosmosQuestionsDBLayer {

    private static final String CONNECTION_URL = System.getenv("COSMOSDB_URL");
    private static final String DB_KEY = System.getenv("COSMOSDB_KEY");
    private static final String DB_NAME = System.getenv("COSMOSDB_DATABASE");


    private static CosmosQuestionsDBLayer instance;

    public static synchronized CosmosQuestionsDBLayer getInstance() {
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
        instance = new CosmosQuestionsDBLayer( client);
        return instance;

    }
    private CosmosClient client;
    private CosmosDatabase db;
    private CosmosContainer questions;

    public CosmosQuestionsDBLayer(CosmosClient client) {
        this.client = client;
    }

    private synchronized void init() {
        if( db != null)
            return;
        db = client.getDatabase(DB_NAME);
        questions = db.getContainer("questions");
    }

    public CosmosItemResponse<QuestionsDAO> putQuestion(QuestionsDAO question) {
        init();
        return questions.createItem(question);
    }



    public CosmosPagedIterable<QuestionsDAO> getQuestionById(String auctionId,String questionId) {
        init();
        return questions.queryItems("SELECT * FROM questions q WHERE q.auctionId = '" + auctionId + "' AND q.id = '" + questionId + "'", new CosmosQueryRequestOptions(), QuestionsDAO.class);
    }

    public CosmosPagedIterable<QuestionsDAO> getQuestions(String auctionId) {
        init();
        return questions.queryItems("SELECT * FROM questions q WHERE q.auctionId = '" + auctionId + "'", new CosmosQueryRequestOptions(), QuestionsDAO.class);
    }
    public void close() {
        client.close();
    }


    public CosmosItemResponse<QuestionsDAO> replyQuestion(QuestionsDAO qu) {
        init();
        return questions.upsertItem(qu);
    }
}
