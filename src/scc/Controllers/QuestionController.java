package scc.Controllers;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/rest/auction/{id}/question")
public class QuestionController {
    //create question and list all questions for auction

    @POST
    @Path("/create")
    public void createQuestion() {
        //create question
    }

    @GET()
    @Path("/list")
    public void listQuestions() {
        //list questions
    }
}
