package be.maximvdw.spigot3684;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Spigot-3684
 * Test cases
 * Created by Maxim on 7/12/2017.
 */
public class Spigot3684 extends JavaPlugin implements Listener {
    /**
     * Reflection
     */
    private static Class<?> nmsScoreboard = null;
    private static Class<?> nmsScoreboardObjective = null;
    private static Class<?> nmsScoreboardTeam = null;
    private static Method nmsScoreboardSetDisplaySlot = null;
    private static Class<?> obcScoreboard = null;
    private static Class<?> obcTeam = null;
    private static Class<?> obcObjective = null;
    private static Method obcScoreboardGetHandle = null;
    private static Method nmsScoreboardHandleTeamChanged = null;

    @Override
    public void onEnable() {
        // Register join listener
        Bukkit.getPluginManager().registerEvents(this, this);

        // Load reflection classes/methods
        try {
            // TODO: Get handle methods better
            nmsScoreboard = ReflectionUtil.getNMSClassWithException("Scoreboard");
            nmsScoreboardTeam = ReflectionUtil.getNMSClassWithException("ScoreboardTeam");
            nmsScoreboardObjective = ReflectionUtil.getNMSClassWithException("ScoreboardObjective");
            nmsScoreboardSetDisplaySlot = nmsScoreboard.getMethod("setDisplaySlot", int.class, nmsScoreboardObjective);
            nmsScoreboardSetDisplaySlot.setAccessible(true);
            nmsScoreboardHandleTeamChanged = nmsScoreboard.getMethod("handleTeamChanged", nmsScoreboardTeam);
            obcScoreboard = ReflectionUtil.getOBCClass("scoreboard.CraftScoreboard");
            obcTeam = ReflectionUtil.getOBCClass("scoreboard.CraftTeam");
            obcScoreboardGetHandle = obcScoreboard.getMethod("getHandle");
            obcScoreboardGetHandle.setAccessible(true);
            obcObjective = ReflectionUtil.getOBCClass("scoreboard.CraftObjective");
        } catch (Exception ex) {

        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("Usage: /testcase <case1|case2> <color>");
            return false;
        }

        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;

        // Get the color
        String colorStr = args[1];
        ChatColor color = null;
        try {
            color = ChatColor.valueOf(colorStr.toUpperCase());
        } catch (Exception ex) {
            sender.sendMessage("Invalid color!");
            return false;
        }

        // Get the team created on join
        Team team = player.getScoreboard().getTeam("TEST");
        if (team == null) {
            sender.sendMessage("Some plugin removed the test team created on join!");
            return false;
        }

        // Check the test case
        if (args[0].equalsIgnoreCase("case1")) {
            sender.sendMessage("[CASE1] Setting ONLY the team color");
            // Before the alternations this would not update the outline
            team.setColor(color);
        } else if (args[0].equalsIgnoreCase("case2")) {
            sender.sendMessage("[CASE2] Setting the team color and forcing update");
            // Proposed behavior - forcing an update
            team.setColor(color);
            // Quick and dirty load
            try {
                //TODO: Use getHandle instead + caching
                Field field = ReflectionUtil.getField("team", obcTeam);
                field.setAccessible(true);
                Object teamHandle = field.get(team);
                nmsScoreboardHandleTeamChanged.invoke(obcScoreboardGetHandle.invoke(player.getScoreboard()), teamHandle);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        } else {
            sender.sendMessage("Invalid case!");
            return false;
        }
        return true;
    }

    /**
     * Clear any previous scoreboard
     * Set the scoreboard to main for visual purposes
     *
     * @param event join event
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        player.setScoreboard(scoreboard);
        // Create/Load team
        Team team = scoreboard.getTeam("TEST");
        if (team == null) {
            team = scoreboard.registerNewTeam("TEST");
        }
        // Add the player
        team.addEntry(player.getName());

        // Create the objectives if not exist
        for (ChatColor color : ChatColor.values()) {
            if (color.ordinal() > 16){
                break;
            }
            try {
                Objective objective = scoreboard.getObjective("TEST" + color.ordinal());
                if (objective != null) {
                    continue;
                }
                objective = scoreboard.registerNewObjective("TEST" + color.ordinal(),
                        "dummy");
                objective.setDisplayName(color + color.name());
                objective.getScore(color + "TEST").setScore(1);
                Object nmsScoreboardObj = obcScoreboardGetHandle.invoke(scoreboard);
                Field field = ReflectionUtil.getField("objective", obcObjective);
                field.setAccessible(true);
                Object nmsScoreboardObjectiveObj = field.get(objective);
                nmsScoreboardSetDisplaySlot.invoke(nmsScoreboardObj, 3 + color.ordinal(), nmsScoreboardObjectiveObj);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

}
