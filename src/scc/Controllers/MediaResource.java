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
@Path("/media")
public class MediaResource
{
	Map<String,byte[]> map = new HashMap<String,byte[]>();
	String storageConnectionString = System.getenv("BlobStoreConnection");
	BlobContainerClient containerClientImages = new BlobContainerClientBuilder()
                .connectionString(storageConnectionString)
                .containerName("images")
                .buildClient();

	BlobContainerClient containerClientVideos = new BlobContainerClientBuilder()
			.connectionString(storageConnectionString)
			.containerName("images")
			.buildClient();
	BlobClient blob;


	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public Response.Status UploadImages(byte[] data){
		String filename = Hash.of(data);
		blob = containerClientImages.getBlobClient(filename);
		blob.upload(BinaryData.fromBytes(data));
		return Response.Status.ACCEPTED;

	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] DownloadImages(@PathParam("id") String filename){
		blob = containerClientImages.getBlobClient(filename);

		//if blob does not exist throw error
		if(!blob.exists()){
			throw new WebApplicationException("Image does not exist", 404);
		}

		BinaryData data = blob.downloadContent();
		byte[] arr = data.toBytes();
		return arr;
}







}
