package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
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

public class TimerPanel extends JPanel {

    private final TimerService   timerService;
    private final SummaryService summaryService;
    private final MainWindow     mainWindow;

    // ── 计时器控件 ────────────────────────────────────────────
    private JComboBox<String> projectCombo;
    private JTextField        newProjectField;
    private JButton           addProjectBtn;
    private JLabel            timerLabel;
    private JButton           startBtn;
    private JButton           stopBtn;

    // ── 排行榜控件 ────────────────────────────────────────────
    private JPanel rankingPanel;

    // ── Swing Timer（每秒刷新秒表）────────────────────────────
    private javax.swing.Timer swingTimer;

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

    // ── 初始化控件 ────────────────────────────────────────────

    private void initComponents() {
        projectCombo    = new JComboBox<>();
        newProjectField = new JTextField(12);
        newProjectField.putClientProperty("JTextField.placeholderText", "New project name");
        addProjectBtn   = new JButton("Add");
        timerLabel      = new JLabel("00:00:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 42));

        startBtn = new JButton("Start");
        stopBtn  = new JButton("Stop");
        stopBtn.setEnabled(false);

        startBtn.addActionListener(e -> onStart());
        stopBtn.addActionListener(e -> onStop());
        addProjectBtn.addActionListener(e -> onAddProject());

        rankingPanel = new JPanel();
        rankingPanel.setLayout(new BoxLayout(rankingPanel, BoxLayout.Y_AXIS));
    }

    // ── 布局 ──────────────────────────────────────────────────

    private void layoutComponents() {
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Timer",
                TitledBorder.LEFT, TitledBorder.TOP));

        // 顶部：项目选择 + 新建
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        topRow.add(new JLabel("Project:"));
        topRow.add(projectCombo);
        topRow.add(newProjectField);
        topRow.add(addProjectBtn);

        // 中部：秒表 + 按钮
        JPanel centerPanel = new JPanel(new BorderLayout(4, 8));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        centerPanel.add(timerLabel, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.add(startBtn);
        btnRow.add(stopBtn);
        centerPanel.add(btnRow, BorderLayout.SOUTH);

        // 底部：今日排行
        JPanel rankingWrapper = new JPanel(new BorderLayout());
        rankingWrapper.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Today's ranking",
                TitledBorder.LEFT, TitledBorder.TOP));
        rankingWrapper.add(rankingPanel, BorderLayout.CENTER);

        add(topRow,        BorderLayout.NORTH);
        add(centerPanel,   BorderLayout.CENTER);
        add(rankingWrapper, BorderLayout.SOUTH);
    }

    // ── Swing Timer（每秒刷新显示）───────────────────────────

    private void initTimer() {
        swingTimer = new javax.swing.Timer(1000, e -> {
            long elapsed = timerService.getElapsedSeconds();
            timerLabel.setText(formatTime(elapsed));
        });
    }

    // ── 按钮事件 ──────────────────────────────────────────────

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
        swingTimer.start();
    }

    private void onStop() {
        long duration = timerService.stop();
        swingTimer.stop();
        timerLabel.setText("00:00:00");
        startBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        projectCombo.setEnabled(true);

        // 更新 SummaryService（HashTable + PriorityQueue）
        String projectName = (String) projectCombo.getSelectedItem();
        Project project = timerService.getProjects().stream()
                .filter(p -> p.getName().equals(projectName))
                .findFirst().orElse(null);
        if (project != null && duration > 0) {
            model.TimeEntry last = project.getEntriesAsList()
                    .get(project.getEntriesAsList().size() - 1);
            summaryService.recordEntry(project, last);
        }

        refreshRanking();
        mainWindow.refreshHistory();
    }

    private void onAddProject() {
        String name = newProjectField.getText().trim();
        if (name.isEmpty()) return;
        if (timerService.getProjects().stream()
                .anyMatch(p -> p.getName().equals(name))) {
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

    // ── 刷新方法 ──────────────────────────────────────────────

    public void refreshProjectCombo() {
        String current = (String) projectCombo.getSelectedItem();
        projectCombo.removeAllItems();
        for (Project p : timerService.getProjects()) {
            projectCombo.addItem(p.getName());
        }
        if (current != null) projectCombo.setSelectedItem(current);
    }

    public void refreshRanking() {
        rankingPanel.removeAll();
        List<Project> top = summaryService.getTopN(5);
        if (top.isEmpty()) {
            rankingPanel.add(new JLabel("  No data yet."));
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

    // ── 工具方法 ──────────────────────────────────────────────

    private String formatTime(long totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    private String formatDuration(long totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        if (h > 0) return h + "h " + m + "m";
        return m + "m";
    }
}