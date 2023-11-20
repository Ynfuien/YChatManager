package pl.ynfuien.ychatmanager.api.event;

import com.google.common.base.Preconditions;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pl.ynfuien.ychatmanager.storage.Nickname;

public class NicknameChangeEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final CommandSender executor;
    private final OfflinePlayer target;
    private Nickname nickname;
    private boolean cancelled;


    public NicknameChangeEvent(@NotNull CommandSender executor, @NotNull OfflinePlayer target, @NotNull Nickname nickname) {
        super(false);

        this.executor = executor;
        this.target = target;
        this.nickname = nickname;
    }

    /**
     * Gets command sender that executed command for the nickname change.
     */
    @NotNull
    public CommandSender getExecutor() {
        return executor;
    }
    /**
     * Gets player whose nickname will be changed.
     */
    @NotNull
    public OfflinePlayer getTarget() {
        return target;
    }
    /**
     * Gets nickname object.
     */
    @NotNull
    public Nickname getNickname() {
        return nickname;
    }

    /**
     * Sets nickname that will be used.
     * @param nickname The new nickname object.
     * @throws IllegalArgumentException If any nickname field is null.
     */
    public void setNickname(@NotNull Nickname nickname) throws IllegalArgumentException {
        Preconditions.checkArgument(nickname != null, "Nickname cannot be null");
        Preconditions.checkArgument(nickname.input() != null, "Nickname.input() cannot be empty");
        Preconditions.checkArgument(nickname.serialized() != null, "Nickname.serialized() cannot be empty");

        this.nickname = nickname;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets whether to cancel this event. Note that plugin won't send any message to the target/executor if event gets cancelled.
     * @param cancel true if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
