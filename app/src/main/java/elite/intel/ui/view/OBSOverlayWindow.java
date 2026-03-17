package elite.intel.ui.view;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.UserInputEvent;
import elite.intel.ui.event.AiResponseLogEvent;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class OBSOverlayWindow extends JFrame {

    private static final Color BG = new Color(0, 0, 0);
    private static final Color FG = new Color(0xFF8C00); // orange

    private final JTextArea textArea;
    private final Timer typewriterTimer;
    private final StringBuilder buffer = new StringBuilder();
    private final AtomicBoolean typewriterActive = new AtomicBoolean(false);

    public OBSOverlayWindow() {
        setTitle("Elite Intel OBS Overlay");
        setUndecorated(true);
        setAlwaysOnTop(false);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(900, 220);
        setLocationRelativeTo(null);

        Font monoFont;
        try {
            monoFont = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(
                    getClass().getResourceAsStream("/fonts/Electrolize-Regular.ttf"))
            ).deriveFont(20f);
        } catch (FontFormatException | IOException e) {
            monoFont = new Font(Font.MONOSPACED, Font.PLAIN, 20);
        }

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setFont(monoFont);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(BG);
        textArea.setForeground(FG);
        textArea.setCaretColor(BG);
        textArea.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JScrollPane scroll = new JScrollPane(textArea,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);

        getContentPane().setBackground(BG);
        getContentPane().add(new JLabel(" "), BorderLayout.NORTH); /// cosmetics
        getContentPane().add(scroll, BorderLayout.CENTER);

        typewriterTimer = new Timer(30, e -> tick());

        EventBusManager.register(this);
    }

    @Subscribe
    public void onUserInputEvent(UserInputEvent event) {
        if (event.getUserInput() == null || event.getUserInput().isBlank()) return;
        enqueue("User: " + event.getUserInput() + "\n\n");
    }

    @Subscribe
    public void onAiResponseLogEvent(AiResponseLogEvent event) {
        if (event.getData() == null || event.getData().isBlank()) return;
        enqueue("AI: " + event.getData() + "\n\n");
    }

    private void enqueue(String text) {
        synchronized (buffer) {
            buffer.append(text);
        }
        if (typewriterActive.compareAndSet(false, true)) {
            SwingUtilities.invokeLater(typewriterTimer::start);
        }
    }

    private void tick() {
        String nextChar;
        synchronized (buffer) {
            if (buffer.isEmpty()) {
                typewriterTimer.stop();
                typewriterActive.set(false);
                return;
            }
            nextChar = String.valueOf(buffer.charAt(0));
            buffer.deleteCharAt(0);
        }
        textArea.append(nextChar);
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}