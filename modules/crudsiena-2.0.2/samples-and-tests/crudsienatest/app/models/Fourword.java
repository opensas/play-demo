package models;

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;

public class Fourword extends Model {
	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	public String test;

	public static Fourword getFourword(Long id2) {
		return all().getByKey(id2);
	}
	
	public String toString() {
		return "id:"+this.id+" - test:"+this.test;
	}
	
	public static Query<Fourword> all() {
		return Model.all(Fourword.class);
	}
}
