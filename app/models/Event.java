package models;

import java.util.Date;

import javax.persistence.ManyToOne;

import lib.utils.DateHelper;
import play.data.validation.Required;
import play.modules.siena.EnhancedModel;
import siena.Column;
import siena.Generator;
import siena.Id;

public class Event extends EnhancedModel {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Required(message="You have to complete the event's name.")
	public String name;
	
	@Required(message="You have to select the type of the event.")
	@Column("type")
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
	
	public static Event findById(Long id) {
		return all().filter("id", id).get();
	}
	
	@Override
	public void get() {
		super.get();
		type = EventType.findById(type.id);
	}
	
}
