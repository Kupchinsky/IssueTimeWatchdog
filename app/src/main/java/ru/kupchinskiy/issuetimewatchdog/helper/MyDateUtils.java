package ru.kupchinskiy.issuetimewatchdog.helper;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MyDateUtils {

    public static Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public static boolean isCurrentMonth(Date date) {
        Calendar calendarDate = Calendar.getInstance();
        calendarDate.setTime(date);

        Calendar calendar = Calendar.getInstance();

        return (calendar.get(Calendar.YEAR) == calendarDate.get(Calendar.YEAR))
                && (calendar.get(Calendar.MONTH) == calendarDate.get(Calendar.MONTH));
    }

    private static Map<TimeUnit, Long> computeDiff(Date date1, Date date2, List<TimeUnit> units) {
        long diffInMillies = date2.getTime() - date1.getTime();

        Map<TimeUnit, Long> result = new LinkedHashMap<>();

        long milliesRest = diffInMillies;

        for (TimeUnit unit : units) {
            long diff = unit.convert(milliesRest, TimeUnit.MILLISECONDS);
            long diffInMilliesForUnit = unit.toMillis(diff);
            milliesRest = milliesRest - diffInMilliesForUnit;
            result.put(unit, diff);
        }

        return result;
    }

    public static String getTimeDifference(Date date1, Date date2, List<TimeUnit> timeUnits) {
        Map<TimeUnit, Long> dateDiff = computeDiff(date1, date2, timeUnits);
        String result = "";

        for (Map.Entry<TimeUnit, Long> entry : dateDiff.entrySet()) {
            if (entry.getValue() != 0) {
                result += entry.getValue() + " ";

                switch (entry.getKey()) {
                    case DAYS:
                        result += "d.";
                        break;
                    case HOURS:
                        result += "h.";
                        break;
                    case MINUTES:
                        result += "m.";
                        break;
                    case SECONDS:
                        result += "s.";
                        break;
                }

                result += " ";
            }
        }

        return result.trim();
    }

}
