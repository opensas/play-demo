import java.util.Date;

import lib.utils.DateHelper;

import org.joda.time.DateTime;
import org.junit.Test;

import play.test.UnitTest;

public class DateDiffTest extends UnitTest {

    @Test
    public void dateDiffTest() {

    	assertEquals("1 second", DateHelper.dateDiff(
    			new DateTime(2011, 1, 1, 10, 30, 0, 0).toDate(), 
    			new DateTime(2011, 1, 1, 10, 30, 1, 0).toDate()
    	));
    	
    	assertEquals("10 seconds", DateHelper.dateDiff(
    			new DateTime(2011, 1, 1, 10, 30, 0, 0).toDate(), 
    			new DateTime(2011, 1, 1, 10, 30, 10, 0).toDate()
    	));

    	assertEquals("5 minutes, 30 seconds", DateHelper.dateDiff(
    			new DateTime(2011, 1, 1, 10, 30, 0, 0).toDate(), 
    			new DateTime(2011, 1, 1, 10, 35, 30, 0).toDate()
    	));

    	assertEquals("-5 minutes, -30 seconds", DateHelper.dateDiff(
    			new DateTime(2011, 1, 1, 10, 35, 30, 0).toDate(),
    			new DateTime(2011, 1, 1, 10, 30, 0, 0).toDate()
    	));

    	assertEquals("1 year, 2 months, 5 days, 3 hours, 15 minutes, 5 seconds", DateHelper.dateDiff(
    			new DateTime(2011, 1, 1, 10, 30, 0, 0).toDate(), 
    			new DateTime(2012, 3, 6, 13, 45, 5, 0).toDate()
    	));

    	assertEquals("5 years, 5 months, 5 days, 5 hours, 5 minutes, 5 seconds", DateHelper.dateDiff(
    			new DateTime(2011, 1, 1, 10, 30, 10, 0).toDate(), 
    			new DateTime(2016, 6, 6, 15, 35, 15, 0).toDate()
    	));

    	//singular form
    	assertEquals("1 year, 1 month, 1 day, 1 hour, 1 minute, 1 second", DateHelper.dateDiff(
    			new DateTime(2011, 1, 1, 10, 30, 10, 0).toDate(), 
    			new DateTime(2012, 2, 2, 11, 31, 11, 0).toDate()
    	));
    	
    }

}
