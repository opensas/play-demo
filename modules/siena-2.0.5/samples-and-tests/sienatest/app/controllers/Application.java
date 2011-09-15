package controllers;

import java.util.List;
import java.util.ArrayList;

import models.Employee;
import models.City;
import play.cache.Cache;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import siena.Model;
import siena.Query;

public class Application extends Controller {
	static Query<Employee> q;

	@Before
	public static void loadQuery() {
		q = (Query<Employee>) Cache.get("q");
		if (q == null) {
			// stateful is just meant to use GAE cursors instead of offsets
			q = Employee.all().stateful().paginate(100);
		}
	}

	@After
	public static void saveQuery() {
		Cache.set("q", q);
	}

	public static void index() {
		List<Employee> emps = q.fetch();

		renderTemplate("Application/list.html", emps);
	}

	public static void nextPage() {
		List<Employee> emps = q.nextPage().fetch();

		renderTemplate("Application/list.html", emps);
	}

	public static void previousPage() {
		List<Employee> emps = q.previousPage().fetch();

		renderTemplate("Application/list.html", emps);
	}

	public static void tryCities() {
		List<City> cities = new ArrayList<City>();
		for(int i=0; i<100; i++){
			City city = new City("city"+i, new String[] { "alpha", "beta" });
			cities.add(city);
		}
		
		Model.batch(City.class).insert(cities);
		int nbBefore = Model.all(City.class).count();
		City.deleteAll();		
		int nbAfter = Model.all(City.class).count();		
		
		render(nbBefore, nbAfter);

	}
}