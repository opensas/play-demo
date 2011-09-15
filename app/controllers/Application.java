package controllers;

import java.util.Date;
import java.util.List;

import lib.jobs.BootstrapJob;
import models.Event;
import models.EventType;
import play.cache.Cache;
import play.data.validation.Valid;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

public class Application extends Controller {
	
    public static void list() {
    	Secure.loadUser();
    	final List<Event> events = Event.all().order("-date").fetch();
        render(events);
    }

    public static void delete(Long id) {
    	final Event event = Event.findById(id);
    	event.delete();
    	refreshNextEvent();
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
    	refreshNextEvent();
    	flash.success("event successfully saved!");
    	list();
    }

    public static void nextEvent() {
    	final Event nextEvent = getNextEvent();
    	render(nextEvent);
    }
    
    public static void loadFromYamlFile() {
    	new BootstrapJob().doJob();
    	refreshNextEvent();
    	list();
    }
    
	@SuppressWarnings("unused")
	@Before
	private static void loadEventTypes() {
		renderArgs.put("types", EventType.all().order("name").fetch());
	}
    
	private static Event getNextEvent() {
		Event nextEvent = (Event) Cache.get("nextEvent");
		if (nextEvent==null) {
			nextEvent = refreshNextEvent();
		}
		return nextEvent;
	}
	
	private static Event refreshNextEvent() {
		final Event nextEvent = Event.all().filter("date>", new Date()).order("date").get();
		Cache.set("nextEvent", nextEvent);
		return nextEvent;
	}
	
}
