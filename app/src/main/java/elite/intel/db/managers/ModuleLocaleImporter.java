package elite.intel.db.managers;

import elite.intel.db.dao.SubSystemDao;
import elite.intel.db.util.Database;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Reads the bundled locale/modules.csv at startup and populates the locale label columns
 * (label_es, label_fr, label_pt, label_ru) in the sub_system table.
 * <p>
 * The CSV format is:
 * messageKey, default_en, en, de, es, fr, pt, ru, zh, ka
 * <p>
 * For each CSV row the messageKey is stripped of its "ships.module.name." prefix to produce
 * a machine_key candidate. The sub_system table is then updated for any row whose machine_key
 * is a substring of that candidate (i.e. the same LIKE matching used at journal parse time).
 * Only the first non-empty locale value per sub_system row is written — subsequent rows for
 * the same category (different class/size variants with identical translations) are no-ops.
 * <p>
 * V1.1 note: extend this class to handle language selection and additional locale columns.
 */
public class ModuleLocaleImporter {

    private static final Logger log = LogManager.getLogger(ModuleLocaleImporter.class);
    private static final String CSV_RESOURCE = "/locale/modules.csv";

    // CSV column indices (0-based)
    private static final int COL_MESSAGE_KEY = 0;
    private static final int COL_ES = 4;
    private static final int COL_FR = 5;
    private static final int COL_PT = 6;
    private static final int COL_RU = 7;

    private static final String KEY_PREFIX = "ships.module.name.";

    public void importLocales() {
        List<String> machineKeys = Database.withDao(SubSystemDao.class, SubSystemDao::getAllMachineKeys);
        if (machineKeys.isEmpty()) {
            log.warn("No machine_keys found in sub_system table - skipping locale import");
            return;
        }

        try (InputStream is = getClass().getResourceAsStream(CSV_RESOURCE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            reader.readLine(); // skip header
            String line;
            int updated = 0;

            while ((line = reader.readLine()) != null) {
                String[] cols = parseCsvLine(line);
                if (cols.length <= COL_RU) continue;

                String messageKey = unquote(cols[COL_MESSAGE_KEY]);
                if (!messageKey.startsWith(KEY_PREFIX)) continue;

                // Derive the machine_key candidate: strip the "ships.module.name." prefix
                String candidate = messageKey.substring(KEY_PREFIX.length());

                String es = unquote(cols[COL_ES]);
                String fr = unquote(cols[COL_FR]);
                String pt = unquote(cols[COL_PT]);
                String ru = unquote(cols[COL_RU]);

                if (es.isEmpty() && fr.isEmpty() && pt.isEmpty() && ru.isEmpty()) continue;

                int rows = Database.withDao(SubSystemDao.class, dao -> {
                    dao.updateLocaleLabels(candidate, es.isEmpty() ? null : es,
                            fr.isEmpty() ? null : fr,
                            pt.isEmpty() ? null : pt,
                            ru.isEmpty() ? null : ru);
                    return 0;
                });
                updated++;
            }

            log.info("Module locale import complete: processed {} CSV rows", updated);

        } catch (Exception e) {
            log.error("Failed to import module locale data from {}", CSV_RESOURCE, e);
        }
    }

    private static String[] parseCsvLine(String line) {
        // Simple CSV split — fields are quoted with double-quotes, no embedded commas or escaped quotes
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    private static String unquote(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) s = s.substring(1, s.length() - 1);
        return s.trim();
    }
}
