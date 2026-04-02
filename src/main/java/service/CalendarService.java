package service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import model.DailySummary;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

public class CalendarService {

    private static final String APP_NAME         = "Time Tracker";
    private static final String CREDENTIALS_FILE = "credentials.json";
    private static final String TOKENS_DIR       = "tokens";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private Calendar calendarClient;

    public CalendarService() {
        try {
            NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential      = authorize(transport);
            calendarClient = new Calendar.Builder(transport, JSON_FACTORY, credential)
                    .setApplicationName(APP_NAME)
                    .build();
        } catch (Exception e) {
            System.err.println("Google Calendar init failed: " + e.getMessage());
            calendarClient = null;
        }
    }

    // ── 主入口 ────────────────────────────────────────────────

    /**
     * 把某天的时间汇总同步为一个 Google Calendar 全天事件
     * 在 SwingWorker 后台线程里调用
     *
     * @param summary 来自 SummaryService.getDailySummary(date)
     * @return 成功时返回事件链接，失败时返回错误信息
     */
    public String syncDay(DailySummary summary) {
        if (calendarClient == null) return "Google Calendar not initialized.";
        if (summary == null)        return "No data for this date.";

        try {
            Event event = buildEvent(summary);
            Event created = calendarClient.events()
                    .insert("primary", event)
                    .execute();
            return "Synced: " + created.getHtmlLink();
        } catch (IOException e) {
            return "Sync failed: " + e.getMessage();
        }
    }

    /**
     * 判断 CalendarService 是否初始化成功
     * GUI 里可以用这个决定是否显示 Sync 按钮
     */
    public boolean isAvailable() {
        return calendarClient != null;
    }

    // ── Event 构建 ────────────────────────────────────────────

    private Event buildEvent(DailySummary summary) {
        LocalDate date = summary.getDate();

        // 标题
        String title = "Time Tracker: " + date;

        // 描述：列出所有项目时长
        StringBuilder desc = new StringBuilder();
        desc.append("Total: ").append(formatDuration(summary.getTotalDuration())).append("\n\n");

        Set<String> projects = summary.getProjectNames();
        for (String project : projects) {
            long dur = summary.getDurationForProject(project);
            desc.append("- ").append(project)
                .append(": ").append(formatDuration(dur)).append("\n");
        }

        // 全天事件：start 和 end 都用 date 格式
        Date startDate = Date.from(
                date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate   = Date.from(
                date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        com.google.api.client.util.DateTime start =
                new com.google.api.client.util.DateTime(true, startDate.getTime(), 0);
        com.google.api.client.util.DateTime end =
                new com.google.api.client.util.DateTime(true, endDate.getTime(), 0);

        return new Event()
                .setSummary(title)
                .setDescription(desc.toString())
                .setStart(new EventDateTime().setDate(start))
                .setEnd(new EventDateTime().setDate(end));
    }

    // ── OAuth 2.0 认证 ────────────────────────────────────────

    private Credential authorize(NetHttpTransport transport) throws IOException {
        // 读取 credentials.json（从 Google Cloud Console 下载）
        File credFile = new File(CREDENTIALS_FILE);
        if (!credFile.exists()) {
            throw new FileNotFoundException(
                    "credentials.json not found. Download it from Google Cloud Console.");
        }

        try (InputStream in = new FileInputStream(credFile)) {
            GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY,
                    new InputStreamReader(in));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    transport, JSON_FACTORY, secrets,
                    Collections.singletonList(CalendarScopes.CALENDAR))
                    .setDataStoreFactory(
                            new FileDataStoreFactory(new File(TOKENS_DIR)))
                    .setAccessType("offline")
                    .build();

            // 第一次运行会打开浏览器让用户授权，之后 token 存在 tokens/ 目录
            LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                    .setPort(8888)
                    .build();
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        }
    }

    // ── 工具方法 ──────────────────────────────────────────────

    private String formatDuration(long totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        if (h > 0) return h + "h " + m + "m";
        return m + "m";
    }
}