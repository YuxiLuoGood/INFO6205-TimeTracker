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

            // 1. 从本地文件加载数据
            List<Project> projects = JsonStorage.load();

            // 2. 初始化所有 service
            TimerService   timerService   = new TimerService(projects);
            SummaryService summaryService = new SummaryService(projects);
            AIService      aiService      = new AIService();
            CalendarService calendarService = new CalendarService();

            // 3. 用已有数据重建 HashTable 和 PriorityQueue
            summaryService.rebuildFromProjects();

            // 4. 启动主窗口
            MainWindow window = new MainWindow(
                    timerService,
                    summaryService,
                    aiService,
                    calendarService
            );

            // 5. 关闭时自动保存数据
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