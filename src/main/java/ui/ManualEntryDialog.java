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

/**
 * ManualEntryDialog is a modal dialog that allows users to retroactively
 * log a time session they forgot to track in real time.
 *
 * The user selects a project, enters a past date, and specifies the duration
 * in hours and minutes. On confirmation, the dialog validates all inputs
 * and exposes the result via getters for HistoryPanel to consume.
 */
public class ManualEntryDialog extends JDialog {

    private final TimerService timerService;

    private JComboBox<String> projectCombo;
    private JTextField        dateField;
    private JTextField        hoursField;
    private JTextField        minutesField;
    private JButton           confirmBtn;
    private JButton           cancelBtn;

    // Result fields — populated on successful confirmation
    private boolean   confirmed = false;
    private String    projectName;
    private LocalDate date;
    private long      durationSeconds;

    /**
     * Creates a modal dialog centred on the parent window.
     *
     * @param parent      the parent frame (used for positioning)
     * @param timerService used to populate the project dropdown
     */
    public ManualEntryDialog(Frame parent, TimerService timerService) {
        super(parent, "Add manual entry", true); // true = modal
        this.timerService = timerService;

        initComponents();
        layoutComponents();

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    // ── Component initialisation ──────────────────────────────

    /**
     * Creates and configures all form controls.
     * The project combo is populated from TimerService.getProjects().
     * The date field defaults to today, and duration defaults to 0h 30m.
     * Pressing Enter triggers the confirm action via setDefaultButton.
     */
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

        // Allow the user to press Enter to submit the form
        getRootPane().setDefaultButton(confirmBtn);
    }

    // ── Layout ────────────────────────────────────────────────

    /**
     * Arranges the form fields using GridBagLayout for precise alignment.
     * Row 0: project selector
     * Row 1: date field with format hint
     * Row 2: hours and minutes fields
     * Bottom: Cancel and Add buttons
     */
    private void layoutComponents() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));
        GridBagConstraints c = new GridBagConstraints();
        c.insets  = new Insets(5, 6, 5, 6);
        c.anchor  = GridBagConstraints.WEST;
        c.fill    = GridBagConstraints.HORIZONTAL;

        // Row 0: Project
        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        form.add(new JLabel("Project:"), c);
        c.gridx = 1; c.gridwidth = 3; c.weightx = 1;
        form.add(projectCombo, c);

        // Row 1: Date
        c.gridx = 0; c.gridy = 1; c.gridwidth = 1; c.weightx = 0;
        form.add(new JLabel("Date:"), c);
        c.gridx = 1; c.gridwidth = 3; c.weightx = 1;
        form.add(dateField, c);
        c.gridx = 4; c.gridwidth = 1; c.weightx = 0;
        form.add(new JLabel("(yyyy-MM-dd)"), c);

        // Row 2: Duration (hours + minutes)
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

        // Button row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnRow.add(cancelBtn);
        btnRow.add(confirmBtn);

        setLayout(new BorderLayout());
        add(form,   BorderLayout.CENTER);
        add(btnRow, BorderLayout.SOUTH);
    }

    // ── Confirmation logic ────────────────────────────────────

    /**
     * Validates all inputs before accepting the entry.
     * Checks that:
     *   - A project is selected
     *   - The date is a valid yyyy-MM-dd string
     *   - Hours >= 0, minutes are between 0 and 59, total duration > 0
     * On success, sets confirmed=true and closes the dialog.
     * On failure, shows an error message and returns focus to the problem field.
     */
    private void onConfirm() {
        // Validate project selection
        if (projectCombo.getSelectedItem() == null) {
            showError("Please select a project.");
            return;
        }

        // Validate date format
        try {
            date = LocalDate.parse(dateField.getText().trim());
        } catch (DateTimeParseException ex) {
            showError("Invalid date. Please use yyyy-MM-dd format.");
            dateField.requestFocus();
            return;
        }

        // Validate duration — hours >= 0, minutes 0–59, total > 0
        try {
            long h = Long.parseLong(hoursField.getText().trim());
            long m = Long.parseLong(minutesField.getText().trim());
            if (h < 0 || m < 0 || m > 59) throw new NumberFormatException();
            durationSeconds = h * 3600 + m * 60;
            if (durationSeconds <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError("Invalid duration.\nHours >= 0, minutes 0-59, total > 0.");
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

    // ── Getters (called by HistoryPanel after dialog closes) ──

    /** Returns true if the user clicked Add and all inputs were valid. */
    public boolean   isConfirmed()        { return confirmed;       }

    /** Returns the selected project name. */
    public String    getProjectName()     { return projectName;     }

    /** Returns the parsed date entered by the user. */
    public LocalDate getDate()            { return date;            }

    /** Returns the total duration in seconds computed from hours and minutes. */
    public long      getDurationSeconds() { return durationSeconds; }
}