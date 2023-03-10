package net.blackofworld.SneakyBastard.Commands;

import lombok.SneakyThrows;
import lombok.experimental.ExtensionMethod;
import net.blackofworld.SneakyBastard.Command.CommandBase;
import net.blackofworld.SneakyBastard.Command.CommandCategory;
import net.blackofworld.SneakyBastard.Command.CommandInfo;
import net.blackofworld.SneakyBastard.Extensions.PlayerExt;
import org.apache.commons.io.FileUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@CommandInfo(command = "debug", description = "Debug shit", Syntax = "<blocks>", category = CommandCategory.Miscellaneous, requiredArgs = 1)
@ExtensionMethod({Player.class, PlayerExt.class})
public class Debug extends CommandBase {
    static {
        try {
            FileUtils.forceMkdir(new File("SneakyBastard/"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private Location first;
    @Override
    public void Execute(Player p, ArrayList<String> args) {
        switch(args.get(0)) {
            case "blocks":
                doBlocks(p);
                break;
            default:
                p.sendHelp(this);
                break;
        }
    }
    @SneakyThrows
    public boolean doBlocks(Player p) {
        Location pl = p.getLocation();
        if(first == null || first.getX() == Double.POSITIVE_INFINITY) {
            first = pl;
            p.sendMessage("First pos set!");
            return true;
        }

        int topBlockX = (first.getBlockX() < pl.getBlockX() ? pl.getBlockX() : first.getBlockX());
        int bottomBlockX = (first.getBlockX() > pl.getBlockX() ? pl.getBlockX() : first.getBlockX());

        int topBlockY = (first.getBlockY() < pl.getBlockY() ? pl.getBlockY() : first.getBlockY());
        int bottomBlockY = (first.getBlockY() > pl.getBlockY() ? pl.getBlockY() : first.getBlockY());

        int topBlockZ = (first.getBlockZ() < pl.getBlockZ() ? pl.getBlockZ() : first.getBlockZ());
        int bottomBlockZ = (first.getBlockZ() > pl.getBlockZ() ? pl.getBlockZ() : first.getBlockZ());

        BufferedWriter log = createLogFile("Blocks");

        for(int x = bottomBlockX; x <= topBlockX; x++)
        {
            for(int z = bottomBlockZ; z <= topBlockZ; z++)
            {
                for(int y = bottomBlockY; y <= topBlockY; y++)
                {
                    int diffBlockX = (topBlockX - x);
                    int diffBlockY = (topBlockY - y);
                    int diffBlockZ = (topBlockZ - z);

                    Block block = pl.getWorld().getBlockAt(x, y, z);
                    log.append(String.format("p.getWorld().getBlockAt(x + %d, y + %d, z + %d).setType(Material.%s);\n", diffBlockX, diffBlockY, diffBlockZ, block.getType()));
                }
            }
        }
        log.close();
        p.Reply("Wrote to SneakyBastard folder!");
        first.setX(Double.POSITIVE_INFINITY);
        return true;
    }

    private BufferedWriter createLogFile(String Action) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("HH-mm");
        String time = sdf.format(new Date());
        return new BufferedWriter(new FileWriter(new File(String.format("SneakyBastard/Debug-%s-%s.txt", Action, time)).getAbsolutePath(), false));
    }
}
