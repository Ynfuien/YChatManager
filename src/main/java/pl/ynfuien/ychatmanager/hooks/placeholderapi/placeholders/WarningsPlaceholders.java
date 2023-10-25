package pl.ynfuien.ychatmanager.hooks.placeholderapi.placeholders;

import org.bukkit.OfflinePlayer;
import pl.ynfuien.ychatmanager.hooks.placeholderapi.Placeholder;
import pl.ynfuien.ychatmanager.modules.AntiSwearModule;

public class WarningsPlaceholders implements Placeholder {
    private final AntiSwearModule antiSwearModule;

    public WarningsPlaceholders(AntiSwearModule antiSwearModule) {
        this.antiSwearModule = antiSwearModule;
    }

    @Override
    public String name() {
        return "warnings";
    }

    @Override
    public String getPlaceholder(String id, OfflinePlayer p) {
        Integer count = antiSwearModule.getSwearWarnings().get(p.getUniqueId());

        return count != null ? count.toString() : "0";
    }
}
