package scc.main;

import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.core.Application;
import scc.Controllers.*;

public class MainApplication extends Application
{
	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> resources = new HashSet<Class<?>>();

	public MainApplication() {
		resources.add(ControlResource.class);
		resources.add(MediaResource.class);
		resources.add(UserController.class);
		resources.add(BidController.class);
		resources.add(QuestionController.class);
		resources.add(AuctionController.class);
		singletons.add( new MediaResource());

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
