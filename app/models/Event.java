package models;

import play.*;
import play.data.validation.Required;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

import lib.utils.DateHelper;

@Entity
public class Event extends Model {

	@Required(message="You have to complete the event's name.")
	public String name;
	
	@Required(message="You have to select the type of the event.")
	@ManyToOne
	public EventType type;
	
	@Required(message="You have to complete the event's place.")
	public String place;
	
	@Required(message="You have to complete the event's date.")
	public Date date;

	@Override
	public String toString() {
		return name;
	}

	public String countDown() {
		return DateHelper.dateDiff(new Date(), date);
	}
	
}
