package pl.ynfuien.ychatmanager.config;

public enum ConfigName {
    LANG,
    CONFIG,
    SWEAR_WORDS,
    SWEAR_WORD_EXCEPTIONS;

    String getFileName() {
        return name().toLowerCase().replace('_', '-') + ".yml";
    }
}
