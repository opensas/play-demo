package controllers;

import java.util.Date;
import java.util.List;

import lib.jobs.BootstrapJob;
import models.Event;
import models.EventType;
import play.data.validation.Valid;
import play.mvc.Before;
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
    
    public static void form(Long id) {
    	final Event event;
    	if (id==null) {
    		event = new Event();
    	} else {
    		event = Event.findById(id);
    	}
    	render(event);
    }

    public static void save(@Valid Event event) {
    	if (validation.hasErrors()) {
    		render("@form", event);
    	}
    	event.save();
    	flash.success("event successfully saved!");
    	list();
    }

    public static void nextEvent() {
    	Event nextEvent = Event.find("date > ? order by date", new Date()).first();
    	render(nextEvent);
    }
    
    public static void loadFromYamlFile() {
    	new BootstrapJob().doJob();
    	list();
    }
    
	@SuppressWarnings("unused")
	@Before
	private static void loadEventTypes() {
		renderArgs.put("types", EventType.find("order by name").fetch());
	}
    
}
