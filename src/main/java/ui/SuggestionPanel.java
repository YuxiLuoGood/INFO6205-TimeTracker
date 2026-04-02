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

public class SuggestionPanel extends JPanel {

    private final AIService       aiService;
    private final CalendarService calendarService;
    private final SummaryService  summaryService;

    // ── AI 建议控件 ───────────────────────────────────────────
    private JTextArea suggestionArea;
    private JButton   refreshBtn;
    private JLabel    statusLabel;

    // ── Calendar 同步控件 ─────────────────────────────────────
    private JTextField dateField;
    private JButton    syncBtn;
    private JLabel     syncStatusLabel;

    public SuggestionPanel(AIService aiService,
                           CalendarService calendarService,
                           SummaryService summaryService) {
        this.aiService       = aiService;
        this.calendarService = calendarService;
        this.summaryService  = summaryService;

        initComponents();
        layoutComponents();
    }

    // ── 初始化控件 ────────────────────────────────────────────

    private void initComponents() {
        // AI 区域
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

        // Calendar 区域
        dateField = new JTextField(LocalDate.now().toString(), 12);
        syncBtn   = new JButton("Sync to Google Calendar");
        syncStatusLabel = new JLabel(" ");
        syncStatusLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        syncStatusLabel.setForeground(Color.GRAY);

        if (!calendarService.isAvailable()) {
            syncBtn.setEnabled(false);
            syncStatusLabel.setText("Google Calendar not configured.");
        }

        syncBtn.addActionListener(e -> onSync());
    }

    // ── 布局 ──────────────────────────────────────────────────

    private void layoutComponents() {
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // ── AI 建议区 ──────────────────────────────────────────
        JPanel aiPanel = new JPanel(new BorderLayout(4, 6));
        aiPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "AI habit suggestions",
                TitledBorder.LEFT, TitledBorder.TOP));

        JPanel aiTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        aiTop.add(refreshBtn);
        aiTop.add(statusLabel);

        aiPanel.add(aiTop,                          BorderLayout.NORTH);
        aiPanel.add(new JScrollPane(suggestionArea), BorderLayout.CENTER);

        // ── Calendar 同步区 ────────────────────────────────────
        JPanel calPanel = new JPanel(new BorderLayout(4, 6));
        calPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Google Calendar sync",
                TitledBorder.LEFT, TitledBorder.TOP));
        calPanel.setPreferredSize(new Dimension(0, 130));

        JPanel calForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        calForm.add(new JLabel("Date (yyyy-MM-dd):"));
        calForm.add(dateField);
        calForm.add(syncBtn);

        calPanel.add(calForm,        BorderLayout.CENTER);
        calPanel.add(syncStatusLabel, BorderLayout.SOUTH);

        // ── 上下分割 ───────────────────────────────────────────
        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, aiPanel, calPanel);
        split.setResizeWeight(0.75);
        split.setBorder(null);

        add(split, BorderLayout.CENTER);
    }

    // ── 按钮事件 ──────────────────────────────────────────────

    private void onGetSuggestions() {
        refreshBtn.setEnabled(false);
        statusLabel.setText("Analysing...");
        suggestionArea.setText("");

        List<DailySummary> recent  = summaryService.getRecentDays(7);
        List<Project>      sorted  = summaryService.getSortedProjects();

        if (recent.isEmpty()) {
            suggestionArea.setText("No data yet. Start tracking your time first!");
            refreshBtn.setEnabled(true);
            statusLabel.setText(" ");
            return;
        }

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