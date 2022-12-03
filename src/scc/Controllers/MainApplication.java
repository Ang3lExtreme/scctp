package scc.Controllers;

import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.core.Application;

public class MainApplication extends Application
{
	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> resources = new HashSet<Class<?>>();

	public MainApplication() {

		resources.add(AuctionController.class);
		resources.add(BidController.class);
		resources.add(ControlResource.class);
		resources.add(MediaResource.class);
		resources.add(QuestionController.class);
		resources.add(UserController.class);

		singletons.add(new AuctionController());
		singletons.add(new BidController());
		singletons.add(new ControlResource());
		singletons.add(new MediaResource());
		singletons.add(new QuestionController());
		singletons.add(new UserController());




	}

	@Override
	public Set<Class<?>> getClasses() {
		return resources;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}
