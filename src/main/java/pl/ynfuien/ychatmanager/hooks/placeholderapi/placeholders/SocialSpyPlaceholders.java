package pl.ynfuien.ychatmanager.hooks.placeholderapi.placeholders;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import pl.ynfuien.ychatmanager.hooks.placeholderapi.Placeholder;
import pl.ynfuien.ychatmanager.storage.Storage;

public class SocialSpyPlaceholders implements Placeholder {
    @Override
    public String name() {
        return "socialspy";
    }

    @Override
    public String getPlaceholder(String id, OfflinePlayer p) {
        // Placeholder: %ycm_socialspy%
        // Returns: yes, no or N/A
        Player player = p.getPlayer();
        if (player == null) return "N/A";

        boolean state = Storage.getSocialSpy(player);

        return state ? "yes" : "no";
    }
}
