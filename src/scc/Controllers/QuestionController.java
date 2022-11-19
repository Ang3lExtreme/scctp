package scc.Controllers;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import scc.Data.DAO.AuctionDAO;
import scc.Data.DAO.QuestionsDAO;
import scc.Data.DAO.UserDAO;
import scc.Data.DTO.Questions;
import scc.Data.DTO.Reply;
import scc.Database.CosmosAuctionDBLayer;
import scc.Database.CosmosQuestionsDBLayer;
import scc.Database.CosmosUserDBLayer;

import java.util.ArrayList;
import java.util.List;

@Path("/auction/{id}/question")
public class QuestionController {
    //create question and list all questions for auction
    private static final String CONNECTION_URL =  System.getenv("COSMOSDB_URL");
    private static final String DB_KEY = System.getenv("COSMOSDB_KEY");
    //create controller to create and update auctions
    CosmosClient cosmosClient = new CosmosClientBuilder()
            .endpoint(CONNECTION_URL)
            .key(DB_KEY)
            .buildClient();

    CosmosQuestionsDBLayer cosmos =  new CosmosQuestionsDBLayer(cosmosClient);
    CosmosAuctionDBLayer cosmosAuction = new CosmosAuctionDBLayer(cosmosClient);

    CosmosUserDBLayer cosmosUser = new CosmosUserDBLayer(cosmosClient);
    @PathParam("id")
    private String id;

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Questions createQuestion(Questions question){
        //if auction does not exist throw error
        CosmosPagedIterable<AuctionDAO> auction = cosmosAuction.getAuctionById(id);
        if(!auction.iterator().hasNext()){
            throw new WebApplicationException("Auction does not exist", 404);
        }

        //if auction is closed or deleted throw error
        if(auction.iterator().next().getStatus().equals("CLOSED") || auction.iterator().next().getStatus().equals("DELETED")){
            throw new WebApplicationException("Auction is closed or deleted", 409);
        }
        //if user does not exist throw error
        CosmosPagedIterable<UserDAO> user = cosmosUser.getUserById(question.getUserId());
        if(!user.iterator().hasNext()){
            throw new WebApplicationException("User does not exist", 404);
        }

        //if question already exists throw error
        CosmosPagedIterable<QuestionsDAO> questions = cosmos.getQuestionById(question.getUserId(),question.getAuctionId(),question.getId());
        if(questions.iterator().hasNext()){
            throw new WebApplicationException("Question already exists", 409);
        }

        //create question
        QuestionsDAO qu = new QuestionsDAO(question.getId(),question.getAuctionId(), question.getUserId(), question.getMessage());
        CosmosItemResponse<QuestionsDAO> response = cosmos.putQuestion(qu);
       return question;
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Questions> listQuestions() {
        //if auction does not exist return 404
        CosmosPagedIterable<AuctionDAO> auction = cosmosAuction.getAuctionById(id);

        if(!auction.iterator().hasNext()){
            throw new WebApplicationException("Auction does not exist", 404);
        }

        //list all questions using cosmos
        List<Questions> questions = new ArrayList<>();
        CosmosPagedIterable<QuestionsDAO> questionsDAO = cosmos.getQuestions(id);
        for (QuestionsDAO q : questionsDAO) {
            questions.add(new Questions(q.getId(),q.getAuctionId(), q.getUserId(), q.getMessage()));
        }
        return questions;

    }

    @POST
    @Path("/{QuestionId}/reply")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Questions replyQuestion(@PathParam("QuestionId") String questionId, Reply reply){
       //TODO: reply to question

        //if auction does not exist return 404
        CosmosPagedIterable<AuctionDAO> auction = cosmosAuction.getAuctionById(id);
        if(!auction.iterator().hasNext()){
            throw new WebApplicationException("Auction does not exist", 404);
        }
        //if question does not exist return 404
        AuctionDAO auctionDAO = auction.iterator().next();
        CosmosPagedIterable<QuestionsDAO> question = cosmos.getQuestionById(auctionDAO.getOwnerId(),id,questionId);
        if(!question.iterator().hasNext()){
            throw new WebApplicationException("Question does not exist", 404);
        }
        //if question already has a reply return 409
        QuestionsDAO questionDAO = question.iterator().next();
        if(questionDAO.getReply() != null){
            throw new WebApplicationException("Question already has a reply", 409);
        }

        questionDAO.setReply(reply.getReply());
        CosmosItemResponse<QuestionsDAO> response = cosmos.replyQuestion(questionDAO);
        return new Questions(questionDAO.getId(),questionDAO.getAuctionId(), questionDAO.getUserId(), questionDAO.getMessage(), questionDAO.getReply());

    }
}
