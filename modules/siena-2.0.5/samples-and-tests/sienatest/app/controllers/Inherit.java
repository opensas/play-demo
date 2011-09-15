package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Employee;
import models.PersonLongAutoIDExtendedAbstract;
import models.TransactionAccountFromModel;
import models.TransactionAccountToModel;
import play.cache.Cache;
import play.mvc.Controller;
import siena.Model;
import siena.SienaException;


public class Inherit extends Controller {
	public static void index() {
		PersonLongAutoIDExtendedAbstract bob = 
			new PersonLongAutoIDExtendedAbstract("Bob", "Doe", "Oklahoma", 1, "the_dog1");
		bob.save();
		
		// fetches it to be sure it has been saved
		bob = 
			Model.getByKey(PersonLongAutoIDExtendedAbstract.class, bob.id);
		
		render(bob);
	}


}