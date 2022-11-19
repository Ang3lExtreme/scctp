package scc.serverless;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import redis.clients.jedis.Jedis;
import scc.Data.DAO.AuctionDAO;
import scc.Data.DTO.Status;
import scc.Database.CosmosAuctionDBLayer;
import scc.cache.RedisCache;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TimerFunction {

	@FunctionName("CloseExpiredAuctions")
	//excecute every 5 minutes
	public void closeAuctions(@TimerTrigger(name = "timerInfo", schedule = "0 */5 * * * *") String timerInfo) {
		//look for every auction that has expired and change its status to closed
		//get all
		CosmosClient cosmosClient = new CosmosClientBuilder()
				.endpoint(System.getenv("COSMOSDB_URL"))
				.key(System.getenv("COSMOSDB_KEY"))
				.buildClient();

		CosmosAuctionDBLayer cosmos =  new CosmosAuctionDBLayer(cosmosClient);
		CosmosPagedIterable<AuctionDAO> auctions = cosmos.getAuctions();
		//check if auction has expired
		//if expired change status to closed
		//update auction
		int counter = 0;
		for(AuctionDAO auction : auctions){
			//parse AuctionDAO to Auction
			if(auction.getEndTime().before(new Date())){
				auction.setStatus(Status.CLOSED);
				cosmos.updateAuction(auction);
				counter++;
			}
		}

		//update cache too
		//TODO

		System.out.println("Timer trigger function executed at: " + timerInfo);
		System.out.println("Closed " + counter + " auctions");
	}

	@FunctionName("GarbageCollector")
	//excecute every 60 minutes
	public void garbageCollector(@TimerTrigger(name = "timerInfo", schedule = "0 */60 * * * *") String timerInfo) {
		//free memory of java garbage collector
		System.gc();
		System.out.println("Garbage collector function executed at: " + timerInfo);

	}

	/*@FunctionName("periodic-compute")
	public void cosmosFunction( @TimerTrigger(name = "periodicSetTime",
			schedule = "30 1 ")
								String timerInfo,
								ExecutionContext context) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			jedis.incr("cnt:timer");
			jedis.set("serverless-time", new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z").format(new Date()));
		}
	}*/

	@FunctionName("SyncStorage")
	//excecute every 5 minutes
	public void syncStorage(@TimerTrigger(name ="syncStorage", schedule = "schedule = \"0 */5 * * * *\"") String timerInfo){
		//every 5 minutes sync storage with blob storage in europe and us
		//TODO
		Map<String,byte[]> map = new HashMap<String,byte[]>();
		String storageConnectionStringEurope = System.getenv("BlobStoreConnectionEurope");

		BlobContainerClient containerClientImagesEurope = new BlobContainerClientBuilder()
				.connectionString(storageConnectionStringEurope)
				.containerName("images")
				.buildClient();

		String storageConnectionStringUs = System.getenv("BlobStoreConnectionUs");
		BlobContainerClient containerClientImagesUs = new BlobContainerClientBuilder()
				.connectionString(storageConnectionStringUs)
				.containerName("images")
				.buildClient();


		BlobClient blobEurope;
		BlobClient blobUs;

	for (String blobName : containerClientImagesEurope.listBlobs().stream().map(blobItem -> blobItem.getName()).toList()) {
			blobEurope = containerClientImagesEurope.getBlobClient(blobName);
			blobUs = containerClientImagesUs.getBlobClient(blobName);
			if(blobUs.exists()){
				//blob exists in both storages
				//check if they are the same
				if(blobEurope.getProperties().getBlobSize() != blobUs.getProperties().getBlobSize()){
					//they are not the same
					//download the one with the biggest size
					if(blobEurope.getProperties().getBlobSize() > blobUs.getProperties().getBlobSize()){
						//download blob from europe
						blobUs.upload(blobEurope.openInputStream(),blobEurope.getProperties().getBlobSize());
					}else{
						//download blob from us
						blobEurope.upload(blobUs.openInputStream(),blobUs.getProperties().getBlobSize());
					}
				}
			}else{
				//blob exists only in europe
				//upload to us
				blobUs.upload(blobEurope.openInputStream(),blobEurope.getProperties().getBlobSize());
			}
		}

		for (String blobName : containerClientImagesUs.listBlobs().stream().map(blobItem -> blobItem.getName()).toList()) {
			blobEurope = containerClientImagesEurope.getBlobClient(blobName);
			blobUs = containerClientImagesUs.getBlobClient(blobName);
			if(!blobEurope.exists()){
				//blob exists only in us
				//upload to europe
				blobEurope.upload(blobUs.openInputStream(),blobUs.getProperties().getBlobSize());
			}
		}


	}



}
