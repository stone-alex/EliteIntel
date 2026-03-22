package elite.intel.ui.view.settings;

import com.google.common.eventbus.Subscribe;
import elite.intel.ai.ears.AudioMonitorEvent;
import elite.intel.gameapi.AudioMonitorBus;
import elite.intel.ui.view.AppTheme;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Arrays;

/**
 * Real-time oscilloscope-style microphone monitor panel.
 * <p>
 * Subscribes to AudioMonitorBus (async) so the audio capture thread is never
 * blocked by UI painting. Threshold lines show calibrated noise floor (grey)
 * and speech gate (blue). Waveform colour reflects current signal quality:
 * rms - noiseFloor  < 200  → red   (gate unlikely to open)
 * rms - noiseFloor 200-400 → amber (marginal)
 * rms - noiseFloor  > 400  → green (clean signal)
 */
public class AudioWaveformPanel extends JPanel {

    // -- Tuning constants ----------------------------------------------------
    /**
     * Total scrolling-window samples held in the ring buffer.
     */
    private static final int WAVE_BUFFER_SIZE = 600;
    /**
     * New waveform points contributed by each ~100 ms audio frame.
     */
    private static final int SAMPLES_PER_FRAME = 20;
    /**
     * Panel height in pixels.
     */
    public static final int PANEL_HEIGHT = 200;

    // -- Palette ------------------------------------------------------------
    private static final Color BG_SCOPE = new Color(0x0D, 0x0E, 0x17);
    private static final Color COLOR_GRID = new Color(0x1E, 0x20, 0x35);
    private static final Color COLOR_RED = new Color(0xFF, 0x44, 0x44);
    private static final Color COLOR_AMBER = new Color(0xFF, 0x8C, 0x00);
    private static final Color COLOR_GREEN = new Color(0x44, 0xCC, 0x66);
    private static final Color COLOR_FLOOR = new Color(0x88, 0x88, 0x88);
    private static final Color COLOR_GATE = new Color(0x20, 0x90, 0xFF);
    private static final Color COLOR_LABEL = new Color(0x66, 0x77, 0x88);
    private static final float[] DASH = {5f, 4f};

    // -- Clip detection ------------------------------------------------------
    /**
     * Samples at or above this absolute value (97.7 % of 32767) are genuine hardware clips.
     */
    private static final short CLIP_THRESHOLD = (short) 32000;
    /**
     * Keep the CLIP badge visible for this many ms after the last detected clip.
     */
    private static final long CLIP_HOLD_MS = 1500;

    // -- State (written on monitor bus thread, read on EDT) ------------------
    private final short[] waveBuffer = new short[WAVE_BUFFER_SIZE];
    private volatile double currentRms = 0;
    private volatile double noiseFloor = 0;
    private volatile double rmsHigh = 0;
    /**
     * Epoch-ms until which the CLIP badge stays lit; 0 = not clipping.
     */
    private volatile long clipExpiry = 0;

    // -- Constructor ---------------------------------------------------------
    public AudioWaveformPanel() {
        setBackground(BG_SCOPE);
        setOpaque(true);
        setPreferredSize(new Dimension(0, PANEL_HEIGHT));
        setMinimumSize(new Dimension(200, PANEL_HEIGHT));
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppTheme.BUTTON_BG, 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6))
        );
        AudioMonitorBus.register(this);
    }

    /**
     * Call when this panel is permanently removed to free the bus subscription.
     */
    public void dispose() {
        AudioMonitorBus.unregister(this);
    }

    // -- Bus subscriber ------------------------------------------------------
    @Subscribe
    public void onAudioFrame(AudioMonitorEvent event) {
        byte[] buf = event.getBuffer();
        int len = event.getLength();
        int totalSamples = len / 2;

        // Subsample the frame evenly to SAMPLES_PER_FRAME points
        short[] incoming = new short[SAMPLES_PER_FRAME];
        for (int i = 0; i < SAMPLES_PER_FRAME; i++) {
            int sampleIdx = (int) ((long) i * totalSamples / SAMPLES_PER_FRAME);
            int byteIdx = sampleIdx * 2;
            if (byteIdx + 1 < len) {
                int lo = buf[byteIdx] & 0xFF;
                int hi = buf[byteIdx + 1] & 0xFF;
                incoming[i] = (short) ((hi << 8) | lo);
            }
        }

        // Scroll buffer left, append new samples at the tail
        synchronized (waveBuffer) {
            System.arraycopy(waveBuffer, SAMPLES_PER_FRAME,
                    waveBuffer, 0,
                    WAVE_BUFFER_SIZE - SAMPLES_PER_FRAME
            );
            System.arraycopy(incoming, 0,
                    waveBuffer, WAVE_BUFFER_SIZE - SAMPLES_PER_FRAME,
                    SAMPLES_PER_FRAME
            );
        }

        currentRms = event.getRms();
        noiseFloor = event.getNoiseFloor();
        rmsHigh = event.getRmsHigh();

        // Clip detection: scan every sample in the full buffer, not just the
        // subsampled display points — we must not miss a single saturated peak.
        for (int i = 0; i + 1 < len; i += 2) {
            short s = (short) (((buf[i + 1] & 0xFF) << 8) | (buf[i] & 0xFF));
            if (s >= CLIP_THRESHOLD || s <= -CLIP_THRESHOLD) {
                clipExpiry = System.currentTimeMillis() + CLIP_HOLD_MS;
                break;
            }
        }

        SwingUtilities.invokeLater(this::repaint);
    }

    // -- Painting ------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int cy = h / 2;

            drawGrid(g2, w, h, cy);

            // Scale: rmsHigh maps to ~70% of half-height; multiply by 1.5 to
            // account for typical voice crest factor (peak ≈ 1.4-1.5× RMS)
            double effectiveHigh = Math.max(rmsHigh, 200.0);
            double scale = (cy * 0.70) / (effectiveHigh * 1.5);

            drawThresholdLines(g2, w, cy, scale);
            drawWaveform(g2, w, cy, scale);
            drawLabels(g2, w, cy, scale);
            drawLegend(g2, w, h);
            drawClipBadge(g2);

        } finally {
            g2.dispose();
        }
    }

    private void drawGrid(Graphics2D g2, int w, int h, int cy) {
        g2.setColor(COLOR_GRID);
        g2.setStroke(new BasicStroke(1f));
        // horizontal centre line
        g2.drawLine(0, cy, w, cy);
        // subtle vertical grid every ~80px
        for (int x = 80; x < w; x += 80) {
            g2.drawLine(x, 0, x, h);
        }
    }

    private void drawThresholdLines(Graphics2D g2, int w, int cy, double scale) {
        BasicStroke dashed = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, DASH, 0f);

        if (noiseFloor > 0) {
            int fy = (int) (noiseFloor * scale);
            g2.setColor(COLOR_FLOOR);
            g2.setStroke(dashed);
            g2.drawLine(0, cy - fy, w, cy - fy);
            g2.drawLine(0, cy + fy, w, cy + fy);
        }

        if (rmsHigh > 0) {
            int gy = (int) (rmsHigh * scale);
            g2.setColor(COLOR_GATE);
            g2.setStroke(dashed);
            g2.drawLine(0, cy - gy, w, cy - gy);
            g2.drawLine(0, cy + gy, w, cy + gy);
        }
    }

    private void drawWaveform(Graphics2D g2, int w, int cy, double scale) {
        double delta = currentRms - noiseFloor;
        Color waveColor;
        if (delta < 200) waveColor = COLOR_RED;
        else if (delta < 400) waveColor = COLOR_AMBER;
        else waveColor = COLOR_GREEN;

        g2.setColor(waveColor);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        short[] snapshot;
        synchronized (waveBuffer) {
            snapshot = Arrays.copyOf(waveBuffer, WAVE_BUFFER_SIZE);
        }

        int[] xs = new int[w];
        int[] ys = new int[w];
        int halfH = getHeight() / 2;

        for (int x = 0; x < w; x++) {
            int bufIdx = x * WAVE_BUFFER_SIZE / w;
            short sample = snapshot[bufIdx];
            int yOff = (int) (sample * scale);
            yOff = Math.max(-(halfH - 3), Math.min(halfH - 3, yOff));
            xs[x] = x;
            ys[x] = cy - yOff;
        }
        g2.drawPolyline(xs, ys, w);

        // Clip markers: bright red ticks at the panel edge for every pixel column
        // that maps to a genuinely saturated sample (≥ CLIP_THRESHOLD).
        // A flat-topped waveform under these ticks = real hardware clipping.
        // A flat-topped waveform with NO ticks = display scale only, not clipping.
        int h = getHeight();
        g2.setColor(new Color(0xFF, 0x22, 0x22));
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        for (int x = 0; x < w; x++) {
            int bufIdx = x * WAVE_BUFFER_SIZE / w;
            short sample = snapshot[bufIdx];
            if (sample >= CLIP_THRESHOLD) {
                g2.drawLine(x, 0, x, 5);
            } else if (sample <= -CLIP_THRESHOLD) {
                g2.drawLine(x, h - 5, x, h);
            }
        }
    }

    private void drawLabels(Graphics2D g2, int w, int cy, double scale) {
        Font labelFont = new Font(Font.MONOSPACED, Font.PLAIN, 9);
        g2.setFont(labelFont);
        FontMetrics fm = g2.getFontMetrics();

        // FLOOR label — sits just above the upper floor line
        if (noiseFloor > 0) {
            int fy = (int) (noiseFloor * scale);
            int lx = 6;
            int ly = cy - fy - 3;
            g2.setColor(new Color(0x0D, 0x0E, 0x17, 0xCC));
            g2.fillRoundRect(lx - 2, ly - fm.getAscent(), fm.stringWidth("FLOOR") + 4, fm.getHeight(), 4, 4);
            g2.setColor(COLOR_FLOOR);
            g2.drawString("FLOOR", lx, ly);
        }

        // GATE label — sits just above the upper gate line
        if (rmsHigh > 0) {
            int gy = (int) (rmsHigh * scale);
            int lx = 6;
            int ly = cy - gy - 3;
            g2.setColor(new Color(0x0D, 0x0E, 0x17, 0xCC));
            g2.fillRoundRect(lx - 2, ly - fm.getAscent(), fm.stringWidth("GATE") + 4, fm.getHeight(), 4, 4);
            g2.setColor(COLOR_GATE);
            g2.drawString("GATE", lx, ly);
        }

        // RMS + delta readout — top-right corner
        double delta = currentRms - noiseFloor;
        Color statusColor;
        if (delta < 200) statusColor = COLOR_RED;
        else if (delta < 400) statusColor = COLOR_AMBER;
        else statusColor = COLOR_GREEN;

        String rmsStr = String.format("RMS %.0f  Δ %.0f", currentRms, Math.max(0, delta));
        int rmsX = w - fm.stringWidth(rmsStr) - 6;
        int rmsY = 11;
        // semi-transparent backing so the label is legible over the waveform
        g2.setColor(new Color(0x0D, 0x0E, 0x17, 0xCC));
        g2.fillRoundRect(rmsX - 3, rmsY - fm.getAscent(), fm.stringWidth(rmsStr) + 6, fm.getHeight(), 4, 4);
        g2.setColor(statusColor);
        g2.drawString(rmsStr, rmsX, rmsY);

        // Static "MIC" badge — top-left
        g2.setColor(new Color(0x0D, 0x0E, 0x17, 0xCC));
        g2.fillRoundRect(4, 3, fm.stringWidth("MIC") + 4, fm.getHeight(), 4, 4);
        g2.setColor(COLOR_LABEL);
        g2.drawString("MIC", 6, 11);
    }

    private void drawLegend(Graphics2D g2, @SuppressWarnings("unused") int w, int h) {
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 9);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        // Three entries: colour swatch + description
        String[] labels = {"Gate closed", "Marginal", "Gate open"};
        Color[] colors = {COLOR_RED, COLOR_AMBER, COLOR_GREEN};

        int swatchSize = fm.getAscent() - 1;
        int rowH = fm.getHeight();
        int entryGap = 14;   // horizontal gap between entries
        int swatchGap = 4;    // gap between swatch and its text

        // Measure total legend width for the backing rect
        int totalW = 0;
        for (int i = 0; i < labels.length; i++) {
            totalW += swatchSize + swatchGap + fm.stringWidth(labels[i]);
            if (i < labels.length - 1) totalW += entryGap;
        }

        int lx = 6;
        int ly = h - 5;   // baseline sits 5px from bottom edge

        // semi-transparent backing
        g2.setColor(new Color(0x0D, 0x0E, 0x17, 0xCC));
        g2.fillRoundRect(lx - 3, ly - fm.getAscent() - 1, totalW + 6, rowH + 2, 4, 4);

        // Draw each swatch + label
        int x = lx;
        for (int i = 0; i < labels.length; i++) {
            // coloured square swatch, vertically centred on the text baseline
            g2.setColor(colors[i]);
            g2.fillRect(x, ly - swatchSize, swatchSize, swatchSize);

            // label text in muted colour
            g2.setColor(COLOR_LABEL);
            g2.drawString(labels[i], x + swatchSize + swatchGap, ly);

            x += swatchSize + swatchGap + fm.stringWidth(labels[i]) + entryGap;
        }
    }

    private void drawClipBadge(Graphics2D g2) {
        if (System.currentTimeMillis() > clipExpiry) return;

        Font f = new Font(Font.MONOSPACED, Font.BOLD, 9);
        g2.setFont(f);
        FontMetrics fm = g2.getFontMetrics();
        String text = "CLIP";
        int tw = fm.stringWidth(text);
        int th = fm.getAscent();
        int px = 30;   // x start (just right of "MIC")
        int py = 3;    // y start (top padding)
        int pw = tw + 8;
        int ph = th + 4;

        // filled red pill
        g2.setColor(COLOR_RED);
        g2.fillRoundRect(px, py, pw, ph, 6, 6);

        // white text centred inside pill
        g2.setColor(Color.WHITE);
        g2.drawString(text, px + 4, py + th);
    }
}
