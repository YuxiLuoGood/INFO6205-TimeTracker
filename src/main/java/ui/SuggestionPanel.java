package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

import model.DailySummary;
import model.Project;
import service.AIService;
import service.CalendarService;
import service.SummaryService;

/**
 * SuggestionPanel is the right-hand panel of the main window.
 * It contains two sections:
 *
 *   1. AI habit suggestions — retrieves the last 7 days of data from
 *      SummaryService and sends it to Ollama for analysis. The HTTP request
 *      runs in a SwingWorker background thread to keep the UI responsive.
 *
 *   2. Google Calendar sync — pushes the DailySummary for a selected date
 *      to the user's Google Calendar as an all-day event.
 *
 * The Calendar date field is kept in sync with the History panel's date
 * filter via setCalendarDate(), which is called by MainWindow.
 */
public class SuggestionPanel extends JPanel {

    private final AIService       aiService;
    private final CalendarService calendarService;
    private final SummaryService  summaryService;

    // AI section controls
    private JTextArea suggestionArea;
    private JButton   refreshBtn;
    private JLabel    statusLabel;

    // Calendar section controls
    private JTextField dateField;
    private JButton    syncBtn;
    private JLabel     syncStatusLabel;

    /**
     * Called by MainWindow when the user changes the date filter in HistoryPanel.
     * Keeps the Calendar sync date field in sync with the history view.
     *
     * @param date the date currently shown in HistoryPanel
     */
    public void setCalendarDate(LocalDate date) {
        if (date != null) dateField.setText(date.toString());
    }

    public SuggestionPanel(AIService aiService,
                           CalendarService calendarService,
                           SummaryService summaryService) {
        this.aiService       = aiService;
        this.calendarService = calendarService;
        this.summaryService  = summaryService;

        initComponents();
        layoutComponents();
    }

    // ── Component initialisation ──────────────────────────────

    /**
     * Creates and configures all controls for both the AI and Calendar sections.
     * If CalendarService failed to initialise (missing credentials.json),
     * the sync button is disabled and a status message is shown.
     */
    private void initComponents() {
        // AI suggestion area — read-only, word-wrapped
        suggestionArea = new JTextArea();
        suggestionArea.setEditable(false);
        suggestionArea.setLineWrap(true);
        suggestionArea.setWrapStyleWord(true);
        suggestionArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        suggestionArea.setText("Click \"Get suggestions\" to analyse your time habits.");

        refreshBtn  = new JButton("Get suggestions");
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        statusLabel.setForeground(Color.GRAY);
        refreshBtn.addActionListener(e -> onGetSuggestions());

        // Calendar section
        dateField = new JTextField(LocalDate.now().toString(), 12);
        syncBtn   = new JButton("Sync to Google Calendar");
        syncStatusLabel = new JLabel(" ");
        syncStatusLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        syncStatusLabel.setForeground(Color.GRAY);

        // Disable sync if OAuth credentials are not available
        if (!calendarService.isAvailable()) {
            syncBtn.setEnabled(false);
            syncStatusLabel.setText("Google Calendar not configured.");
        }

        syncBtn.addActionListener(e -> onSync());
    }

    // ── Layout ────────────────────────────────────────────────

    /**
     * Arranges the AI panel (top, resizable) and Calendar panel (bottom, fixed height)
     * inside a vertical JSplitPane with 75% weight given to the AI section.
     */
    private void layoutComponents() {
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // AI suggestion section
        JPanel aiPanel = new JPanel(new BorderLayout(4, 6));
        aiPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "AI habit suggestions",
                TitledBorder.LEFT, TitledBorder.TOP));

        JPanel aiTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        aiTop.add(refreshBtn);
        aiTop.add(statusLabel);

        aiPanel.add(aiTop,                           BorderLayout.NORTH);
        aiPanel.add(new JScrollPane(suggestionArea), BorderLayout.CENTER);

        // Calendar sync section
        JPanel calPanel = new JPanel(new BorderLayout(4, 6));
        calPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Google Calendar sync",
                TitledBorder.LEFT, TitledBorder.TOP));
        calPanel.setPreferredSize(new Dimension(0, 130));

        JPanel calForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        calForm.add(new JLabel("Date (yyyy-MM-dd):"));
        calForm.add(dateField);
        calForm.add(syncBtn);

        calPanel.add(calForm,         BorderLayout.CENTER);
        calPanel.add(syncStatusLabel, BorderLayout.SOUTH);

        // Stack AI on top, Calendar below
        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, aiPanel, calPanel);
        split.setResizeWeight(0.75);
        split.setBorder(null);

        add(split, BorderLayout.CENTER);
    }

    // ── Button actions ────────────────────────────────────────

    /**
     * Retrieves the last 7 days of DailySummary data and the sorted project list,
     * then sends them to AIService in a background thread.
     * The button is disabled while the request is in flight to prevent double-clicks.
     * If no data exists yet, a prompt is shown instead of calling the API.
     */
    private void onGetSuggestions() {
        refreshBtn.setEnabled(false);
        statusLabel.setText("Analysing...");
        suggestionArea.setText("");

        List<DailySummary> recent = summaryService.getRecentDays(7);
        List<Project>      sorted = summaryService.getSortedProjects();

        if (recent.isEmpty()) {
            suggestionArea.setText("No data yet. Start tracking your time first!");
            refreshBtn.setEnabled(true);
            statusLabel.setText(" ");
            return;
        }

        // Run the HTTP request off the Event Dispatch Thread
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return aiService.getSuggestions(recent, sorted);
            }

            @Override
            protected void done() {
                try {
                    suggestionArea.setText(get());
                    statusLabel.setText("Last updated: " + LocalDate.now());
                } catch (Exception ex) {
                    suggestionArea.setText("Error: " + ex.getMessage());
                    statusLabel.setText(" ");
                }
                refreshBtn.setEnabled(true);
            }
        }.execute();
    }

    /**
     * Validates the date input, retrieves the corresponding DailySummary
     * from MyHashTable via SummaryService, and pushes it to Google Calendar
     * in a background thread.
     * Shows a red error message if the date is invalid or has no tracked data.
     * Shows a green success message with the event link on success.
     */
    private void onSync() {
        String input = dateField.getText().trim();
        LocalDate date;
        try {
            date = LocalDate.parse(input);
        } catch (DateTimeParseException ex) {
            syncStatusLabel.setForeground(Color.RED);
            syncStatusLabel.setText("Invalid date format. Use yyyy-MM-dd.");
            return;
        }

        DailySummary summary = summaryService.getDailySummary(date);
        if (summary == null) {
            syncStatusLabel.setForeground(Color.RED);
            syncStatusLabel.setText("No data for " + date + ".");
            return;
        }

        syncBtn.setEnabled(false);
        syncStatusLabel.setForeground(Color.GRAY);
        syncStatusLabel.setText("Syncing...");

        // Run the Google Calendar API call off the Event Dispatch Thread
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return calendarService.syncDay(summary);
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    syncStatusLabel.setForeground(
                            result.startsWith("Synced") ? new Color(0, 120, 0) : Color.RED);
                    syncStatusLabel.setText(result);
                } catch (Exception ex) {
                    syncStatusLabel.setForeground(Color.RED);
                    syncStatusLabel.setText("Error: " + ex.getMessage());
                }
                syncBtn.setEnabled(calendarService.isAvailable());
            }
        }.execute();
    }
}