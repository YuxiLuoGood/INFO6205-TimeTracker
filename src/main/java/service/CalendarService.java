package service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import model.DailySummary;

public class CalendarService {

    private static final String APP_NAME         = "Time Tracker";
    private static final String CREDENTIALS_FILE = "credentials.json";
    private static final String TOKENS_DIR       = "tokens";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private Calendar calendarClient;

    public CalendarService() {
        try {
            NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential      = authorize(transport);
            calendarClient = new Calendar.Builder(transport, JSON_FACTORY, credential)
                    .setApplicationName(APP_NAME)
                    .build();
        } catch (Exception e) {
            System.err.println("Google Calendar init failed: " + e.getMessage());
            calendarClient = null;
        }
    }

    // Main entrance 

    /**
     * Aggregate and sync the events for a specific day into a single all-day event in Google Calendar
     * Called in a SwingWorker background thread
     *
     * @param summary Retrieved from SummaryService.getDailySummary(date)
     * @return Returns the event link on success; returns an error message on failure
     */
    public String syncDay(DailySummary summary) {
        if (calendarClient == null) return "Google Calendar not initialized.";
        if (summary == null)        return "No data for this date.";

        try {
            Event event = buildEvent(summary);
            Event created = calendarClient.events()
                    .insert("primary", event)
                    .execute();
            return "Synced: " + created.getHtmlLink();
        } catch (IOException e) {
            return "Sync failed: " + e.getMessage();
        }
    }

    /**
     * Checks whether CalendarService has initialized successfully
     * This can be used in the GUI to determine whether to display the Sync button
     */
    public boolean isAvailable() {
        return calendarClient != null;
    }

    // Event Construct

    private Event buildEvent(DailySummary summary) {
        LocalDate date = summary.getDate();

        // title
        String title = "Time Tracker: " + date;

        // Description: List the duration of all items
        StringBuilder desc = new StringBuilder();
        desc.append("Total: ").append(formatDuration(summary.getTotalDuration())).append("\n\n");

        Set<String> projects = summary.getProjectNames();
        for (String project : projects) {
            long dur = summary.getDurationForProject(project);
            desc.append("- ").append(project)
                .append(": ").append(formatDuration(dur)).append("\n");
        }

        // All-day events: Both start and end times must be in date format
        Date startDate = Date.from(
                date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate   = Date.from(
                date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        com.google.api.client.util.DateTime start =
                new com.google.api.client.util.DateTime(true, startDate.getTime(), 0);
        com.google.api.client.util.DateTime end =
                new com.google.api.client.util.DateTime(true, endDate.getTime(), 0);

        return new Event()
                .setSummary(title)
                .setDescription(desc.toString())
                .setStart(new EventDateTime().setDate(start))
                .setEnd(new EventDateTime().setDate(end));
    }

    // OAuth 2.0 Authorize

    private Credential authorize(NetHttpTransport transport) throws IOException {
        // Read credentials.json (downloaded from the Google Cloud Console)
        File credFile = new File(CREDENTIALS_FILE);
        if (!credFile.exists()) {
            throw new FileNotFoundException(
                    "credentials.json not found. Download it from Google Cloud Console.");
        }

        try (InputStream in = new FileInputStream(credFile)) {
            GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY,
                    new InputStreamReader(in));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    transport, JSON_FACTORY, secrets,
                    Collections.singletonList(CalendarScopes.CALENDAR))
                    .setDataStoreFactory(
                            new FileDataStoreFactory(new File(TOKENS_DIR)))
                    .setAccessType("offline")
                    .build();

            // The first time it runs, it will open a browser window to prompt the user for authorization;
            // afterward, the token is stored in the `tokens/` directory.
            LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                    .setPort(8888)
                    .build();
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        }
    }

    // Tools method

    private String formatDuration(long totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        if (h > 0) return h + "h " + m + "m";
        return m + "m";
    }
}