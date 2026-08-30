package com.earth2me.essentials.utils;

import io.papermc.lib.PaperLib;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public final class BedSpawnLocationUtil {
    private BedSpawnLocationUtil() {
    }

    public static CompletableFuture<Location> getBedSpawnLocationAsync(final Player player, final boolean urgent) {
        if (!PaperLib.isPaper()) {
            return PaperLib.getBedSpawnLocationAsync(player, urgent);
        }
        return getBedSpawnLocationAsync(player, urgent, PaperLib::getChunkAtAsync);
    }

    static CompletableFuture<Location> getBedSpawnLocationAsync(final Player player, final boolean urgent, final AsyncChunkLoader chunkLoader) {
        final RespawnTarget target = RespawnTarget.from(player.getPotentialBedLocation());
        if (target == null) {
            return CompletableFuture.completedFuture(null);
        }

        return chunkLoader.getChunkAtAsync(target.world, target.x >> 4, target.z >> 4, false, urgent).thenApply(chunk -> {
            if (chunk == null || !target.matches(player.getPotentialBedLocation())) {
                return null;
            }
            return player.getBedSpawnLocation();
        });
    }

    @FunctionalInterface
    interface AsyncChunkLoader {
        CompletableFuture<Chunk> getChunkAtAsync(World world, int x, int z, boolean generate, boolean urgent);
    }

    private static final class RespawnTarget {
        private final World world;
        private final int x;
        private final int y;
        private final int z;

        private RespawnTarget(final World world, final int x, final int y, final int z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private static RespawnTarget from(final Location location) {
            if (location == null || location.getWorld() == null) {
                return null;
            }
            return new RespawnTarget(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }

        private boolean matches(final Location location) {
            return location != null
                    && location.getWorld() == world
                    && location.getBlockX() == x
                    && location.getBlockY() == y
                    && location.getBlockZ() == z;
        }
    }
}
