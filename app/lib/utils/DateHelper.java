package lib.utils;

import java.util.Date;

import org.joda.time.Period;

import play.i18n.Messages;

public class DateHelper {

	public static String dateDiff(final Date begin, final Date end)  {
		
		final Period p = new Period(begin.getTime(), end.getTime());
	
		String message =
			    addTime(p.getYears(), "dateHelper.year") +
			    addTime(p.getMonths(), "dateHelper.month") +
			    addTime(p.getDays() + (p.getWeeks()*7), "dateHelper.day") +
			    addTime(p.getHours(), "dateHelper.hour") +
			    addTime(p.getMinutes(), "dateHelper.minute") +
			    addTime(p.getSeconds(), "dateHelper.second");
		
		message = message.replaceAll(", $", "");
		
		return message;
	}

	private static String addTime(final Integer time, final String period, final String periods) {
		if (time==0) return "";
		
		String periodLocalized = Messages.get(period);
		String periodsLocalized = Messages.get(periods);
		
		return String.valueOf(time) + " " + (time==1 ? periodLocalized : periodsLocalized) + ", ";
	}
	private static String addTime(final Integer time, final String period) {
		return addTime(time, period, period + "s");
	}
	
	// non instantiable class
	private DateHelper() {
		throw new AssertionError();
	}
}