package net.blackofworld.SneakyBastard.Command;

import net.blackofworld.SneakyBastard.Start;
import net.blackofworld.SneakyBastard.Start.Config;
import net.blackofworld.SneakyBastard.Utils.Packets.IPacket;
import net.blackofworld.SneakyBastard.Utils.Packets.PacketInject.PacketEvent;
import net.blackofworld.SneakyBastard.Utils.Packets.PacketType;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.util.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static net.blackofworld.SneakyBastard.Utils.Packets.PacketInject.PacketListener;

public class CommandListener implements Listener, PacketListener {
    final HashMap<String, ArrayList<Tuple<Object, Method>>> events = new HashMap<>();
    final EventExecutor executor = (listener, event) -> {
        try {
            Class<?> clazz = event.getClass();
            do {
                ArrayList<Tuple<Object, Method>> methods;
                if ((methods = events.get(clazz.getSimpleName())) != null) {
                    methods.forEach((method -> {
                        try {
                            method.getB().invoke(method.getA(), event);
                        } catch (IllegalAccessException | InvocationTargetException ignored) {
                        }
                    }));
                    return;
                } else {
                    clazz = clazz.getSuperclass();
                }
            } while (clazz != Object.class);
        } catch (Throwable var5) {
            throw new EventException(var5);
        }
    };

    @SuppressWarnings("unchecked")
    CommandListener() {
        for (CommandBase cmd : CommandManager.Instance.commandList) {
            for (Method m : cmd.getClass().getDeclaredMethods()) {
                if (m.getParameterCount() == 1) {
                    var param = m.getParameters()[0];
                    boolean isValidEvent = true;
                    Class<?> isEventType = param.getType();
                    do {
                        isEventType = isEventType.getSuperclass();
                        if (isEventType == null || isEventType.equals(Object.class)) {
                            isValidEvent = false;
                            break;
                        }
                    } while (!isEventType.equals(Event.class));
                    if (!isValidEvent) break;
                    Bukkit.getPluginManager().registerEvent((Class<? extends Event>) param.getType(), this, EventPriority.LOW, executor, Start.Instance);
                    ArrayList<Tuple<Object, Method>> methods;
                    Tuple<Object, Method> tuple = new Tuple<>(cmd, m);
                    if ((methods = events.get(param.getType().getSimpleName())) != null) {
                        methods.add(tuple);
                        events.replace(param.getType().getSimpleName(), methods);
                    } else {
                        events.put(param.getType().getSimpleName(), new ArrayList<>(Collections.singleton(tuple)));
                    }
                }
            }
        }
    }

    @IPacket(direction = PacketType.INCOMING)
    public void logIncomingPacket(PacketEvent event) {
        if (!Config.LogPackets) return;
        var packet = event.packet;
        Bukkit.getScheduler().runTask(Start.Instance, () -> Bukkit.broadcastMessage(packet.toString()));
    }

    @IPacket(direction = PacketType.OUTGOING)
    public void logOutboundPacket(PacketEvent event) {
        if (!Config.LogPackets) return;
        var packet = event.packet;
        if (packet.getClass().equals(ClientboundSystemChatPacket.class)) return;
        Bukkit.getScheduler().runTask(Start.Instance, () -> Bukkit.broadcastMessage(packet.toString()));
    }

    public void Destroy() {
        events.clear();
    }
}
