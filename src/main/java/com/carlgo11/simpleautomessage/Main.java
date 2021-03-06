package com.carlgo11.simpleautomessage;

import com.carlgo11.simpleautomessage.commands.*;
import com.carlgo11.simpleautomessage.updater.*;
import com.carlgo11.simpleautomessage.language.*;
import com.carlgo11.simpleautomessage.player.*;
import java.io.File;
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import com.carlgo11.simpleautomessage.metrics.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import static org.bukkit.Bukkit.getPluginManager;
import org.bukkit.plugin.PluginManager;

public class Main extends JavaPlugin {

    public int tick = 1; // msg+<tick> int
    public int time = 0; // the delay
    public static YamlConfiguration LANG;
    public static File LANG_FILE;
    public boolean update = false;
    public boolean debugm;
    public ArrayList<String> messages = new ArrayList<String>();
    public String configv = "1.0.7";

    public void onEnable()
    {

        reloadConfig();
        checkConfig();
        checkDebugMode();
        checkVersion();
        getServer().getPluginManager().registerEvents(new loadLang(this), this);
        registerListeners(getPluginManager());
        registerCommands();
        getLogger().info(getDescription().getName() + " " + getDescription().getVersion() + " " + Lang.ENABLED);
    }

    public void onDisable()
    {
        getLogger().info(getDescription().getName() + " " + getDescription().getVersion() + " " + Lang.DISABLED);
    }

    private void registerListeners(PluginManager pm)
    {
        moveMessages.moveOldMessages(this);
        loadMessages();
        pm.registerEvents(new Time(this), this);
        pm.registerEvents(new PlayerJoin(this), this);

        Announce announce = new Announce();
        announce.setup(this);

        try {
            Metrics metrics = new Metrics(this);
            CustomGraphs.graphs(metrics, this);
            metrics.start();
        } catch (IOException ex) {
            System.out.println("[" + getDescription().getName() + "] " + Lang.STATS_ERROR + "Output: " + ex.toString());
        }
    }

    private void registerCommands()
    {
        getCommand("simpleautomessage").setExecutor(new SimpleautomessageCommand(this));
    }

    public void checkVersion()
    {
        if (getDescription().getVersion().startsWith("dev-")) { // prints out a warning when using dev build
            getLogger().warning("You are using a development build. Keep in mind development builds may contain bugs!");
            getLogger().warning("If you want a fully working version please use a recommended build.");
            getLogger().warning("Type /simpleautomessage update to download the latest recommended build.");
        }

        if (getConfig().getBoolean("auto-update")) {
            debug("Calling Updater.java");
            Updater updater = new Updater(this, 49417, getFile(), Updater.UpdateType.DEFAULT, true);
            update = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;

        } else if (!getConfig().getString("warn-update").equalsIgnoreCase("none")) {
            Updater updater = new Updater(this, 49417, getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
            update = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;
        } else {
            debug("auto-update & warn-update is set to false!");
        }
    }

    public void checkConfig()
    {
        File config = new File(getDataFolder(), "config.yml");
        if (!config.exists()) {
            saveDefaultConfig();
            System.out.println("[" + getDescription().getName() + "] " + "No config.yml found, config.yml created.");
        }
        if (getConfig().getBoolean("update-config")) {
            if (!getConfig().getString("version").equals(this.configv)) {
                config.renameTo(new File(getDataFolder(), "config.version-" + getConfig().getString("version") + ".yml"));
                saveDefaultConfig();
            }
        } else {
            debug("update-config is set to false.");
        }
    }

    private void loadMessages()
    {
        messages.clear();
        messages.add("This message should not be displayed. Contact the developers");
        try {
            File file = this.getDataFolder();
            if (!file.exists()) {
                file.createNewFile();
                this.getLogger().info("No messages.txt found. Created a new one");
            }
            BufferedReader read;
            read = new BufferedReader(new FileReader(file));

            String line;
            while ((line = read.readLine()) != null) {
                if (!messages.contains(line)) {
                    if (!line.startsWith("#")) {
                        messages.add(line);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public YamlConfiguration getLang()
    {
        return LANG;
    }

    public File getLangFile()
    {
        return LANG_FILE;
    }

    public void debug(String s)
    {
        if (debugm) {
            getLogger().log(Level.INFO, "[" + "Debug" + "]" + " {0}", s);
        }
    }

    public void checkDebugMode()
    {
        if (getConfig().getBoolean("debug")) {
            debugm = true;
        }
    }

    public void forceUpdate(CommandSender p, String sender0)
    {
        String up = Lang.UPDATING.toString().replaceAll("%prefix%", getDescription().getName());
        String updone = Lang.UPDATED.toString().replaceAll("%prefix%", getDescription().getName());
        p.sendMessage(sender0 + " " + ChatColor.GREEN + up);
        Updater updater = new Updater(this, 49417, getFile(), Updater.UpdateType.NO_VERSION_CHECK, true);
        p.sendMessage(sender0 + " " + ChatColor.GREEN + updone);
    }

    public boolean onlinePlayers()
    {
        int conf = getConfig().getInt("min-players");
        int online = Bukkit.getOnlinePlayers().length;

        if (online >= conf) {
            return true;
        } else {
            return false;
        }
    }

    public int getRandomInt(int maxammount)
    {
        int qwe = 0;
        int a = 0;
        if (maxammount < 3 || qwe == 0) {
            Random n = new Random();
            int num = 0;
            for (int count = 1; count <= 2; count++) {
                num = 1 + n.nextInt(maxammount);
            }
            a = num;
        } else {
            Random n = new Random();
            int num = 0;
            for (int count = 1; count <= 2; count++) {
                num = 1 + n.nextInt(maxammount);
            }
            a = num;
            if (qwe == a) {
                this.getRandomInt(maxammount);
            }
        }
        return a;
    }

    public File getMessageFile()
    {
        return new File("" + this.getDataFolder() + File.separatorChar + this.getConfig().getString("message-file"));
    }

}
