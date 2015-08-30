package me.stray.listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

/**
 * Created by Stray on 8/29/2015.
 */
public class ConvictedListener extends JavaPlugin implements Listener, CommandExecutor {
    public static Economy economy = null;

    public static File vFile;
    public static FileConfiguration votes;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("vote").setExecutor(this);
        setupEconomy();
        vFile = new File(getDataFolder(), "votes.yml");

        try {
            firstRun();
        } catch (Exception e) {

        }


        votes = new YamlConfiguration();
        loadYamls();
    }

    @Override
    public void onDisable() {
        saveYamls();
    }

    @EventHandler
    public void onPlayerVote(VotifierEvent event) {
        Vote v = event.getVote();
        saveVote(Bukkit.getServer().getOfflinePlayer(v.getUsername()));
        economy.depositPlayer(this.getServer().getOfflinePlayer(v.getUsername()), 1000);
        Bukkit.broadcastMessage(ChatColor.GREEN + v.getUsername() + ChatColor.DARK_GREEN + " got $1000 by using " + ChatColor.RED + "/vote.");
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        if(event.getClickedBlock() != null) {
            if(!event.getPlayer().isOp()) {
                if(event.getClickedBlock().getType() == Material.ENCHANTMENT_TABLE) {
                    if(!hasPlayerVoted(event.getPlayer())) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(ChatColor.RED + "You must vote for the server before you can enchant.");
                    }
                }
            }
        }
    }

    public boolean hasPlayerVoted(Player p) {
        if(votes.get("votes." + p.getUniqueId().toString()) != null) {
            if(votes.getLong("votes." + p.getUniqueId().toString() + ".expires") >= System.currentTimeMillis()) {
                return true;
            }
            else {
                votes.set("votes." + p.getUniqueId().toString(), null);
                saveYamls();
                return false;
            }
        }
        else {
            return false;
        }
    }

    public void saveVote(OfflinePlayer p) {
        long end = System.currentTimeMillis() + 86400000;
        votes.set("votes." + p.getUniqueId().toString() + ".expires", end);
        saveYamls();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmnd, String[] args) {
        if(cmd.getName().equalsIgnoreCase("vote")) {
            //sender.sendMessage(ChatColor.RED + "Coming soon.");
            sender.sendMessage(ChatColor.YELLOW + "########## Voting information ##########");
            sender.sendMessage(ChatColor.YELLOW + "You can vote once a day at these links.");
            sender.sendMessage(ChatColor.YELLOW + "You must vote before you can enchant. You");
            sender.sendMessage(ChatColor.YELLOW + "receive $1000 per vote.");
            sender.sendMessage(ChatColor.YELLOW + "Voting Link: " + ChatColor.GREEN + "http://bit.ly/ConvictedVoting");
            sender.sendMessage(ChatColor.YELLOW + "####################################");
        }
        return true;
    }

    private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }

    private void firstRun() throws Exception {
        if(!vFile.exists()){
            vFile.getParentFile().mkdirs();
            copy(getResource("votes.yml"), vFile);
        }

    }

    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {

        }
    }
    public void loadYamls() {
        try {
            votes.load(vFile);

        } catch (Exception e) {

        }
    }

    public void saveYamls() {
        try {
            votes.save(vFile);
        } catch (IOException e) {

        }
    }
}
