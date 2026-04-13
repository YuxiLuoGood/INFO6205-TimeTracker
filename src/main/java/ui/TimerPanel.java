package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDate;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import model.Project;
import service.SummaryService;
import service.TimerService;

/**
 * TimerPanel is the top-left panel of the main window.
 * It provides three areas:
 *
 *   1. Project controls — dropdown to select a project, text field to add
 *      a new one, and a Remove button to delete the selected project.
 *
 *   2. Stopwatch — a large HH:MM:SS display updated every second by
 *      javax.swing.Timer, with Start and Stop buttons.
 *
 *   3. Ranking panel — shows the Top 5 projects by total duration for a
 *      selected date. Today's ranking uses MyHashTable via getTopNByDate;
 *      historical dates also use MyHashTable.
 */
public class TimerPanel extends JPanel {

    private final TimerService   timerService;
    private final SummaryService summaryService;
    private final MainWindow     mainWindow;

    private JComboBox<String> projectCombo;
    private JTextField        newProjectField;
    private JButton           addProjectBtn;
    private JButton           removeProjectBtn;
    private JLabel            timerLabel;
    private JButton           startBtn;
    private JButton           stopBtn;
    private JPanel            rankingPanel;
    private JTextField        rankingDateField;  // date filter for the ranking section

    private javax.swing.Timer swingTimer;        // fires every 1000ms to update the stopwatch

    public TimerPanel(TimerService timerService,
                      SummaryService summaryService,
                      MainWindow mainWindow) {
        this.timerService   = timerService;
        this.summaryService = summaryService;
        this.mainWindow     = mainWindow;

        initComponents();
        layoutComponents();
        initTimer();
        refreshProjectCombo();
    }

    // ── Component initialisation ──────────────────────────────

    /**
     * Creates and wires up all controls.
     * The ranking date field triggers refreshRanking() on Enter or focus loss.
     */
    private void initComponents() {
        projectCombo    = new JComboBox<>();
        newProjectField = new JTextField(12);
        newProjectField.putClientProperty("JTextField.placeholderText", "New project name");

        addProjectBtn    = new JButton("Add");
        removeProjectBtn = new JButton("Remove");
        addProjectBtn.addActionListener(e -> onAddProject());
        removeProjectBtn.addActionListener(e -> onRemoveProject());

        timerLabel = new JLabel("00:00:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 42));

        startBtn = new JButton("Start");
        stopBtn  = new JButton("Stop");
        stopBtn.setEnabled(false);
        startBtn.addActionListener(e -> onStart());
        stopBtn.addActionListener(e -> onStop());

        rankingPanel = new JPanel();
        rankingPanel.setLayout(new BoxLayout(rankingPanel, BoxLayout.Y_AXIS));

        rankingDateField = new JTextField(LocalDate.now().toString(), 10);
        rankingDateField.addActionListener(e -> refreshRanking());
        rankingDateField.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { refreshRanking(); }
        });
    }

    // ── Layout ────────────────────────────────────────────────

    /**
     * Arranges the three sections vertically:
     *   NORTH  — project selector row
     *   CENTER — stopwatch and Start/Stop buttons
     *   SOUTH  — ranking panel with date filter
     */
    private void layoutComponents() {
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Timer",
                TitledBorder.LEFT, TitledBorder.TOP));

        // Top row: project selection and management
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        topRow.add(new JLabel("Project:"));
        topRow.add(projectCombo);
        topRow.add(newProjectField);
        topRow.add(addProjectBtn);
        topRow.add(removeProjectBtn);

        // Centre: stopwatch display and Start/Stop buttons
        JPanel centerPanel = new JPanel(new BorderLayout(4, 8));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        centerPanel.add(timerLabel, BorderLayout.CENTER);
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.add(startBtn);
        btnRow.add(stopBtn);
        centerPanel.add(btnRow, BorderLayout.SOUTH);

        // Bottom: ranking with date filter and Today shortcut
        JButton rankTodayBtn = new JButton("Today");
        rankTodayBtn.addActionListener(e -> {
            rankingDateField.setText(LocalDate.now().toString());
            refreshRanking();
        });

        JPanel rankDateRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        rankDateRow.add(new JLabel("Date:"));
        rankDateRow.add(rankingDateField);
        rankDateRow.add(rankTodayBtn);

        JPanel rankingWrapper = new JPanel(new BorderLayout());
        rankingWrapper.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Ranking",
                TitledBorder.LEFT, TitledBorder.TOP));
        rankingWrapper.add(rankDateRow,  BorderLayout.NORTH);
        rankingWrapper.add(rankingPanel, BorderLayout.CENTER);

        add(topRow,         BorderLayout.NORTH);
        add(centerPanel,    BorderLayout.CENTER);
        add(rankingWrapper, BorderLayout.SOUTH);
    }

    // ── Swing timer ───────────────────────────────────────────

    /**
     * Initialises a javax.swing.Timer that fires every 1000ms on the EDT.
     * Each tick calls getElapsedSeconds() and updates the stopwatch label.
     * This avoids blocking the UI thread with a sleep loop.
     */
    private void initTimer() {
        swingTimer = new javax.swing.Timer(1000, e ->
                timerLabel.setText(formatTime(timerService.getElapsedSeconds())));
    }

    // ── Button actions ────────────────────────────────────────

    /**
     * Starts a timing session for the selected project.
     * Disables the Start button, project dropdown, and Remove button
     * while the timer is running to prevent conflicting actions.
     */
    private void onStart() {
        String project = (String) projectCombo.getSelectedItem();
        if (project == null || project.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select or create a project first.",
                    "No project", JOptionPane.WARNING_MESSAGE);
            return;
        }
        timerService.start(project);
        startBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        projectCombo.setEnabled(false);
        removeProjectBtn.setEnabled(false);
        swingTimer.start();
    }

    /**
     * Stops the current session, saves the TimeEntry, and refreshes the UI.
     * After stopping:
     *   - The last TimeEntry is passed to SummaryService.recordEntry() to update
     *     MyHashTable and MyPriorityQueue.
     *   - refreshRanking() updates the ranking panel.
     *   - MainWindow.refreshHistory() updates the history table.
     */
    private void onStop() {
        long duration = timerService.stop();
        swingTimer.stop();
        timerLabel.setText("00:00:00");
        startBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        projectCombo.setEnabled(true);
        removeProjectBtn.setEnabled(true);

        // Retrieve the TimeEntry just appended to the project's MyLinkedList
        String projectName = (String) projectCombo.getSelectedItem();
        Project project = timerService.getProjects().stream()
                .filter(p -> p.getName().equals(projectName))
                .findFirst().orElse(null);
        if (project != null && duration > 0) {
            List<model.TimeEntry> entries = project.getEntriesAsList();
            if (!entries.isEmpty()) {
                // Notify SummaryService to update HashTable and PriorityQueue
                summaryService.recordEntry(project, entries.get(entries.size() - 1));
            }
        }
        refreshRanking();
        mainWindow.refreshHistory();
    }

    /**
     * Adds a new project to the project list.
     * Checks for duplicates before calling TimerService.addProject().
     * Selects the new project in the dropdown after creation.
     */
    private void onAddProject() {
        String name = newProjectField.getText().trim();
        if (name.isEmpty()) return;
        if (timerService.getProjects().stream().anyMatch(p -> p.getName().equals(name))) {
            JOptionPane.showMessageDialog(this,
                    "Project \"" + name + "\" already exists.",
                    "Duplicate", JOptionPane.WARNING_MESSAGE);
            return;
        }
        timerService.addProject(name);
        newProjectField.setText("");
        refreshProjectCombo();
        projectCombo.setSelectedItem(name);
    }

    /**
     * Removes the selected project and all its time entries after confirmation.
     * Blocked while the timer is running to avoid data inconsistency.
     * Calls SummaryService.rebuildRanking() to remove the deleted project
     * from MyPriorityQueue and all DailySummary entries in MyHashTable.
     */
    private void onRemoveProject() {
        String selected = (String) projectCombo.getSelectedItem();
        if (selected == null || selected.isEmpty()) return;
        if (timerService.isRunning()) {
            JOptionPane.showMessageDialog(this,
                    "Stop the timer before removing a project.",
                    "Timer running", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove project \"" + selected + "\" and all its records?",
                "Confirm remove", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        timerService.removeProject(selected);
        summaryService.rebuildRanking(); // sync ADTs after deletion
        refreshProjectCombo();
        mainWindow.refreshAll();
    }

    // ── Refresh methods ───────────────────────────────────────

    /**
     * Repopulates the project dropdown from TimerService.
     * Preserves the currently selected item if it still exists.
     */
    public void refreshProjectCombo() {
        String current = (String) projectCombo.getSelectedItem();
        projectCombo.removeAllItems();
        for (Project p : timerService.getProjects()) {
            projectCombo.addItem(p.getName());
        }
        if (current != null) projectCombo.setSelectedItem(current);
    }

    /**
     * Redraws the ranking panel for the date shown in rankingDateField.
     * Uses SummaryService.getTopNByDate() for all dates (including today),
     * which reads from MyHashTable to ensure consistent, accurate data.
     * Displays "No data for this date." if no entries exist for the chosen date.
     */
    public void refreshRanking() {
        rankingPanel.removeAll();

        LocalDate date;
        try {
            date = LocalDate.parse(rankingDateField.getText().trim());
        } catch (Exception ex) {
            date = LocalDate.now();
        }

        // Always use HashTable-backed lookup for consistent results
        List<Project> top = summaryService.getTopNByDate(date, 5);

        if (top.isEmpty()) {
            rankingPanel.add(new JLabel("  No data for this date."));
        } else {
            long max = top.get(0).getTotalDuration();
            for (int i = 0; i < top.size(); i++) {
                Project p   = top.get(i);
                long    dur = p.getTotalDuration();
                int     pct = max > 0 ? (int) (dur * 100 / max) : 0;

                JPanel row = new JPanel(new BorderLayout(6, 0));
                row.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

                JLabel nameLabel = new JLabel((i + 1) + ". " + p.getName());
                nameLabel.setPreferredSize(new Dimension(120, 20));

                JProgressBar bar = new JProgressBar(0, 100);
                bar.setValue(pct);
                bar.setPreferredSize(new Dimension(100, 14));

                JLabel timeLabel = new JLabel(formatDuration(dur));
                timeLabel.setPreferredSize(new Dimension(70, 20));
                timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

                row.add(nameLabel, BorderLayout.WEST);
                row.add(bar,       BorderLayout.CENTER);
                row.add(timeLabel, BorderLayout.EAST);
                rankingPanel.add(row);
            }
        }
        rankingPanel.revalidate();
        rankingPanel.repaint();
    }

    // ── Formatting helpers ────────────────────────────────────

    /** Formats elapsed seconds as HH:MM:SS for the stopwatch display. */
    private String formatTime(long totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    /** Formats a duration in seconds as a human-readable string (e.g. "2h 30m"). */
    private String formatDuration(long totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        if (h > 0) return h + "h " + m + "m";
        return m + "m";
    }
}