package inspection.visitor;

import inspection.model.ConfigurationFile;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** Shared config-scanning helper used by the Security and Compliance visitors. */
public final class SecretScanner {
    private static final Pattern SECRET_KEY = Pattern.compile("(?i).*(password|secret|token|api[._-]?key).*");

    private SecretScanner() { }

    /** Keys holding a literal secret; placeholders like ${DB_PASSWORD} are considered safe. */
    public static List<String> plaintextSecretKeys(ConfigurationFile file) {
        return file.properties().entrySet().stream()
                .filter(e -> SECRET_KEY.matcher(e.getKey()).matches())
                .filter(e -> e.getValue() != null && !e.getValue().isBlank() && !e.getValue().startsWith("${"))
                .map(Map.Entry::getKey).sorted().toList();
    }

    public static List<String> insecureSettings(ConfigurationFile file) {
        return file.properties().entrySet().stream()
                .filter(e -> ("debug".equalsIgnoreCase(e.getKey()) && "true".equalsIgnoreCase(e.getValue()))
                        || ("ssl.enabled".equalsIgnoreCase(e.getKey()) && "false".equalsIgnoreCase(e.getValue())))
                .map(e -> e.getKey() + "=" + e.getValue()).sorted().toList();
    }
}
