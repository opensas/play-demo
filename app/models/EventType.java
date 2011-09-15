package models;

import play.data.validation.Required;
import play.modules.siena.EnhancedModel;
import siena.Generator;
import siena.Id;

public class EventType extends EnhancedModel {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Required(message="You have to complete the event type's name.")
	public String name;

	@Override
	public String toString() {
		return name;
	}
	
	public static EventType findById(Long id) {
		return all().filter("id", id).get();
	}
	
}
