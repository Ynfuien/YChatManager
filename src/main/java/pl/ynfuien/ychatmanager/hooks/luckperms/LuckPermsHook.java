package pl.ynfuien.ychatmanager.hooks.luckperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.modules.DisplayNameModule;
import pl.ynfuien.ychatmanager.utils.Logger;

import java.util.Collection;

// Hooks into user and group change event, and updates players display names,
// to keep prefixes and suffixed up to date.
public class LuckPermsHook {
    private final YChatManager instance;
    private final DisplayNameModule displayNameModule;
    private BukkitTask userTask = null;
    private BukkitTask groupTask = null;

    public LuckPermsHook(YChatManager instance) {
        this.instance = instance;
        this.displayNameModule = instance.getModules().getDisplaynameModule();

        LuckPerms lp = LuckPermsProvider.get();
        EventBus eventBus = lp.getEventBus();

        eventBus.subscribe(instance, UserDataRecalculateEvent.class, e -> {
            if (!displayNameModule.isEnabled()) return;

            // Using scheduler to ignore multiple events
            // (UserDataRecalculate firing a few times at the same time)
            if (userTask != null) userTask.cancel();
            userTask = Bukkit.getScheduler().runTaskLater(instance, () -> {
                Player p = Bukkit.getPlayer(e.getUser().getUniqueId());
                if (p == null || !p.isOnline()) return;

                displayNameModule.updateDisplayname(p);
                Logger.log("<dark_purple>Update for the player " + p.getName());
            }, 0);
        });


        eventBus.subscribe(instance, GroupDataRecalculateEvent.class, e -> {
            if (!displayNameModule.isEnabled()) return;

            // Using scheduler to ignore multiple events
            // (GroupDataRecalculateEvent firing a few times at the same time)
            // And because in the moment of an event, player's prefix/suffix
            // isn't yet changed.
            if (groupTask != null) groupTask.cancel();
            groupTask = Bukkit.getScheduler().runTaskLater(instance, () -> {
                Group group = e.getGroup();
                Logger.log("<light_purple>Update for the group " + group.getName() + ":");

                for (Player p : Bukkit.getOnlinePlayers()) {
                    User user = lp.getPlayerAdapter(Player.class).getUser(p);
                    Collection<Group> groups = user.getInheritedGroups(user.getQueryOptions());

                    if (!groups.contains(group)) continue;
                    displayNameModule.updateDisplayname(p);
                    Logger.log("<dark_purple>Group update for player " + p.getName());
                }
            }, 0);
        });
    }
}
