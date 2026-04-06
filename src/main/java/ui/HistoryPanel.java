package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
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
    private JTextField         dateFilter;
    private JTable             table;
    private DefaultTableModel  tableModel;
    private JButton            deleteBtn;
    private JButton            editBtn;

    private static final String[] COLUMNS =
            {"Project", "Date", "Time", "Duration", "Type"};

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

    private void initComponents() {
        projectFilter = new JComboBox<>();
        projectFilter.addItem("All Projects");
        for (Project p : timerService.getProjects()) {
            projectFilter.addItem(p.getName());
        }
        projectFilter.addActionListener(e -> refresh());

        // 日期筛选框，默认显示今天
        dateFilter = new JTextField(LocalDate.now().toString(), 10);
        dateFilter.addActionListener(e -> refresh());          // 按 Enter 刷新
        dateFilter.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                refresh();                                     // 点别处自动刷新
            }
        });

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(0).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);

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

    private void layoutComponents() {
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "History",
                TitledBorder.LEFT, TitledBorder.TOP));

        // 顶部第一行：项目过滤 + 补录
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        row1.add(new JLabel("Project:"));
        row1.add(projectFilter);
        JButton manualBtn = new JButton("+ Manual entry");
        manualBtn.addActionListener(e -> onManualEntry());
        row1.add(manualBtn);

        // 顶部第二行：日期过滤
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        row2.add(new JLabel("Date:"));
        row2.add(dateFilter);
        JButton todayBtn = new JButton("Today");
        JButton clearBtn = new JButton("All dates");
        todayBtn.addActionListener(e -> { dateFilter.setText(LocalDate.now().toString()); refresh(); });
        clearBtn.addActionListener(e -> { dateFilter.setText(""); refresh(); });
        row2.add(todayBtn);
        row2.add(clearBtn);

        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.add(row1);
        topPanel.add(row2);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        btnRow.add(editBtn);
        btnRow.add(deleteBtn);

        add(topPanel,               BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btnRow,                 BorderLayout.SOUTH);
    }

    public void refresh() {
        // 刷新项目下拉
        String selectedFilter = (String) projectFilter.getSelectedItem();
        projectFilter.removeAllItems();
        projectFilter.addItem("All Projects");
        for (Project p : timerService.getProjects()) {
            projectFilter.addItem(p.getName());
        }
        if (selectedFilter != null) projectFilter.setSelectedItem(selectedFilter);

        // 解析日期筛选
        String dateStr = dateFilter.getText().trim();
        LocalDate filterDate = null;
        if (!dateStr.isEmpty()) {
            try {
                filterDate = LocalDate.parse(dateStr);
            } catch (DateTimeParseException ex) {
                // 格式不对就忽略日期筛选
            }
        }

        // 同步日期到 Calendar sync 面板
        if (filterDate != null) mainWindow.syncCalendarDate(filterDate);
        // 刷新表格
        tableModel.setRowCount(0);
        String projectFilterStr = (String) projectFilter.getSelectedItem();
        final LocalDate fd = filterDate;

        for (Project p : timerService.getProjects()) {
            if (!"All Projects".equals(projectFilterStr)
                    && !p.getName().equals(projectFilterStr)) continue;
            for (TimeEntry e : p.getEntriesAsList()) {
                if (fd != null && !e.getDate().equals(fd)) continue;
                tableModel.addRow(new Object[]{
                        p.getName(),
                        e.getDate().toString(),
                        formatTimeRange(e),
                        formatDuration(e.getDuration()),
                        e.isManual() ? "Manual" : "Timed"
                });
            }
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        String projectName = (String) tableModel.getValueAt(row, 0);
        String dateStr     = (String) tableModel.getValueAt(row, 1);
        String durStr      = (String) tableModel.getValueAt(row, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this entry from \"" + projectName + "\" on " + dateStr + "?",
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

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
        String durStr      = (String) tableModel.getValueAt(row, 3);

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
        ManualEntryDialog dialog = new ManualEntryDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                timerService);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            timerService.addManualEntry(
                    dialog.getProjectName(), dialog.getDate(), dialog.getDurationSeconds());
            summaryService.refreshDate(dialog.getDate());
            mainWindow.refreshAll();
        }
    }

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

    private String formatTimeRange(TimeEntry e) {
        if (e.isManual() || e.getStartTime() == null) return "—";
        return e.getStartTime().toLocalTime().toString().substring(0, 5)
             + " → "
             + e.getEndTime().toLocalTime().toString().substring(0, 5);
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