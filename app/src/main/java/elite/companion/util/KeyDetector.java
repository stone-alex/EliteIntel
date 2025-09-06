package elite.companion.util;

import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.ui.event.AppLogEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class KeyDetector {
    private static final Map<ProviderEnum, Pattern> PATTERNS = Map.ofEntries(
            Map.entry(ProviderEnum.GROK, Pattern.compile("^[a-zA-Z0-9]{64}$")),
            Map.entry(ProviderEnum.GOOGLE_STT, Pattern.compile("^AIzaSy[a-zA-Z0-9_-]{33}$")),
            Map.entry(ProviderEnum.GOOGLE_TTS, Pattern.compile("^AIzaSy[a-zA-Z0-9_-]{33}$")),
            Map.entry(ProviderEnum.ANTHROPIC, Pattern.compile("^sk-ant-[a-zA-Z0-9-]{80,90}$")),
            Map.entry(ProviderEnum.AWS_LLM, Pattern.compile("^(AKIA|ASIA)[A-Z0-9]{16}$")),
            Map.entry(ProviderEnum.AWS_STT, Pattern.compile("^(AKIA|ASIA)[A-Z0-9]{16}$")),
            Map.entry(ProviderEnum.AWS_TTS, Pattern.compile("^(AKIA|ASIA)[A-Z0-9]{16}$")),
            Map.entry(ProviderEnum.AZURE_LLM, Pattern.compile("^[0-9a-f]{32}$")),
            Map.entry(ProviderEnum.AZURE_STT, Pattern.compile("^[0-9a-f]{32}$")),
            Map.entry(ProviderEnum.AZURE_TTS, Pattern.compile("^[0-9a-f]{32}$")),
            Map.entry(ProviderEnum.ELEVEN_LABS, Pattern.compile("^[0-9a-fA-F]{32}$")),
            Map.entry(ProviderEnum.DEEP_GRAM, Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")),
            Map.entry(ProviderEnum.IBM_WATSON_STT, Pattern.compile("^[a-zA-Z0-9+/=]{40,50}$")),
            Map.entry(ProviderEnum.IBM_WATSON_TTS, Pattern.compile("^[a-zA-Z0-9+/=]{40,50}$"))
    );

    public static ProviderEnum detectProvider(String key, String category) {
        List<ProviderEnum> matches = new ArrayList<>();
        for (Map.Entry<ProviderEnum, Pattern> entry : PATTERNS.entrySet()) {
            if (entry.getValue().matcher(key).matches() && entry.getKey().supportsCategory(category)) {
                matches.add(entry.getKey());
            }
        }
        if (matches.size() == 1) return matches.get(0);
        if (matches.size() > 1) {
            EventBusManager.publish(new AppLogEvent("Ambiguous key matches: " + matches));
            EventBusManager.publish(new VoiceProcessEvent("Multiple providers detected—say provider name"));
        }
        return ProviderEnum.UNKNOWN;
    }
}