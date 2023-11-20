package pl.ynfuien.ychatmanager.api.event;

import com.google.common.base.Preconditions;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PrivateMessageSendEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final CommandSender sender;
    private final CommandSender receiver;
    private String message;

    private boolean playSound;
    private boolean cancelled;


    public PrivateMessageSendEvent(@NotNull CommandSender sender, @NotNull CommandSender receiver, @NotNull String message) {
        super(false);

        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    @NotNull
    public CommandSender getSender() {
        return sender;
    }
    @NotNull
    public CommandSender getReceiver() {
        return receiver;
    }
    @NotNull
    public String getMessage() {
        return message;
    }
    public boolean getPlaySound() {
        return playSound;
    }

    public void setMessage(@NotNull String message) throws IllegalArgumentException {
        Preconditions.checkArgument(message != null, "Message cannot be null");
        Preconditions.checkArgument(message.isEmpty(), "Message cannot be empty");
        this.message = message;
    }

    /**
     * Sets whether to play receive sound to a receiver.
     * You can disable it, to play a sound yourself.
     * @param playSound Whether to play a sound
     */
    public void setPlaySound(boolean playSound) {
        this.playSound = playSound;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

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
