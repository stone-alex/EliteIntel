package elite.intel.ai.brain.i18n;

import elite.intel.session.Status;

import java.util.Map;

public interface AiActionAliasProvider {

    void addAliases(Map<String, String> map, Status status, boolean isDryRun);
}