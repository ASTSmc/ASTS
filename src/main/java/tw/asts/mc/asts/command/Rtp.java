package tw.asts.mc.asts.command;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import tw.asts.mc.asts.util.PluginPermission;
import tw.asts.mc.asts.util.action.CommandAction;
import tw.asts.mc.asts.util.action.CommandExecuteAction;
import tw.asts.mc.asts.util.text;

import java.util.*;

public final class Rtp extends CommandAction {

    private final List<String> disabledWorlds;
    private final boolean disabledRadiusFar;
    private final int radiusDefault;
    private final int radiusFar;
    private final int cooldownTime = 10;
    private final Plugin plugin;
    private final Map<String, Long> cooldowns = new HashMap<>();

    public Rtp(FileConfiguration config, Plugin plugin) {
        super("隨機傳送");
        this.disabledWorlds = config.getStringList("rtp.disable.worlds");
        this.disabledRadiusFar = config.getBoolean("rtp.disable.far");
        this.radiusDefault = config.getInt("rtp.radius.default");
        this.radiusFar = config.getInt("rtp.radius.far");
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String @NotNull [] args) {
        final CommandExecuteAction executeAction = executeAction(stack, args);
        if (executeAction.onlyPlayer()) {
            return;
        } else if (disabledWorlds.contains(Objects.requireNonNull(stack.getExecutor()).getWorld().getName())) {
            executeAction.sendMessage(text.t("asts.cmd.rtp.disabledWorld"), true);
            return;
        } else if (args.length > 1) {
            executeAction.sendMessage(text.l("asts.cmd.unknownArg"), true);
            return;
        }
        int max = radiusDefault;
        int minY = 64;
        if (args.length == 1) {
            String arg = args[0];
            if (arg.equals("cave")) {
                minY = -60;
            } else if (arg.equals("far")) {
                if (disabledRadiusFar) {
                    executeAction.sendMessage(text.t("asts.cmd.rtp.disabledFar"), true);
                    return;
                }
                max = radiusFar;
            } else {
                executeAction.sendMessage(text.t("asts.cmd.unknownArg"), true);
                return;
            }
        }
        if (cooldowns.containsKey(executeAction.getPlayerName()) && cooldowns.get(executeAction.getPlayerName()) - System.currentTimeMillis() / 1000 < cooldownTime * 1000L) {
            final long timeLeft = (cooldowns.get(executeAction.getPlayerName()) + cooldownTime * 1000L - System.currentTimeMillis()) / 1000;
            executeAction.sendMessage(text.t("asts.cmd.waitSeconds", text.l(String.valueOf(timeLeft))), true);
            return;
        }
        cooldowns.put(executeAction.getPlayerName(), System.currentTimeMillis());
        final World world = Objects.requireNonNull(executeAction.getExecutor()).getWorld();
        if (world.getEnvironment() == World.Environment.NETHER) {
            minY = 32;
        }
        int finalMinY = minY;
        int finalMax = max;
        new BukkitRunnable() {
            @Override
            public void run() {
                int retry = 0;
                List<Material> unsafeBlocks = List.of(Material.CACTUS, Material.COBWEB, Material.MAGMA_BLOCK, Material.SWEET_BERRY_BUSH);
                boolean teleported = false;
                int x = (int) (Math.random() * finalMax * 2) - finalMax;
                final int xInit = x;
                int z = (int) (Math.random() * finalMax * 2) - finalMax;
                while(!teleported && stack.getExecutor().isValid()){
                    x+=1;
                    if (retry>256){
                        z+=1;
                        x = xInit;
                        retry=0;
                    }

//
//                    int x = (int) (Math.random() * finalMax * 2) - finalMax;
//                    int z = (int) (Math.random() * finalMax * 2) - finalMax;
                    retry++;
                    int y = world.getHighestBlockYAt(x, z);
                    if (y < finalMinY) continue;

                    if (world.getEnvironment() == World.Environment.NETHER || (args.length == 1 && args[0].equals("cave"))) {
                        while (y >= finalMinY) {
                            y--;
                            if (world.getBlockAt(x, y + 1, z).getType().isAir() && world.getBlockAt(x, y + 2, z).getType().isAir()) {
                                break;
                            }
                        }
                        if (y < finalMinY) continue;
                    }

                    final Location blockLoc = new Location(world, x, y, z);
                    if (!world.getBlockAt(blockLoc).getType().isSolid() || unsafeBlocks.contains(world.getBlockAt(blockLoc).getType())) {
                        continue;
                    }

                    final Location teleportLoc = new Location(world, x + 0.5, y + 1, z + 0.5);
                    String locStr = x + ", " + y + ", " + z;

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            executeAction.sendMessage(text.t("asts.cmd.tp.teleportTo", text.l(locStr)), true);
                            executeAction.teleport(teleportLoc);
                        }
                    }.runTask(plugin);
                    teleported = true;
                }
            }
        }.runTaskAsynchronously(plugin);
    }
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String @NotNull [] args) {
        if (args.length <= 1) {
            List<String> arg1 = List.of("cave", "far");
            if (args.length == 1) {
                return arg1.stream().filter(a -> a.toLowerCase().startsWith(args[0].toLowerCase())).toList();
            }
            return arg1;
        }
        return List.of();
    }

    @Override
    public String permission() {
        return PluginPermission.commandRtp;
    }
}