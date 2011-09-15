package models;

import play.modules.siena.EnhancedModel;
import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Table;
import siena.embed.Embedded;
import siena.embed.EmbeddedList;


public class City extends EnhancedModel {

	@Id
	public long id;

	public String name;

	@Embedded
	public String[] stations;

	public City(String name, String[] stations) {
		this.name = name;
		this.stations = stations;
	}
}