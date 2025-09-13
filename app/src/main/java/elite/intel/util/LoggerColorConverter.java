package elite.intel.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

public class LoggerColorConverter extends ForegroundCompositeConverterBase<ILoggingEvent> {
    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        String loggerMessage = event.getMessage();
        if (loggerMessage.contains("VoiceGenerator")) {
            return "35"; // Magenta
        } else if (loggerMessage.contains("Processing sanitizedTranscript")) {
            return "32"; // Green
        } else if (loggerMessage.contains("Sanitized voice command")) {
            return "36"; // Cyan
        } else {
            return "39"; // Default (no color)
        }
    }
}