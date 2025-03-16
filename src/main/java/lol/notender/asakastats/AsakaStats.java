package lol.notender.asakastats;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;

public final class AsakaStats extends JavaPlugin implements CommandExecutor {

    private DataStorage dataStorage;
    private Logger logger;

    @Override
    public void onEnable() {
        this.logger = getLogger();
        this.dataStorage = new JsonFileStorage(this, logger);
        new AsakaStatsExpansion(dataStorage).register();

        Bukkit.getPluginManager().registerEvents(new PlayerKillListener(dataStorage), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(dataStorage), this);

        this.getCommand("stats").setExecutor(new StatsCommand(dataStorage));
        this.getCommand("resetstats").setExecutor(new ResetStatsCommand(dataStorage));
        this.getCommand("asakastats").setExecutor(this);

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage("§f");
        console.sendMessage("§bAsakaStats §ev" + getDescription().getVersion());
        console.sendMessage("§eAuthor: §fNotEnder §7(NotEnderVN)");
        console.sendMessage("§f");
    }

    @Override
    public void onDisable() {
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage("§eThank you for using AsakaStats");
        this.dataStorage.close();
    }

    private void logInfo(String message) {
        logger.info("§f" + message);
    }

    interface DataStorage {
        void registerPlayer(Player player);
        void recordKill(Player player);
        void recordDeath(Player player);
        int getKills(Player player);
        int getDeaths(Player player);
        double getKDR(Player player);
        int getKillstreak(Player player);
        int getTopKillstreak(Player player);
        void resetPlayerStats(Player player);
        void close();
    }

    static class JsonFileStorage implements DataStorage {
        private final AsakaStats plugin;
        private final File dataFolder;
        private final Gson gson = new Gson();
        private final Logger logger;

        public JsonFileStorage(AsakaStats plugin, Logger logger) {
            this.plugin = plugin;
            this.dataFolder = new File(plugin.getDataFolder(), "data");
            this.logger = logger;
            if (!this.dataFolder.exists()) {
                if (this.dataFolder.mkdirs()) {
                    logger.info("Data folder created.");
                } else {
                    logger.warning("Failed to create data folder.");
                }
            }
        }

        @Override
        public void registerPlayer(Player player) {
            File playerFile = new File(dataFolder, player.getUniqueId() + ".json");
            if (!playerFile.exists()) {
                JsonObject playerData = new JsonObject();
                playerData.addProperty("name", player.getName());
                playerData.addProperty("kills", 0);
                playerData.addProperty("deaths", 0);
                playerData.addProperty("killstreak", 0);
                playerData.addProperty("topkillstreak", 0);
                playerData.addProperty("kdr", 0.0);

                savePlayerData(playerFile, playerData);
            }
        }

        @Override
        public void recordKill(Player player) {
            updatePlayerData(player, playerData -> {
                int kills = playerData.get("kills").getAsInt() + 1;
                int killstreak = playerData.get("killstreak").getAsInt() + 1;
                int topKillstreak = Math.max(playerData.get("topkillstreak").getAsInt(), killstreak);
                int deaths = playerData.get("deaths").getAsInt();
                double kdr = deaths == 0 ? kills : (double) kills / deaths;
                playerData.addProperty("kills", kills);
                playerData.addProperty("killstreak", killstreak);
                playerData.addProperty("topkillstreak", topKillstreak);
                playerData.addProperty("kdr", kdr);
            });
        }

        @Override
        public void recordDeath(Player player) {
            updatePlayerData(player, playerData -> {
                int deaths = playerData.get("deaths").getAsInt() + 1;
                int kills = playerData.get("kills").getAsInt();
                double kdr = kills == 0 ? 0.0 : (double) kills / deaths;
                playerData.addProperty("deaths", deaths);
                playerData.addProperty("killstreak", 0);
                playerData.addProperty("kdr", kdr);
            });
        }

        @Override
        public int getKills(Player player) {
            return getPlayerData(player, "kills", 0);
        }

        @Override
        public int getDeaths(Player player) {
            return getPlayerData(player, "deaths", 0);
        }

        @Override
        public double getKDR(Player player) {
            return getPlayerData(player, "kdr", 0.0);
        }

        @Override
        public int getKillstreak(Player player) {
            return getPlayerData(player, "killstreak", 0);
        }

        @Override
        public int getTopKillstreak(Player player) {
            return getPlayerData(player, "topkillstreak", 0);
        }

        @Override
        public void resetPlayerStats(Player player) {
            updatePlayerData(player, playerData -> {
                playerData.addProperty("kills", 0);
                playerData.addProperty("deaths", 0);
                playerData.addProperty("killstreak", 0);
                playerData.addProperty("topkillstreak", 0);
                playerData.addProperty("kdr", 0.0);
            });
        }

        private void updatePlayerData(Player player, java.util.function.Consumer<JsonObject> updater) {
            File playerFile = new File(dataFolder, player.getUniqueId() + ".json");
            if (playerFile.exists()) {
                JsonObject playerData = loadPlayerData(playerFile);
                updater.accept(playerData);
                savePlayerData(playerFile, playerData);
            }
        }

        private <T> T getPlayerData(Player player, String key, T defaultValue) {
            File playerFile = new File(dataFolder, player.getUniqueId() + ".json");
            if (playerFile.exists()) {
                JsonObject playerData = loadPlayerData(playerFile);
                if (playerData.has(key)) {
                    if (defaultValue instanceof Integer) {
                        return (T) Integer.valueOf(playerData.get(key).getAsInt());
                    } else if (defaultValue instanceof Double) {
                        return (T) Double.valueOf(playerData.get(key).getAsDouble());
                    }
                }
            }
            return defaultValue;
        }

        private JsonObject loadPlayerData(File playerFile) {
            try (Reader reader = new InputStreamReader(new FileInputStream(playerFile), StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error loading player data from " + playerFile.getName(), e);
                return new JsonObject();
            }
        }

        private void savePlayerData(File playerFile, JsonObject playerData) {
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(playerFile), StandardCharsets.UTF_8);
                 JsonWriter jsonWriter = new JsonWriter(writer)) {
                gson.toJson(playerData, jsonWriter);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error saving player data to " + playerFile.getName(), e);
            }
        }

        @Override
        public void close() {}
    }

    static class PlayerJoinListener implements Listener {
        private final DataStorage dataStorage;

        public PlayerJoinListener(DataStorage dataStorage) {
            this.dataStorage = dataStorage;
        }

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            dataStorage.registerPlayer(event.getPlayer());
        }
    }

    static class PlayerKillListener implements Listener {
        private final DataStorage dataStorage;

        public PlayerKillListener(DataStorage dataStorage) {
            this.dataStorage = dataStorage;
        }

        @EventHandler
        public void onPlayerKill(EntityDamageByEntityEvent event) {
            if (event.getEntity() instanceof Player victim && event.getDamager() instanceof Player killer) {
                if (victim.getHealth() - event.getFinalDamage() <= 0) {
                    dataStorage.recordKill(killer);
                }
            }
        }

        @EventHandler
        public void onPlayerDeath(PlayerDeathEvent event) {
            Player victim = event.getEntity();
            dataStorage.recordDeath(victim);
        }
    }

    static class AsakaStatsExpansion extends PlaceholderExpansion {
        private final DataStorage dataStorage;

        public AsakaStatsExpansion(DataStorage dataStorage) {
            this.dataStorage = dataStorage;
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public boolean canRegister() {
            return true;
        }

        @Override
        public @NotNull String getIdentifier() {
            return "asakastats";
        }

        @Override
        public @NotNull String getAuthor() {
            return "NotEnder";
        }

        @Override
        public @NotNull String getVersion() {
            return "1.2";
        }

        @Override
        public String onPlaceholderRequest(Player player, String identifier) {
            if (player == null) {
                return "";
            }

            switch (identifier.toLowerCase()) {
                case "kills":
                    return String.valueOf(dataStorage.getKills(player));
                case "deaths":
                    return String.valueOf(dataStorage.getDeaths(player));
                case "kdr":
                    return String.format("%.2f", dataStorage.getKDR(player));
                case "killstreak":
                    return String.valueOf(dataStorage.getKillstreak(player));
                case "topkillstreak":
                    return String.valueOf(dataStorage.getTopKillstreak(player));
                default:
                    return null;
            }
        }
    }

    static class StatsCommand implements CommandExecutor {
        private final DataStorage dataStorage;

        public StatsCommand(DataStorage dataStorage) {
            this.dataStorage = dataStorage;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cThis command can only be executed by players.");
                return true;
            }

            player.sendMessage("§3§lStats:");
            player.sendMessage("§bKills: §f" + dataStorage.getKills(player));
            player.sendMessage("§bDeaths: §f" + dataStorage.getDeaths(player));
            player.sendMessage("§bKDR: §f" + String.format("%.2f", dataStorage.getKDR(player)));
            player.sendMessage("§bKillstreak: §f" + dataStorage.getKillstreak(player));
            player.sendMessage("§bTop Killstreak: §f" + dataStorage.getTopKillstreak(player));
            return true;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("asakastats")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.sendMessage("§f");
                player.sendMessage("§bAsakaStats §ev" + getDescription().getVersion());
                player.sendMessage("§eAuthor: §fNotEnder §7(NotEnderVN)");
                player.sendMessage("§f");
            } else {
                sender.sendMessage("");
                sender.sendMessage("AsakaStats §ev" + getDescription().getVersion());
                sender.sendMessage("Author: §fNotEnder §7(NotEnderVN)");
                sender.sendMessage("");
            }
            return true;
        }
        return false;
    }

    static class ResetStatsCommand implements CommandExecutor {
        private final DataStorage dataStorage;

        public ResetStatsCommand(DataStorage dataStorage) {
            this.dataStorage = dataStorage;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!sender.hasPermission("asakastats.admin")) {
                sender.sendMessage("§cYou don't have permission to execute this command.");
                return true;
            }

            if (args.length != 1) {
                sender.sendMessage("§cUsage: §f/resetstats <player>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found.");
                return true;
            }

            dataStorage.resetPlayerStats(target);
            sender.sendMessage("§aSuccessfully reset stats for §f" + target.getName() + ".");
            return true;
        }
    }
}