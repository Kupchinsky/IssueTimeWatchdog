package ru.killer666.issuetimewatchdog.helper;

import java.util.Calendar;
import java.util.Date;

public class MyDateUtils {

    public static Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTime();
    }

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
    
}
