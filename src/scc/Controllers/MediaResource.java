package scc.Controllers;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import scc.utils.Hash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.MediaType;

/**
 * Resource for managing media files, such as images.
 */
@Path("/rest/media")
public class MediaResource
{
	Map<String,byte[]> map = new HashMap<String,byte[]>();
	String storageConnectionString = "";
	BlobContainerClient containerClient = new BlobContainerClientBuilder()
                .connectionString(storageConnectionString)
                .containerName("images")
                .buildClient();
	BlobClient blob;


	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public Response.Status Upload(byte[] data){
		String filename = Hash.of(data);
		blob = containerClient.getBlobClient(filename);
		blob.upload(BinaryData.fromBytes(data));
		return Response.Status.ACCEPTED;
		//devolver string
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] Download(@PathParam("id") String filename){
		blob = containerClient.getBlobClient(filename);

		BinaryData data = blob.downloadContent();
		byte[] arr = data.toBytes();
		return arr;
}





}
