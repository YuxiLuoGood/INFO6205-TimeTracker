package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.Project;
import service.TimerService;

public class ManualEntryDialog extends JDialog {

    private final TimerService timerService;

    private JComboBox<String> projectCombo;
    private JTextField        dateField;
    private JTextField        hoursField;
    private JTextField        minutesField;
    private JButton           confirmBtn;
    private JButton           cancelBtn;

    private boolean   confirmed = false;
    private String    projectName;
    private LocalDate date;
    private long      durationSeconds;

    public ManualEntryDialog(Frame parent, TimerService timerService) {
        super(parent, "Add manual entry", true); // modal
        this.timerService = timerService;

        initComponents();
        layoutComponents();

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    // ── 初始化控件 ────────────────────────────────────────────

    private void initComponents() {
        projectCombo = new JComboBox<>();
        for (Project p : timerService.getProjects()) {
            projectCombo.addItem(p.getName());
        }

        dateField    = new JTextField(LocalDate.now().toString(), 12);
        hoursField   = new JTextField("0", 4);
        minutesField = new JTextField("30", 4);

        confirmBtn = new JButton("Add");
        cancelBtn  = new JButton("Cancel");

        confirmBtn.addActionListener(e -> onConfirm());
        cancelBtn.addActionListener(e -> dispose());

        // Enter 键触发确认
        getRootPane().setDefaultButton(confirmBtn);
    }

    // ── 布局 ──────────────────────────────────────────────────

    private void layoutComponents() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));
        GridBagConstraints c = new GridBagConstraints();
        c.insets  = new Insets(5, 6, 5, 6);
        c.anchor  = GridBagConstraints.WEST;
        c.fill    = GridBagConstraints.HORIZONTAL;

        // 行 0：Project
        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        form.add(new JLabel("Project:"), c);
        c.gridx = 1; c.gridwidth = 3; c.weightx = 1;
        form.add(projectCombo, c);

        // 行 1：Date
        c.gridx = 0; c.gridy = 1; c.gridwidth = 1; c.weightx = 0;
        form.add(new JLabel("Date:"), c);
        c.gridx = 1; c.gridwidth = 3; c.weightx = 1;
        form.add(dateField, c);
        c.gridx = 4; c.gridwidth = 1; c.weightx = 0;
        form.add(new JLabel("(yyyy-MM-dd)"), c);

        // 行 2：Duration
        c.gridx = 0; c.gridy = 2; c.gridwidth = 1;
        form.add(new JLabel("Duration:"), c);
        c.gridx = 1; c.weightx = 0.3;
        form.add(hoursField, c);
        c.gridx = 2; c.weightx = 0;
        form.add(new JLabel("h"), c);
        c.gridx = 3; c.weightx = 0.3;
        form.add(minutesField, c);
        c.gridx = 4; c.weightx = 0;
        form.add(new JLabel("m"), c);

        // 按钮行
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnRow.add(cancelBtn);
        btnRow.add(confirmBtn);

        setLayout(new BorderLayout());
        add(form,   BorderLayout.CENTER);
        add(btnRow, BorderLayout.SOUTH);
    }

    // ── 确认逻辑 ──────────────────────────────────────────────

    private void onConfirm() {
        // 验证项目
        if (projectCombo.getSelectedItem() == null) {
            showError("Please select a project.");
            return;
        }

        // 验证日期
        try {
            date = LocalDate.parse(dateField.getText().trim());
        } catch (DateTimeParseException ex) {
            showError("Invalid date. Please use yyyy-MM-dd format.");
            dateField.requestFocus();
            return;
        }

        // 验证时长
        try {
            long h = Long.parseLong(hoursField.getText().trim());
            long m = Long.parseLong(minutesField.getText().trim());
            if (h < 0 || m < 0 || m > 59) throw new NumberFormatException();
            durationSeconds = h * 3600 + m * 60;
            if (durationSeconds <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError("Invalid duration.\nHours ≥ 0, minutes 0–59, total > 0.");
            hoursField.requestFocus();
            return;
        }

        projectName = (String) projectCombo.getSelectedItem();
        confirmed   = true;
        dispose();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Input error",
                JOptionPane.WARNING_MESSAGE);
    }

    // ── Getters（HistoryPanel 调用）───────────────────────────

    public boolean   isConfirmed()       { return confirmed;       }
    public String    getProjectName()    { return projectName;     }
    public LocalDate getDate()           { return date;            }
    public long      getDurationSeconds(){ return durationSeconds; }
}