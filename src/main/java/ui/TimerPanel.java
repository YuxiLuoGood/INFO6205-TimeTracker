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
    private JTextField        rankingDateField;

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

    private void layoutComponents() {
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Timer",
                TitledBorder.LEFT, TitledBorder.TOP));

        // 顶部：项目选择
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        topRow.add(new JLabel("Project:"));
        topRow.add(projectCombo);
        topRow.add(newProjectField);
        topRow.add(addProjectBtn);
        topRow.add(removeProjectBtn);

        // 中部：秒表 + 按钮
        JPanel centerPanel = new JPanel(new BorderLayout(4, 8));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        centerPanel.add(timerLabel, BorderLayout.CENTER);
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.add(startBtn);
        btnRow.add(stopBtn);
        centerPanel.add(btnRow, BorderLayout.SOUTH);

        // 底部：排行榜 + 日期选择
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

    private void initTimer() {
        swingTimer = new javax.swing.Timer(1000, e ->
                timerLabel.setText(formatTime(timerService.getElapsedSeconds())));
    }

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

    private void onStop() {
        long duration = timerService.stop();
        swingTimer.stop();
        timerLabel.setText("00:00:00");
        startBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        projectCombo.setEnabled(true);
        removeProjectBtn.setEnabled(true);

        String projectName = (String) projectCombo.getSelectedItem();
        Project project = timerService.getProjects().stream()
                .filter(p -> p.getName().equals(projectName))
                .findFirst().orElse(null);
        if (project != null && duration > 0) {
            List<model.TimeEntry> entries = project.getEntriesAsList();
            if (!entries.isEmpty()) {
                summaryService.recordEntry(project, entries.get(entries.size() - 1));
            }
        }
        refreshRanking();
        mainWindow.refreshHistory();
    }

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
        summaryService.rebuildRanking();
        refreshProjectCombo();
        mainWindow.refreshAll();
    }

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

        LocalDate date;
        try {
            date = LocalDate.parse(rankingDateField.getText().trim());
        } catch (Exception ex) {
            date = LocalDate.now();
        }

        List<Project> top = date.equals(LocalDate.now())
                ? summaryService.getTopNByDate(LocalDate.now(), 5)
                : summaryService.getTopNByDate(date, 5);

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