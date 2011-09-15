package lib.utils;

import java.util.Date;

import org.joda.time.Period;

public class DateHelper {

	public static String dateDiff(final Date begin, final Date end)  {
		
		final Period p = new Period(begin.getTime(), end.getTime());
	
		String message =
			    addTime(p.getYears(), "year") +
			    addTime(p.getMonths(), "month") +
			    addTime(p.getDays() + (p.getWeeks()*7), "day") +
			    addTime(p.getHours(), "hour") +
			    addTime(p.getMinutes(), "minute") +
			    addTime(p.getSeconds(), "second");
		
		message = message.replaceAll(", $", "");
		
		return message;
	}

	private static String addTime(final Integer time, final String period, final String periods) {
		if (time==0) return "";
		return String.valueOf(time) + " " + (time==1 ? period : periods) + ", ";
	}
	private static String addTime(final Integer time, final String period) {
		return addTime(time, period, period + "s");
	}
	
	// non instantiable class
	private DateHelper() {
		throw new AssertionError();
	}
}