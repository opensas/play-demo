package controllers;

import models.DiscoveryLifeCycle;
import play.mvc.Controller;


public class Lifecycle extends Controller {
	public static void index() {
		DiscoveryLifeCycle d = new DiscoveryLifeCycle("name1");
		
		d.insert();
		d.name = "name2";
		d.update();
		d.name = "name3";
		d.save();
		d.get();
		d.delete();
		
		render();
	}

}