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

	HashMap<String, Player> requests;

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

			// tp accept
		if (cmd.getName().equalsIgnoreCase("ok")) {
			if (!(sender instanceof Player)){
				System.out.println("Konsola nie moze nikogo do siebie teleportowac");
				return true;
			}
			if (!requests.containsKey(sender.getName())) {
				sender.sendMessage(ChatColor.DARK_RED + "Nikt aktualnie nie proboje sie do Ciebie teleportowac!");
				return true;
			}
			//teleportacja
			Player player = requests.get(sender.getName());
			player.sendMessage("Teleportacja zaakceptowana");
			sender.sendMessage("Teleportacja zaakceptowana");
			Location loc = ((Player) sender).getLocation();
			player.teleport(loc);
			requests.remove(sender.getName());
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("nie")) {
			if (!(sender instanceof Player)){
				System.out.println("Konsola nie moze nikogo do siebie teleportowac");
				return true;
			}
			if (!requests.containsKey(sender.getName())) {
				sender.sendMessage(ChatColor.DARK_RED + "Nikt aktualnie nie proboje sie do Ciebie teleportowac!");
				return true;
			}
			Player player = requests.get(sender.getName());
			player.sendMessage("Teleportacja anulowana");
			sender.sendMessage("Teleportacja anulowana");
			requests.remove(sender.getName());

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
					requests.put(target.getName(), player);
					player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Wysłano zapytanie o teleportacje do: "
							+ target.getName());
					target.sendMessage(ChatColor.YELLOW + player.getName() + "Chce sie do Ciebie teleportowac");
					target.sendMessage(ChatColor.YELLOW + "Aby zaakceptować wpisz " + ChatColor.DARK_GREEN + "/ok");
					target.sendMessage(ChatColor.YELLOW + "Aby odrzucic wpisz " + ChatColor.DARK_GREEN + "/nie");

					if (player.getInventory().getItemInMainHand().getAmount() > 1) {
						player.getInventory().getItemInMainHand().setAmount(
								(player.getInventory().getItemInMainHand().getAmount() - 1));

					} else {
						if (player.getInventory().getItemInMainHand() != null || player.getInventory().getItemInMainHand().
								getType() != Material.AIR)
							player.getInventory().removeItem(player.getInventory().getItemInMainHand());
					}
			}
		}catch(Exception e) {
			System.out.println("Teleportation failed");
			event.getPlayer().sendMessage(ChatColor.RED + "Cos poszlo nie tak. Zglos to do administracji " +
					"serwera/developera");
		}


    }


}
