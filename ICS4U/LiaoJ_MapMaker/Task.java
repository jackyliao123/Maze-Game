// Jacky Liao
// December 4, 2017
// Map maker
// ICS4U Ms.Strelkovska

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Task {
	public static void main(String[] args) {

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

		TimeZone tz = TimeZone.getTimeZone("America/Toronto");

		Calendar currentTime = Calendar.getInstance(tz);

		Calendar first = Calendar.getInstance(tz);
		first.set(Calendar.HOUR_OF_DAY, 15);
		first.set(Calendar.MINUTE, 0);
		first.set(Calendar.SECOND, 0);
		first.set(Calendar.MILLISECOND, 0);

		if(first.before(currentTime)) {
			first.add(Calendar.DAY_OF_YEAR, 1);
		}

		scheduler.scheduleAtFixedRate(new Runnable() {
			public void run() {
				System.out.println("Run!");
			}
		}, first.getTimeInMillis() - currentTime.getTimeInMillis(), 86400000, TimeUnit.MILLISECONDS);
	}
}
