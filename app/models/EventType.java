package models;

import play.*;
import play.data.validation.Required;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class EventType extends Model {
    
	@Required(message="You have to complete the event type's name.")
	public String name;

	@Override
	public String toString() {
		return name;
	}
	
}
