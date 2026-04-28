package elite.intel.ui.view;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.SystemSession;
import elite.intel.ui.event.LlmUsageEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class UsageStatsTabPanel extends JPanel {

    private final Instant sessionStart = Instant.now();
    // Only ever read/written on the EDT
    private final Set<String> seenModels = new LinkedHashSet<>();

    private volatile int lastPrompt = 0;
    private volatile int lastCompletion = 0;
    private final AtomicInteger totalPrompt = new AtomicInteger();
    private final AtomicInteger totalCompletion = new AtomicInteger();
    private final AtomicInteger totalCachedHits = new AtomicInteger();
    private final AtomicInteger totalCacheWritten = new AtomicInteger();
    private final SystemSession systemSession = SystemSession.getInstance();

    private JLabel providerLabel;
    private JLabel sessionTimeLabel;
    private JLabel totalLabel;
    private JLabel savedLabel;
    private JLabel tphLabel;
    private BarChart chart;

    @SuppressWarnings("unused")
    private final Timer clockTimer;

    public UsageStatsTabPanel() {
        EventBusManager.register(this);
        buildUi();
        clockTimer = new Timer(1_000, e -> tickClock());
        clockTimer.start();
    }

    private void buildUi() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(16, 2, 16, 20));
        setOpaque(false);

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(8, 0, 0, 0));

        providerLabel = new JLabel("LLM: N/A");
        providerLabel.setFont(providerLabel.getFont().deriveFont(Font.BOLD, 16f));
        providerLabel.setForeground(AppTheme.FG);

        sessionTimeLabel = new JLabel("Session time: 00:00:00");
        sessionTimeLabel.setForeground(AppTheme.FG_MUTED);

        header.add(providerLabel);
        header.add(Box.createHorizontalGlue());
        header.add(sessionTimeLabel);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Chart
        chart = new BarChart();

        // Footer
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 0, 0, 0));
        footer.setBackground(AppTheme.LOG_BG);
        footer.setPreferredSize(new Dimension(super.getPreferredSize().width, 180));
        if (systemSession.useLocalCommandLlm() && systemSession.useLocalQueryLlm()) {
            totalLabel = new JLabel("Total tokens used (free):  0");
        } else {
            totalLabel = new JLabel("Total tokens used (chargeable):  0");
        }
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
        totalLabel.setForeground(AppTheme.ACCENT);

        savedLabel = new JLabel("Tokens saved by caching:  0");
        savedLabel.setForeground(AppTheme.FG_MUTED);

        tphLabel = new JLabel("Tokens / Hour (estimate this session):  -");
        tphLabel.setForeground(AppTheme.FG_MUTED);

        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        savedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        tphLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        footer.add(Box.createVerticalGlue());
        footer.add(totalLabel);
        footer.add(savedLabel);
        footer.add(tphLabel);

        /// put it all together
        add(header);
        add(chart);
        add(Box.createVerticalGlue());
        add(footer);
    }

    @Subscribe
    public void onLlmUsage(LlmUsageEvent event) {
        lastPrompt = event.promptTokens();
        lastCompletion = event.completionTokens();
        totalPrompt.addAndGet(event.promptTokens());
        totalCompletion.addAndGet(event.completionTokens());
        totalCachedHits.addAndGet(event.cachedTokens());
        totalCacheWritten.addAndGet(event.cacheWrittenTokens());
        SwingUtilities.invokeLater(() -> {
            seenModels.add(event.provider() + "  [" + event.model() + "]");
            refresh();
        });
    }

    private void tickClock() {
        Duration d = Duration.between(sessionStart, Instant.now());
        sessionTimeLabel.setText(String.format("Session time: %02d:%02d:%02d",
                d.toHours(), d.toMinutesPart(), d.toSecondsPart()));
        refreshTph();
    }

    private void refresh() {
        if (!seenModels.isEmpty()) {
            providerLabel.setText("LLM: " + String.join(" / ", seenModels));
        }
        int hits = totalCachedHits.get();
        int written = totalCacheWritten.get();
        int total = totalPrompt.get() + totalCompletion.get();

        chart.update(lastPrompt, lastCompletion, hits, written);
        if (systemSession.useLocalCommandLlm() && systemSession.useLocalQueryLlm()) {
            totalLabel.setText("Total tokens used (FREE):  " + total);
        } else {
            totalLabel.setText("Total tokens used (chargeable):  " + total);
        }
        savedLabel.setText(hits > 0
                ? "Tokens saved by caching:  " + hits + "  (served at reduced rate)"
                : "Tokens saved by caching:  0");
        refreshTph();
    }

    private void refreshTph() {
        // Only count non-cached input + completion as full-rate chargeable tokens
        int prompt = totalPrompt.get();
        int completion = totalCompletion.get();
        int hits = totalCachedHits.get();
        int chargeable = (prompt - hits) + completion;
        long elapsedSeconds = Duration.between(sessionStart, Instant.now()).toSeconds();
        if (elapsedSeconds < 600) {
            tphLabel.setText("Tokens / Hour (estimate this session):  - (collecting data...)");
        } else if (chargeable > 0) {
            long tph = Math.round(chargeable / (elapsedSeconds / 3600.0));
            tphLabel.setText("Tokens / Hour (estimate this session):  " + tph);
        } else {
            tphLabel.setText("Tokens / Hour (estimate this session):  -");
        }
    }

    // -------------------------------------------------------------------------

    private static final class BarChart extends JPanel {

        private static final String[] LABELS = {
                "Last Prompt",
                "Last Completion",
                "Cache Hits Total",
                "Cache Written Total"
        };
        private static final Color[] COLORS = {
                new Color(0x03529F),   // blue   – prompt
                new Color(0x2E8B57),   // green  – completion
                new Color(0xFF8C00),   // orange – cache hits
                new Color(0x6A6A8A)   // grey   – cache written
        };

        private int[] values = new int[4];

        BarChart() {
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(super.getPreferredSize().width, 180);
        }

        @Override
        public Dimension getMaximumSize() {
            // Let it grow horizontally but stay fixed height
            return new Dimension(Integer.MAX_VALUE, 180);
        }

        void update(int prompt, int completion, int hits, int written) {
            values = new int[]{prompt, completion, hits, written};
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int n = values.length;
                int labelW = 260;
                int valueW = 80;
                int barAreaW = getWidth() - labelW - valueW - 24;
                if (barAreaW <= 0) return;

                int rowH = Math.min(42, Math.max(20, (getHeight() - 20) / n));
                int barH = rowH - Math.max(4, rowH / 5);
                int totalH = n * rowH - (rowH - barH);
                int startY = (getHeight() - totalH) / 2;

                int maxVal = 1;
                for (int v : values) maxVal = Math.max(maxVal, v);

                Font font = g2.getFont().deriveFont(18f);
                g2.setFont(font);
                FontMetrics fm = g2.getFontMetrics(font);
                int baseline = barH / 2 + fm.getAscent() / 2 - 1;

                for (int i = 0; i < n; i++) {
                    int y = startY + i * rowH;

                    g2.setColor(AppTheme.FG_MUTED);
                    g2.drawString(LABELS[i], 0, y + baseline);

                    g2.setColor(AppTheme.BG_PANEL);
                    g2.fillRoundRect(labelW, y, barAreaW, barH, 6, 6);

                    if (values[i] > 0) {
                        int fillW = Math.max(6, (int) ((long) values[i] * barAreaW / maxVal));
                        g2.setColor(COLORS[i]);
                        g2.fillRoundRect(labelW, y, fillW, barH, 6, 6);
                    }

                    g2.setColor(AppTheme.FG);
                    g2.drawString(format(values[i]), labelW + barAreaW + 8, y + baseline);
                }
            } finally {
                g2.dispose();
            }
        }

        private static String format(int v) {
            if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000.0);
            if (v >= 1_000) return String.format("%.1fK", v / 1_000.0);
            return String.valueOf(v);
        }
    }
}
