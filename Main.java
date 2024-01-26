import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class Main {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.ENGLISH);

    public static void main(String[] args) {
        String filePath = "Assignment_Timecard.csv";
        try {
            analyzeData(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void analyzeData(String filePath) throws ParseException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
                PrintWriter writer = new PrintWriter("output.txt")) {
            writer.println("Employee Name,Position,Consecutive Days Worked,Hours Between Shifts,Single Shift Duration");

            String line;
            // Skip the header row
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] data = line.split(","); // Use comma as the delimiter
                if (data.length < 8 || data[2].isEmpty() || data[3].isEmpty()) {
                    // Skip lines with missing or empty date fields
                    continue;
                }

                String name = data[7];
                String position = data[1];
                Date startTime = parseDate(data[2]);
                Date endTime = parseDate(data[3]);

                if (startTime == null || endTime == null) {
                    // Skip lines with invalid date formats
                    continue;
                }

                analyzeConsecutiveDays(name, position, startTime, br, writer);
                analyzeTimeBetweenShifts(name, position, endTime, writer);
                analyzeSingleShiftDuration(name, position, startTime, endTime, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Date parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty() || dateString.equalsIgnoreCase("Time")) {
            // Return null for invalid or empty date values
            return null;
        }

        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace(); // Print the exception for debugging
            return null;
        }
    }

    private static void analyzeConsecutiveDays(String name, String position, Date startTime, BufferedReader br,
            PrintWriter writer) throws IOException, ParseException {
        if (startTime == null) {
            System.out.println("Skipping entry due to missing or empty date field: " + name);
            return;
        }

        Date nextDay = addDays(startTime, 1);
        String nextLine;
        boolean consecutiveDaysWorked = false;

        while ((nextLine = br.readLine()) != null) {
            String[] nextData = nextLine.split(",");
            if (nextData.length >= 4 && !nextData[2].isEmpty() && !nextData[3].isEmpty()
                    && !nextData[3].equals("0:00")) {
                String nextName = nextData[7];
                Date nextStartTime = dateFormat.parse(nextData[2]);

                if (nextName.equals(name) && isSameDay(nextStartTime, nextDay)) {
                    consecutiveDaysWorked = true;
                    nextDay = addDays(nextDay, 1);
                } else {
                    break;
                }
            }
        }

        if (consecutiveDaysWorked) {
            writer.println(name + " (Position: " + position + ") worked for 7 consecutive days.");
        }
    }

    private static void analyzeTimeBetweenShifts(String name, String position, Date endTime, PrintWriter writer)
            throws IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader("Assignment_Timecard.csv"));

        try {
            String nextLine;
            while ((nextLine = br.readLine()) != null) {
                String[] nextData = nextLine.split(",");
                if (nextData.length < 8) {
                    System.out.println(
                            "Skipping entry due to missing or empty date fields: " + Arrays.toString(nextData));
                    continue;
                }

                String nextName = nextData[7];
                Date nextStartTime = parseDate(nextData[2]);

                if (nextName.equals(name) && nextStartTime != null) {
                    long hoursBetweenShifts = (nextStartTime.getTime() - endTime.getTime()) / (60 * 60 * 1000);

                    if (hoursBetweenShifts > 1 && hoursBetweenShifts < 10) {
                        writer.println(name + " (Position: " + position + ") has less than 10 hours between shifts.");
                    }
                    break;
                }
            }
        } finally {
            br.close();
        }
    }

    private static void analyzeSingleShiftDuration(String name, String position, Date startTime, Date endTime,
            PrintWriter writer) {
        long hoursWorked = (endTime.getTime() - startTime.getTime()) / (60 * 60 * 1000);

        if (hoursWorked > 14) {
            String message = name + " (Position: " + position + ") worked for more than 14 hours in a single shift.";
            System.out.println(message); // Print to console for verification
            writer.println(message); // Write to the output file
        }
    }

    private static Date addDays(Date date, int days) {
        // Helper method to add days to a date
        return new Date(date.getTime() + days * 24 * 60 * 60 * 1000);
    }

    private static boolean isSameDay(Date date1, Date date2) {
        // Helper method to check if two dates are on the same day
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        return fmt.format(date1).equals(fmt.format(date2));
    }
}