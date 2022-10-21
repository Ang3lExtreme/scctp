package scc.Controllers;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import scc.Data.DAO.QuestionsDAO;
import scc.Data.DTO.Questions;
import scc.Database.CosmosQuestionsDBLayer;

@Path("/auction/{id}/question")
public class QuestionController {
    //create question and list all questions for auction
    private static final String CONNECTION_URL = "https://scc23tp1.documents.azure.com:443/";
    private static final String DB_KEY = "YpAeFIibJ97KY37FQk8j8iarptqtylUdh8rwtaU5DMc7IlDhZdzFlbt5Z7ZKr81ZkLFyv0JSK3rheRhdIcFZIw==";
    //create controller to create and update auctions
    CosmosClient cosmosClient = new CosmosClientBuilder()
            .endpoint(CONNECTION_URL)
            .key(DB_KEY)
            .buildClient();

    CosmosQuestionsDBLayer cosmos =  new CosmosQuestionsDBLayer(cosmosClient);
    @PathParam("id")
    private String id;

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CosmosItemResponse<QuestionsDAO> createQuestion(Questions question){
        //create question
        QuestionsDAO qu = new QuestionsDAO(question.getAuctionId(), question.getUserId(), question.getMessage());
        CosmosItemResponse<QuestionsDAO> response = cosmos.putQuestion(qu);
        return response;
    }

    @GET
    @Path("/list")
    public Questions[] listQuestions() {
        //list all questions using cosmos
        CosmosPagedIterable<QuestionsDAO> questions = cosmos.getQuestions(id);
        if (questions == null) {
            throw new NotFoundException();
        }
        Questions[] questionsList = new Questions[questions.stream().toArray().length];
        int i = 0;
        for (QuestionsDAO q : questions) {
            questionsList[i] = new Questions(q.getAuctionId(), q.getUserId(), q.getMessage());
            i++;
        }
        return questionsList;



    }
}
