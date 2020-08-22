package tech.incineratez.randomspawn.runnables;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import tech.incineratez.randomspawn.RandomSpawn;

public class RetrieverRunnable implements Runnable {
    RandomSpawn plugin;
    public RetrieverRunnable(RandomSpawn plugin) {
        this.plugin = plugin;
    }

    private void tp(Player p, Location loc) {
        plugin.tp(p, loc);
    }

    @Override
    public void run() {
        if(!plugin.foundLocs.isEmpty()) {
            for(Player p : plugin.foundLocs.keySet()) {
                Location loc = plugin.foundLocs.get(p);
                if(plugin.forceTps.containsKey(p)) {
                    int offset = 0;
                    if(plugin.MCVERSION.equalsIgnoreCase("1.16")) offset = 1;
                    Location glass = plugin.foundLocs.get(p).add(0, 0 + offset, 0);
                    glass.getWorld().getBlockAt(glass).setType(Material.GLASS);
                    plugin.forceTps.remove(p);
                    loc.add(0, 2, 0);
                }
                tp(p, plugin.foundLocs.get(p));
                plugin.foundLocs.remove(p);
            }
        }
        if(!plugin.pEffects.isEmpty()) {
            for(Player p : plugin.pEffects.keySet()) {
                p.addPotionEffect(plugin.pEffects.get(p));
                plugin.pEffects.remove(p);
            }
        }
    }
}
