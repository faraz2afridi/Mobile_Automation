package utilities;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtility {
        public static String getPreviousMonths(int months) {
            // Define the date format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);

            // Get the current date
            LocalDate currentDate = LocalDate.now();

            // Calculate the date 5 months back
            LocalDate dateFiveMonthsBack = currentDate.minusMonths(months);

            // Return the formatted date 5 months back
            return dateFiveMonthsBack.format(formatter);
        }
}