package me.Siup.TP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin implements Listener {

	HashMap<String, Boolean> players;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		Bukkit.addRecipe(getRecipe());
		System.out.print("[Teleport] Pomyslnie zaladowano plugin teleportacyjny\n");
	}

	@Override
	public void onDisable() {
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("nazwij")) {
			if (!(sender instanceof Player)) {
				return true;
			}
			
			Player player = (Player) sender;
			if(args.length < 1) {
				player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "usage: /nazwij <nickname>");
				return true;
			}
			
			if (!player.getInventory().getItemInMainHand().getItemMeta().getLocalizedName().
					equals("Teleportation Scroll")) {
				
				player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "\nTej komendy mozesz uzyc tylko "
						+ "na zwoju teleportacyjnym");
				return true;
			}
			ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
			meta.setDisplayName(args[0]);
			player.getInventory().getItemInMainHand().setItemMeta(meta);
			
			return true;
		}

		//zmiana zezwolenia na TP
		if (cmd.getName().equalsIgnoreCase("pozwolenie")) {
			if(args[1].equalsIgnoreCase("sprawdz")) {
				if (players.get(sender.getName())){
					sender.sendMessage(ChatColor.GREEN + "Obecnie zezwalasz na teleportacje do siebie");
					return true;

				} else {
					sender.sendMessage(ChatColor.DARK_RED + "Obecnie nie zezwalasz na teleportacje do siebie");
					return true;
				}
			} else if (args[1].equalsIgnoreCase("zmien")) {
				if (args[2].equalsIgnoreCase("TAK")) {
					players.replace(sender.getName(), true);
					sender.sendMessage(ChatColor.GREEN + "Od teraz zezwalasz na teleportacje do siebie");
					return true;

				} else if (args[2].equalsIgnoreCase("NIE")) {
					players.replace(sender.getName(), false);
					sender.sendMessage(ChatColor.DARK_RED + "Od teraz nie zezwalasz na teleportacje do siebie");
					return true;

				} else {
					sender.sendMessage(ChatColor.RED + "Uzycie: /pozwolenie zmien TAK|NIE");
					return true;

				}
			} else {
				sender.sendMessage(ChatColor.RED + "Uzycie: /pozwolenie sprawdz|zmien");
				return true;
			}
		}
		return false;
	}






	public ShapedRecipe getRecipe() { //teleportation scroll
		ItemStack item = new ItemStack(Material.PAPER);
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<String>();
		lore.add("");
		lore.add(ChatColor.GREEN + "Aby teleportowac sie do gracza");
		lore.add(ChatColor.GREEN + "trzymajac zwoj w reku wpisz: ");
		lore.add(ChatColor.YELLOW + "" + ChatColor.ITALIC + "/nazwij <nickname>");
		lore.add(ChatColor.GREEN + "i nacisnij prawy przycisk myszy");
		meta.setLore(lore);
		meta.setLocalizedName("Teleportation Scroll");
		meta.setDisplayName("ZWOJ TELEPORTACYJNY");
		item.setItemMeta(meta);

		NamespacedKey key = new NamespacedKey(this, "ZWOJ_TELEPORTACYJNY");
		ShapedRecipe recipe = new ShapedRecipe(key, item);

		recipe.shape("PPP", "PGP", "PPP");
		recipe.setIngredient('P', Material.GOLD_INGOT);
		recipe.setIngredient('G', Material.PAPER);


		return recipe;
	}


	@EventHandler
    public void onClick(PlayerInteractEvent event) {

		if (!event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.PAPER)) //is paper
            return;

        if (!event.getPlayer().getInventory().getItemInMainHand().hasItemMeta()) {
            return;
        }

        if (!event.getPlayer().getInventory().getItemInMainHand().getItemMeta().
                getLocalizedName().equals("Teleportation Scroll")) { //is tp scroll
            return;
        }

		try {
			if (event.getAction().equals(Action.RIGHT_CLICK_AIR)) { //is right click
				Player target = Bukkit.getPlayerExact(event.getPlayer().getInventory().getItemInMainHand()
						.getItemMeta().getDisplayName());
				Player player = event.getPlayer();

				if (target == null) {
					player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Cel nie istnieje, sprawdz nazwe przedmiotu"
							+ " i upewnij sie, ze zawiera ona nick gracza, ktory jest online"); //player not online
					return;
				}

				if (players.get(target.getName())) {
					//jesli gracz zezwala na teleport
					player.teleport(target.getLocation()); // teleport player to target
					player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Teleportacja do " + target.getName());
					if (player.getInventory().getItemInMainHand().getAmount() > 1) {
						player.getInventory().getItemInMainHand().setAmount(
								(player.getInventory().getItemInMainHand().getAmount() - 1));

					} else {
						if (player.getInventory().getItemInMainHand() != null || player.getInventory().getItemInMainHand().
								getType() != Material.AIR)
							player.getInventory().removeItem(player.getInventory().getItemInMainHand());
					}
				} else {
					player.sendMessage(ChatColor.RED + "gracz obecnie nie zezwala na teleportowanie sie do niego");
				}
			}
		}catch(Exception e) {
			System.out.println("No kurwa jeblo przy tepaniu XD");
			event.getPlayer().sendMessage(ChatColor.RED + "Ziomek no, cos jeblo przy tepaniu XD zglos to Sidowi na discordzie");
		}


    }

    @EventHandler
	public void onLogin(PlayerLoginEvent event) {
		players.put(event.getPlayer().getName(), true);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		players.remove(event.getPlayer().getName());
	}


}
