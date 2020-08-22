package tech.incineratez.randomspawn.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.incineratez.randomspawn.RandomSpawn;
import tech.incineratez.randomspawn.utils.ChatUtils;

public class RandomSpawnCommands implements CommandExecutor {
    private RandomSpawn plugin;

    public RandomSpawnCommands(RandomSpawn plugin) {
        this.plugin = plugin;
        plugin.getCommand("rtp").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + this.plugin.getConfig().getString("non-player"));
        } else {
            Player p = (Player)sender;
            if (label.equalsIgnoreCase("rtp")) {
                if (args.length == 0 && p.hasPermission("rtp.tp")) {
                    if (this.plugin.getConfig().getLong("cooldown") > 0L && plugin.customConfig.contains("cooldowns." + p.getName()) && System.currentTimeMillis() - this.plugin.customConfig.getLong("cooldowns." + p.getName()) < 0L) {
                        String timeS = "", timeM = "", timeH = "", timeD = "";
                        long timeMinutes = 0L, timeHours = 0L, timeDays = 0L;
                        long timeSeconds = (long)Math.ceil(((double)this.plugin.customConfig.getLong("cooldowns." + p.getName()) - (double)System.currentTimeMillis()) / 1000.0D);
                        if (timeSeconds >= 60L) {
                            timeMinutes = timeSeconds / 60L;
                            timeSeconds = timeSeconds % 60L;
                            if (timeMinutes >= 60L) {
                                timeHours = timeMinutes / 60L;
                                timeMinutes = timeMinutes % 60L;
                                if (timeHours >= 24L) {
                                    timeDays = timeHours / 24L;
                                    timeHours = timeHours % 24L;
                                }
                            }
                        }

                        if (timeSeconds != 0L) {
                            timeS = timeSeconds + " Seconds";
                        }

                        if (timeMinutes != 0L) {
                            timeM = timeMinutes + " Minutes ";
                        }

                        if (timeHours != 0L) {
                            timeH = timeHours + " Hours ";
                        }

                        if (timeDays != 0L) {
                            timeD = timeDays + " Days ";
                        }

                        String finalTime = timeD + timeH + timeM + timeS;
                        p.sendMessage(ChatUtils.colorFormat(this.plugin.getConfig().getString("in-cooldown").replace("%time%", finalTime)));
                        return false;
                    }
                    p.sendMessage(ChatUtils.colorFormat(this.plugin.getConfig().getString("tp-request")));
                    this.plugin.tpRandom(p);
                    this.plugin.customConfig.set("cooldowns." + p.getName(), this.plugin.getConfig().getLong("cooldown") + System.currentTimeMillis());
                    this.plugin.saveCustomYml(this.plugin.customConfig, this.plugin.cooldowns);
                } else {
                    double posX;
                    double posZ;
                    if (args[0].equalsIgnoreCase("pos1") && p.hasPermission("rtp.setpos")) {
                        posX = p.getLocation().getX();
                        posZ = p.getLocation().getZ();
                        posX = Math.floor(posX);
                        posZ = Math.floor(posZ);
                        this.plugin.getConfig().set("pos1.x", posX);
                        this.plugin.getConfig().set("pos1.z", posZ);
                        this.plugin.saveConfig();
                        this.plugin.reloadConfig();
                        p.sendMessage(ChatUtils.colorFormat("&a[Random Spawn] Position 1 Set!"));
                    } else if (args[0].equalsIgnoreCase("pos2") && p.hasPermission("rtp.setpos")) {
                        posX = p.getLocation().getX();
                        posZ = p.getLocation().getZ();
                        posX = Math.floor(posX);
                        posZ = Math.floor(posZ);
                        this.plugin.getConfig().set("pos2.x", posX);
                        this.plugin.getConfig().set("pos2.z", posZ);
                        this.plugin.saveConfig();
                        this.plugin.reloadConfig();
                        p.sendMessage(ChatUtils.colorFormat("&a[Random Spawn] Position 2 Set!"));
                    } else if (args.length > 0) {
                        p.sendMessage(ChatUtils.colorFormat(this.plugin.getConfig().getString("unknown-args")));
                    } else {
                        p.sendMessage(ChatUtils.colorFormat(this.plugin.getConfig().getString("no-permission")));
                    }
                }
            }
        }

        return true;
    }
}
