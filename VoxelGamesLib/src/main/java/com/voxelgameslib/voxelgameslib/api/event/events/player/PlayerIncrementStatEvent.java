package com.voxelgameslib.voxelgameslib.api.event.events.player;

import javax.annotation.Nonnull;

import com.voxelgameslib.voxelgameslib.api.stats.Trackable;
import com.voxelgameslib.voxelgameslib.components.user.User;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class PlayerIncrementStatEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled = false;
    private Trackable statType;
    private double oldVal;
    private double newVal;
    private double incrementAmount;

    public PlayerIncrementStatEvent(@Nonnull User user, Trackable statType, double oldVal, double newVal, double incrementAmount) {
        super(user);
        this.statType = statType;
        this.oldVal = oldVal;
        this.newVal = newVal;
        this.incrementAmount = incrementAmount;
    }

    @Nonnull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    @Nonnull
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }

    public Trackable getStat() {
        return statType;
    }

    public double getOldVal() {
        return oldVal;
    }

    public double getNewVal() {
        return newVal;
    }

    public void setNewVal(double newVal) {
        this.newVal = newVal;
    }

    public double getIncrementAmount() {
        return incrementAmount;
    }
}
