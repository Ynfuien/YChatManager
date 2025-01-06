package pl.ynfuien.ychatmanager;

import pl.ynfuien.ydevlib.config.ConfigObject;

public enum ConfigName implements ConfigObject.Name {
    LANG,
    CONFIG,
    SWEAR_WORDS,
    SWEAR_WORD_EXCEPTIONS;

    @Override
    public String getFileName() {
        return name().toLowerCase().replace('_', '-') + ".yml";
    }
}
