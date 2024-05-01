package pl.ynfuien.ychatmanager.hooks.placeholderapi.placeholders;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import pl.ynfuien.ychatmanager.hooks.placeholderapi.Placeholder;
import pl.ynfuien.ychatmanager.storage.Nickname;
import pl.ynfuien.ychatmanager.storage.Storage;

public class NicknamePlaceholders implements Placeholder {

    @Override
    public String name() {
        return "nickname";
    }

    @Override
    public String getPlaceholder(String id, OfflinePlayer p) {
        Nickname nick = Storage.getNick(p.getUniqueId());

        // Placeholder: %ycm_nickname_formatted%
        // Returns: formatted nickname
        if (id.equals("formatted")) {
            return LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(nick.serialized()));
        }

        // Placeholder: %ycm_nickname_serialized%
        // Returns: serialized nickname
        if (id.equals("serialized")) {
            return nick.serialized();
        }

        // Placeholder: %ycm_nickname_input%
        // Returns: input nickname
        if (id.equals("input")) {
            return nick.input();
        }

        return null;
    }
}
