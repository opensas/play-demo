/**
 * 
 */
package models;

import siena.Column;
import siena.Filter;
import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;


/**
 * @author pascal
 *
 */
public class Thing extends Model {
    @Id(Generator.AUTO_INCREMENT)
    public Long id;
    
    private String name;
    
    @Column("boss")
    private Thing parentThing;
    
    @Filter("parentThing")
    private Query<Thing> things;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Thing getParentThing() {
		return parentThing;
	}

	public void setParentThing(Thing parentThing) {
		this.parentThing = parentThing;
	}

	public Query<Thing> getThings() {
		return things;
	}

	public void setThings(Query<Thing> things) {
		this.things = things;
	}
    
    
}
