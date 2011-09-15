import org.junit.*;
import java.util.*;

import play.modules.siena.SienaFixtures;
import play.test.*;
import models.*;

public class LoadTest extends UnitTest {

	@Test
    public void loadEmployees() {
		SienaFixtures.loadModels("data1.yml");
		
		// Bob
		Employee bob = Employee.all(Employee.class).filter("firstName", "Bob").get();
		List<Employee> emps = new ArrayList(1000);
		for(int i=0; i<1000; i++){
			Employee newBob = new Employee(bob);
			newBob.firstName = "Bob_"+i;
			newBob.lastName = "Name_"+i;
			emps.add(newBob);
		}
        Employee.batch().insert(emps);
        
        List<Employee> emps2 = Employee.all().search("Bob_*", "firstName").fetch();
//        for(int i=0; i<1000; i++){
//        	Employee bob1 = emps.get(i);
//			Employee bob2 = emps2.get(i);
//			assertEquals(bob1.firstName, bob2.firstName);
//			assertEquals(bob1.lastName, bob2.lastName);
//		}
        assertEquals(1000, emps2.size());
	}


	
    @Before
    public void setUp() {
        SienaFixtures.deleteDatabase();
    }
}
