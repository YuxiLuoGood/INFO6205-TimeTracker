package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JSplitPane;

import service.AIService;
import service.CalendarService;
import service.SummaryService;
import service.TimerService;

public class MainWindow extends JFrame {

    private final TimerService    timerService;
    private final SummaryService  summaryService;
    private final AIService       aiService;
    private final CalendarService calendarService;

    private TimerPanel       timerPanel;
    private HistoryPanel     historyPanel;
    private SuggestionPanel  suggestionPanel;

    public MainWindow(TimerService timerService,
                      SummaryService summaryService,
                      AIService aiService,
                      CalendarService calendarService) {

        this.timerService    = timerService;
        this.summaryService  = summaryService;
        this.aiService       = aiService;
        this.calendarService = calendarService;

        initWindow();
        initPanels();
        layoutPanels();
    }

    //Window Basic Settings 

    private void initWindow() {
        setTitle("Time Tracker");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 关闭由 Main.java 处理
        setSize(900, 640);
        setMinimumSize(new Dimension(800, 560));
        setLocationRelativeTo(null); // Center alignment
    }

    //Initialize each panel

    private void initPanels() {
        timerPanel      = new TimerPanel(timerService, summaryService, this);
        historyPanel    = new HistoryPanel(timerService, summaryService, this);
        suggestionPanel = new SuggestionPanel(aiService, calendarService, summaryService);
    }


    private void layoutPanels() {
        setLayout(new BorderLayout(8, 8));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left: Timer + History, split vertically
        JSplitPane leftSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, timerPanel, historyPanel);
        leftSplit.setDividerLocation(320);
        leftSplit.setResizeWeight(0.5);
        leftSplit.setBorder(null);

        // Split left and right
        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, leftSplit, suggestionPanel);
        mainSplit.setDividerLocation(520);
        mainSplit.setResizeWeight(0.6);
        mainSplit.setBorder(null);

        add(mainSplit, BorderLayout.CENTER);
    }

    // Inter-panel communication

    /** Synchronizes the date field in the SuggestionPanel's Calendar when the date in the HistoryPanel changes */
    public void syncCalendarDate(java.time.LocalDate date) {
        if (suggestionPanel != null) suggestionPanel.setCalendarDate(date);
    }

    /** Refresh the history list after TimerPanel stop() */
    public void refreshHistory() {
        historyPanel.refresh();
    }

    /** Refresh history and rankings after manually adding or editing data */
    public void refreshAll() {
        historyPanel.refresh();
        timerPanel.refreshRanking();
    }
}