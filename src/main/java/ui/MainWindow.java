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

    // ── 窗口基本设置 ──────────────────────────────────────────

    private void initWindow() {
        setTitle("Time Tracker");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 关闭由 Main.java 处理
        setSize(900, 640);
        setMinimumSize(new Dimension(800, 560));
        setLocationRelativeTo(null); // 居中显示
    }

    // ── 初始化各面板 ──────────────────────────────────────────

    private void initPanels() {
        timerPanel      = new TimerPanel(timerService, summaryService, this);
        historyPanel    = new HistoryPanel(timerService, summaryService, this);
        suggestionPanel = new SuggestionPanel(aiService, calendarService, summaryService);
    }

    // ── 布局 ──────────────────────────────────────────────────
    //
    //  ┌─────────────────┬──────────────────────┐
    //  │   TimerPanel    │                      │
    //  │  (左上，计时器)  │   SuggestionPanel    │
    //  ├─────────────────┤   (右侧，AI + 日历)   │
    //  │  HistoryPanel   │                      │
    //  │  (左下，历史)    │                      │
    //  └─────────────────┴──────────────────────┘

    private void layoutPanels() {
        setLayout(new BorderLayout(8, 8));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 左侧：计时器 + 历史记录，上下分割
        JSplitPane leftSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, timerPanel, historyPanel);
        leftSplit.setDividerLocation(320);
        leftSplit.setResizeWeight(0.5);
        leftSplit.setBorder(null);

        // 左右分割
        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, leftSplit, suggestionPanel);
        mainSplit.setDividerLocation(520);
        mainSplit.setResizeWeight(0.6);
        mainSplit.setBorder(null);

        add(mainSplit, BorderLayout.CENTER);
    }

    // ── 面板间通信 ────────────────────────────────────────────

    /** TimerPanel stop() 后刷新历史记录列表 */
    public void refreshHistory() {
        historyPanel.refresh();
    }

    /** 手动补录或编辑后刷新历史和排行 */
    public void refreshAll() {
        historyPanel.refresh();
        timerPanel.refreshRanking();
    }
}