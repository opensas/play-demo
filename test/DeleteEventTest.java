import models.Event;

import org.junit.Before;
import org.junit.Test;

import play.db.jpa.JPA;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

public class DeleteEventTest extends FunctionalTest {

	@Before
	public void deleteModels() {
		Fixtures.deleteAllModels();
		Fixtures.loadModels("data-test.yml");
	}

    @Test
    public void deleteEventTest() {

    	final Long originalCount = Event.count();
    	final Event originalNextEvent = Event.all().order("-date").limit(1).get(); 
    	
        Response response = DELETE("/event/" + originalNextEvent.id); 
        assertStatus(302, response);
        assertHeaderEquals("Location", "/", response);
        
        assertEquals("There should be one less event", originalCount-1, Event.count());
        
        assertNotSame("The next event must have changed",
                originalNextEvent,
                Event.all().order("-date").limit(1).get()
        );

        final Event deletedEventByCondition = Event.findById(originalNextEvent.id);
        assertNull("The event should have been deleted", deletedEventByCondition);
    }
    
}