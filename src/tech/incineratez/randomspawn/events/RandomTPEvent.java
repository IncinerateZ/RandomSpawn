package tech.incineratez.randomspawn.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RandomTPEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player p;
    private final Location loc;

    private boolean cancelled = false;

    public RandomTPEvent(Player p, Location loc) {
        this.p = p;
        this.loc = loc;
    }
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = true;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Player getP() {
        return p;
    }

    public Location getLoc() {
        return loc;
    }
}
