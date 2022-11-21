package scc.serverless;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import scc.Data.DAO.AuctionDAO;
import scc.Data.DAO.BidDAO;
import scc.Data.DTO.Status;
import scc.Database.CosmosAuctionDBLayer;
import scc.Database.CosmosBidDBLayer;


import java.util.Date;

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

	@FunctionName("DefineAuctionWinner")
	//excecute every 5 minutes
	public void defineAuctionWinner(@TimerTrigger(name = "timerInfo", schedule = "0 */5 * * * *") String timerInfo) {
		//look for every auction that has expired and change its status to closed
		//get all
		CosmosClient cosmosClient = new CosmosClientBuilder()
				.endpoint(System.getenv("COSMOSDB_URL"))
				.key(System.getenv("COSMOSDB_KEY"))
				.buildClient();

		CosmosAuctionDBLayer cosmos =  new CosmosAuctionDBLayer(cosmosClient);
		CosmosBidDBLayer cosmosBid = new CosmosBidDBLayer(cosmosClient);
		CosmosPagedIterable<AuctionDAO> auctions = cosmos.getAuctions();
		//for all auctions that are closed check all bids of the auction, and the one with the highest value is the winner
		//update auction
		int counter = 0;
		for(AuctionDAO auction : auctions){
			//parse AuctionDAO to Auction
			if(auction.getStatus() == Status.CLOSED){

				//get all bids of auction
				//get highest bid
				//set winner
				//update auction
				counter++;
				CosmosPagedIterable<BidDAO> bis = cosmosBid.getBids(auction.getId());
				BidDAO winner = null;
				for(BidDAO bid : bis){
					if(winner == null || bid.getValue() > winner.getValue()){
						winner = bid;
					}
				}
				if(winner != null){
					auction.setWinnerId(winner.getUserId());
					cosmos.updateAuction(auction);
					System.out.println("Auction " + auction.getId() + " has a winner");
				}
				System.out.println("Auction " + auction.getId() + " has no winner");

			}
		}
	}

	@FunctionName("GarbageCollector")
	//excecute every 60 minutes
	public void garbageCollector(@TimerTrigger(name = "timerInfo", schedule = "0 */60 * * * *") String timerInfo) {
		//free memory of java garbage collector
		System.gc();
		System.out.println("Garbage collector function executed at: " + timerInfo);

	}

	@FunctionName("SyncStorage")
	//excecute every 5 minutes
	public void syncStorage(@TimerTrigger(name = "timerInfo", schedule = "0 */5 * * * *") String timerInfo){
		//every 5 minutes sync storage with blob storage in europe and us
		//TODO
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


		for(BlobItem blob : containerClientImagesEurope.listBlobs()){
			blobEurope = containerClientImagesEurope.getBlobClient(blob.getName());
			blobUs = containerClientImagesUs.getBlobClient(blob.getName());
			if(blobUs.exists()){
				//if blob exists in us, check if it is the same
				if(blobUs.getProperties().getBlobSize() != blobEurope.getProperties().getBlobSize()){
					//if not the same, delete the one in us and upload the one in europe
					blobUs.delete();
					blobUs.upload(blobEurope.openInputStream(), blobEurope.getProperties().getBlobSize());
				}
			}else{
				//if blob does not exist in us, upload it
				blobUs.upload(blobEurope.openInputStream(), blobEurope.getProperties().getBlobSize());
			}
		}

		//for every blob in us, check if it exists in europe and upload it if it does not
		for(BlobItem blob : containerClientImagesUs.listBlobs()){
			blobUs = containerClientImagesUs.getBlobClient(blob.getName());
			blobEurope = containerClientImagesEurope.getBlobClient(blob.getName());
			if(!blobEurope.exists()){
				blobEurope.upload(blobUs.openInputStream(), blobUs.getProperties().getBlobSize());
			}
		}

		System.out.println("Sync storage function executed at: " + timerInfo);

	}



}
