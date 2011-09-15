package models;

import siena.Generator;
import siena.Id;
import siena.Max;
import siena.Model;
import siena.Table;
import siena.core.lifecycle.PostDelete;
import siena.core.lifecycle.PostFetch;
import siena.core.lifecycle.PostInsert;
import siena.core.lifecycle.PostSave;
import siena.core.lifecycle.PostUpdate;
import siena.core.lifecycle.PreDelete;
import siena.core.lifecycle.PreFetch;
import siena.core.lifecycle.PreInsert;
import siena.core.lifecycle.PreSave;
import siena.core.lifecycle.PreUpdate;

@Table("discoveries_lifecycle")
public class DiscoveryLifeCycle extends Model {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Max(100)
	public String name;
	
	public DiscoveryLifeCycle(String name) {
		this.name = name;
	}
	
	public DiscoveryLifeCycle() {
	}

	@PreFetch
	private void preFetch() {
		System.out.println("preFetch");
	}

	@PostFetch
	private void postFetch() {
		System.out.println("postFetch");
	}
	
	@PreInsert
	private void preInsert() {
		System.out.println("preInsert");
	}

	@PostInsert
	private void postInsert() {
		System.out.println("postInsert");
	}

	@PreDelete
	private void preDelete() {
		System.out.println("preDelete");
	}

	@PostDelete
	private void postDelete() {
		System.out.println("postDelete");
	}

	@PreUpdate
	private void preUpdate() {
		System.out.println("preUpdate");
	}

	@PostUpdate
	private void postUpdate() {
		System.out.println("postUpdate");
	}

	@PreSave
	private void preSave() {
		System.out.println("preSave");
	}

	@PostSave
	private void postSave() {
		System.out.println("postSave");
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass())
			return false;

		DiscoveryLifeCycle other = (DiscoveryLifeCycle) obj;
		
		if(other.name != null && other.name.equals(name))
			return true;
		
		return false;
	}
	
	public boolean isOnlyIdFilled() {
		if(this.id != null 
			&& this.name == null
		) return true;
		return false;
	}
	
	public String toString() {
		return "Discovery [ id:"+id+" - name:"+name+" ]";
	}
}
