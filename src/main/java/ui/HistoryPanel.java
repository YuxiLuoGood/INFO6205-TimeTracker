package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.time.LocalDate;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import model.Project;
import model.TimeEntry;
import service.SummaryService;
import service.TimerService;

public class HistoryPanel extends JPanel {

    private final TimerService   timerService;
    private final SummaryService summaryService;
    private final MainWindow     mainWindow;

    private JComboBox<String>  projectFilter;
    private JTable             table;
    private DefaultTableModel  tableModel;
    private JButton            deleteBtn;
    private JButton            editBtn;

    private static final String[] COLUMNS =
            {"Project", "Date", "Duration", "Type"};

    public HistoryPanel(TimerService timerService,
                        SummaryService summaryService,
                        MainWindow mainWindow) {
        this.timerService   = timerService;
        this.summaryService = summaryService;
        this.mainWindow     = mainWindow;

        initComponents();
        layoutComponents();
        refresh();
    }

    // ── 初始化控件 ────────────────────────────────────────────

    private void initComponents() {
        projectFilter = new JComboBox<>();
        projectFilter.addItem("All Projects");
        for (Project p : timerService.getProjects()) {
            projectFilter.addItem(p.getName());
        }
        projectFilter.addActionListener(e -> refresh());

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);

        deleteBtn = new JButton("Delete");
        editBtn   = new JButton("Edit duration");
        deleteBtn.setEnabled(false);
        editBtn.setEnabled(false);

        table.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = table.getSelectedRow() >= 0;
            deleteBtn.setEnabled(sel);
            editBtn.setEnabled(sel);
        });

        deleteBtn.addActionListener(e -> onDelete());
        editBtn.addActionListener(e -> onEdit());
    }

    // ── 布局 ──────────────────────────────────────────────────

    private void layoutComponents() {
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "History",
                TitledBorder.LEFT, TitledBorder.TOP));

        // 顶部：过滤 + 补录按钮
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        topRow.add(new JLabel("Filter:"));
        topRow.add(projectFilter);

        JButton manualBtn = new JButton("+ Manual entry");
        manualBtn.addActionListener(e -> onManualEntry());
        topRow.add(manualBtn);

        // 底部：操作按钮
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        btnRow.add(editBtn);
        btnRow.add(deleteBtn);

        add(topRow,                        BorderLayout.NORTH);
        add(new JScrollPane(table),        BorderLayout.CENTER);
        add(btnRow,                        BorderLayout.SOUTH);
    }

    // ── 刷新表格 ──────────────────────────────────────────────

    public void refresh() {
        // 刷新项目下拉
        String selectedFilter = (String) projectFilter.getSelectedItem();
        projectFilter.removeAllItems();
        projectFilter.addItem("All Projects");
        for (Project p : timerService.getProjects()) {
            projectFilter.addItem(p.getName());
        }
        if (selectedFilter != null) projectFilter.setSelectedItem(selectedFilter);

        // 刷新表格数据
        tableModel.setRowCount(0);
        String filter = (String) projectFilter.getSelectedItem();

        for (Project p : timerService.getProjects()) {
            if (!"All Projects".equals(filter) && !p.getName().equals(filter)) continue;
            for (TimeEntry e : p.getEntriesAsList()) {
                tableModel.addRow(new Object[]{
                        p.getName(),
                        e.getDate().toString(),
                        formatDuration(e.getDuration()),
                        e.isManual() ? "Manual" : "Timed"
                });
            }
        }
    }

    // ── 按钮事件 ──────────────────────────────────────────────

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        String projectName = (String) tableModel.getValueAt(row, 0);
        String dateStr     = (String) tableModel.getValueAt(row, 1);
        String durStr      = (String) tableModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this entry from \"" + projectName + "\" on " + dateStr + "?",
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // 找到对应的 TimeEntry 对象
        TimeEntry target = findEntry(projectName, dateStr, durStr);
        if (target != null) {
            timerService.removeEntry(projectName, target);
            summaryService.refreshDate(target.getDate());
        }
        mainWindow.refreshAll();
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        String projectName = (String) tableModel.getValueAt(row, 0);
        String dateStr     = (String) tableModel.getValueAt(row, 1);
        String durStr      = (String) tableModel.getValueAt(row, 2);

        String input = JOptionPane.showInputDialog(this,
                "Enter new duration in minutes:",
                "Edit duration", JOptionPane.PLAIN_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;

        try {
            long newMinutes = Long.parseLong(input.trim());
            if (newMinutes <= 0) throw new NumberFormatException();
            long newSeconds = newMinutes * 60;

            TimeEntry target = findEntry(projectName, dateStr, durStr);
            if (target != null) {
                timerService.editEntry(projectName, target, newSeconds);
                summaryService.refreshDate(target.getDate());
            }
            mainWindow.refreshAll();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid positive number.",
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onManualEntry() {
        // 弹出补录对话框
        ManualEntryDialog dialog = new ManualEntryDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                timerService);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            String    name    = dialog.getProjectName();
            LocalDate date    = dialog.getDate();
            long      seconds = dialog.getDurationSeconds();
            timerService.addManualEntry(name, date, seconds);
            summaryService.refreshDate(date);
            mainWindow.refreshAll();
        }
    }

    // ── 工具方法 ──────────────────────────────────────────────

    /**
     * 根据表格行信息找到对应的 TimeEntry 对象
     * 用项目名 + 日期 + 时长匹配
     */
    private TimeEntry findEntry(String projectName, String dateStr, String durStr) {
        for (Project p : timerService.getProjects()) {
            if (!p.getName().equals(projectName)) continue;
            for (TimeEntry e : p.getEntriesAsList()) {
                if (e.getDate().toString().equals(dateStr)
                        && formatDuration(e.getDuration()).equals(durStr)) {
                    return e;
                }
            }
        }
        return null;
    }

    private String formatDuration(long totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        if (h > 0) return h + "h " + m + "m " + s + "s";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }
}