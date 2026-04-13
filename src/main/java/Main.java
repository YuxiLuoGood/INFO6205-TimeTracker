import java.util.List;

import javax.swing.SwingUtilities;

import model.Project;
import service.AIService;
import service.CalendarService;
import service.SummaryService;
import service.TimerService;
import storage.JsonStorage;
import ui.MainWindow;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

        	// 1. Load data from a local file
            List<Project> projects = JsonStorage.load();

            // 2. Initialize all services
            TimerService   timerService   = new TimerService(projects);
            SummaryService summaryService = new SummaryService(projects);
            AIService      aiService      = new AIService();
            CalendarService calendarService = new CalendarService();

            // 3. Reconstruct a HashTable and PriorityQueue using existing data
            summaryService.rebuildFromProjects();

            // 4. Launch the main window
            MainWindow window = new MainWindow(
                    timerService,
                    summaryService,
                    aiService,
                    calendarService
            );

            // 5. Automatically save data when closing
            window.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    JsonStorage.save(projects);
                    System.exit(0);
                }
            });

            window.setVisible(true);
        });
    }
}