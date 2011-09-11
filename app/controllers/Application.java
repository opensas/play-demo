package controllers;

import java.util.List;

import lib.jobs.BootstrapJob;
import models.Event;
import play.mvc.Controller;

public class Application extends Controller {
	
    public static void list() {
    	List<Event> events = Event.find("order by date desc").fetch();
        render(events);
    }

    public static void delete(Long id) {
    	Event event = Event.findById(id);
    	event.delete();
    	list();
    }
    
    public static void loadFromYamlFile() {
    	new BootstrapJob().doJob();
    	list();
    }
    
}
