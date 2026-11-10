package inspection.model;

/** A single dependency entry; version may be null/blank when not pinned. */
public record Dependency(String name, String version, boolean knownVulnerable) {
    public boolean hasVersion() {
        return version != null && !version.isBlank();
    }
}
