package com.cahrypt.me.punishmentsx.punishments.display;

import com.cahrypt.me.punishmentsx.PunishmentsX;
import com.cahrypt.me.punishmentsx.player.PlayerManager;
import com.cahrypt.me.punishmentsx.player.StorablePlayerInfo;
import com.cahrypt.me.punishmentsx.punishments.PunishmentInfo;
import com.cahrypt.me.punishmentsx.punishments.PunishmentManager;
import com.cahrypt.me.punishmentsx.util.Utils;
import dev.fumaz.commons.bukkit.gui.Gui;
import dev.fumaz.commons.bukkit.gui.item.ClickableGuiItem;
import dev.fumaz.commons.bukkit.gui.item.DisplayGuiItem;
import dev.fumaz.commons.bukkit.gui.item.GuiItem;
import dev.fumaz.commons.bukkit.gui.item.GuiItemBuilder;
import dev.fumaz.commons.bukkit.gui.page.ListPage;
import dev.fumaz.commons.bukkit.gui.page.Page;
import dev.fumaz.commons.bukkit.item.ColorableMaterials;
import dev.fumaz.commons.bukkit.item.ItemBuilder;
import dev.fumaz.commons.bukkit.item.MaterialColors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PunishmentInfoDisplay {
    private static final int PUNISHMENT_GUI_ROWS = 5;
    private static final int INFO_GUI_ROWS = 3;
    private static final GuiItem PUNISHMENT_GUI_FILLER = new DisplayGuiItem(ColorableMaterials.STAINED_GLASS_PANE.withColor(MaterialColors.RED));
    private static final GuiItem INFO_GUI_FILLER = new DisplayGuiItem(ColorableMaterials.STAINED_GLASS_PANE.withColor(MaterialColors.RED));

    private final PunishmentsX main;
    private final PlayerManager playerManager;

    private final OfflinePlayer target;

    /**
     * Creates a new system of GUIs to display the specified target's punishment information
     * @param target the target
     */
    public PunishmentInfoDisplay(OfflinePlayer target) {
        this.main = JavaPlugin.getPlugin(PunishmentsX.class);
        this.playerManager = main.getPlayerManager();

        this.target = target;
    }

    /**
     * Builds the main info GUI for the given {@link StorablePlayerInfo}
     * @return the main info GUI
     */
    private Gui buildInfoGui(StorablePlayerInfo targetInfo) {
        Gui gui = new Gui(main, targetInfo.getName() + "'s Punishment Info", INFO_GUI_ROWS);
        Page page = new Page(gui);

        page.setItem(4, new DisplayGuiItem(ItemBuilder.of(Material.PLAYER_HEAD)
                .consumeCustomMeta(SkullMeta.class, skullMeta -> skullMeta.setOwningPlayer(targetInfo.getOfflinePlayer()))
                .displayName(net.md_5.bungee.api.ChatColor.GREEN + targetInfo.getName() + "'s Logs")
                .build())
        );

        page.setItem(getInfoGuiSlot(2), getClickableDisplayItem(SpecificDisplay.MUTE_DISPLAY, targetInfo));
        page.setItem(getInfoGuiSlot(3), getClickableDisplayItem(SpecificDisplay.KICK_DISPLAY, targetInfo));
        page.setItem(getInfoGuiSlot(5), getClickableDisplayItem(SpecificDisplay.BAN_DISPLAY, targetInfo));
        page.setItem(getInfoGuiSlot(6), getClickableDisplayItem(SpecificDisplay.BLACKLIST_DISPLAY, targetInfo));

        page.fillEmpty(INFO_GUI_FILLER);
        gui.setPage(page);

        return gui;
    }

    /**
     * Create a clickable item from the specified {@link StorablePlayerInfo} to open the specified {@link SpecificDisplay} GUI
     * @param specificDisplay the desired {@link SpecificDisplay}
     * @param targetInfo the target's SQL-storable information
     * @return the clickable item
     */
    private ClickableGuiItem getClickableDisplayItem(SpecificDisplay specificDisplay, StorablePlayerInfo targetInfo) {
        return new ClickableGuiItem(specificDisplay.getDisplayItem(), event -> specificDisplay.display((Player) event.getWhoClicked(), targetInfo, this));
    }

    /**
     * Obtains an appropriate middle-row slot
     * @param column the column of the slot
     * @return the slot number
     */
    private int getInfoGuiSlot(int column) {
        return (int) (Math.floor(INFO_GUI_ROWS / 2D) * 9) + column;
    }

    /**
     * Displays the main punishment information GUI of the target to the specified player
     * @param player the player to display to
     */
    public void displayPunishmentInfo(Player player) {
        playerManager.usePlayerInfoOrElseASync(target.getName(), info -> buildInfoGui(info).show(player), str -> {});
    }

    public enum SpecificDisplay {

        MUTE_DISPLAY(
                "Mute Logs",
                ItemBuilder.of(Material.PAPER).displayName(ChatColor.GREEN + "Mute Logs").build(),
                PunishmentManager.PunishmentStorage.MUTE_STORAGE
        ),

        BAN_DISPLAY(
                "Ban Logs",
                ItemBuilder.of(Material.DIAMOND_AXE).displayName(ChatColor.GREEN + "Ban Logs").build(),
                PunishmentManager.PunishmentStorage.BAN_STORAGE
        ),

        KICK_DISPLAY(
                "Kick Logs",
                ItemBuilder.of(Material.LEATHER_BOOTS).displayName(ChatColor.GREEN + "Kick Logs").build(),
                PunishmentManager.PunishmentStorage.KICK_STORAGE
        ),

        BLACKLIST_DISPLAY(
                "Blacklist (IP-Ban) Logs",
                ItemBuilder.of(Material.OBSIDIAN).displayName(ChatColor.GREEN + "Blacklist Logs").build(),
                PunishmentManager.PunishmentStorage.BLACKLIST_STORAGE
        );

        private final String displayName;
        private final ItemStack displayItem;
        private final PunishmentManager.PunishmentStorage storage;

        /**
         * Create a new GUI punishment display system for a punishment storage
         * @param displayName the GUI name
         * @param displayItem the GUI display item
         * @param storage the storage
         */
        SpecificDisplay(@NotNull String displayName, @NotNull ItemStack displayItem, @NotNull PunishmentManager.PunishmentStorage storage) {
            this.displayName = displayName;
            this.displayItem = displayItem;
            this.storage = storage;
        }

        /**
         * Builds a {@link GuiItem} that displays information in the provided {@link PunishmentInfo}
         * @param punishmentInfo the {@link PunishmentInfo}
         * @return the {@link GuiItem}
         */
        private GuiItem buildPunishmentDisplayItem(@NotNull PunishmentInfo punishmentInfo) {
            String storableSender = punishmentInfo.getStorableSender();
            String displaySender;

            try {
                displaySender = (Bukkit.getOfflinePlayer(UUID.fromString(storableSender)).getName());
            } catch (IllegalArgumentException exception) {
                displaySender = (storableSender);
            }

            boolean pardoned = punishmentInfo.isPardoned();
            ChatColor color = (pardoned || (punishmentInfo.getExpiry().getTime() < Utils.getCurrentTimeMillis()) ? ChatColor.GREEN : ChatColor.RED);

            return new GuiItemBuilder().item(ItemBuilder.of(Material.BOOK)
                    .displayName(color + "Reason: " + ChatColor.GRAY + punishmentInfo.getReason())
                    .addToLore(
                            color + "Punisher: " + ChatColor.GRAY + displaySender,
                            color + "Date: " + ChatColor.GRAY + Utils.formatTimestamp(punishmentInfo.getPunishDate()),
                            color + "Expiry: " + ChatColor.GRAY + Utils.formatTimestamp(punishmentInfo.getExpiry()),
                            color + "Pardoned: " + ChatColor.GRAY + pardoned,
                            (pardoned ? color + "Pardon Reason: " + ChatColor.GRAY + punishmentInfo.getPardonReason() : "")
                    ).build()
            ).build();
        }

        /**
         * Builds an item for reopening the specified {@link PunishmentInfoDisplay} main info page
         * @param player the player the {@link PunishmentInfoDisplay} will be displayed to
         * @param homepage the {@link PunishmentInfoDisplay} that should be opened upon click
         * @return the {@link GuiItem}
         */
        private GuiItem buildBackItem(@NotNull Player player, @Nullable PunishmentInfoDisplay homepage) {
            if (homepage == null) {
                return null;
            }

            return new GuiItemBuilder()
                    .item(ItemBuilder.of(Material.ARROW)
                            .displayName(ChatColor.AQUA + "Back To Main Page")
                            .build())
                    .onClick(event -> homepage.displayPunishmentInfo(player))
                    .build();
        }

        /**
         * @return the display name of the GUI
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * @return the display item
         */
        public ItemStack getDisplayItem() {
            return displayItem;
        }

        /**
         * Display the specific punishment information of the provided {@link StorablePlayerInfo} to the provided player
         * @param player the punishment information viewer
         * @param targetInfo the target information
         */
        public void display(Player player, StorablePlayerInfo targetInfo) {
            display(player, targetInfo, null);
        }

        /**
         * Display the specific punishment information of the provided {@link StorablePlayerInfo} to the provided player
         * (Allows for a {@link PunishmentInfoDisplay} main information page)
         * @param player the punishment information viewer
         * @param targetInfo the target information
         */
        public void display(@NotNull Player player, @NotNull StorablePlayerInfo targetInfo, @Nullable PunishmentInfoDisplay homepage) {
            Gui gui = new Gui(JavaPlugin.getPlugin(PunishmentsX.class), player.getName() + "'s " + displayName, PUNISHMENT_GUI_ROWS);
            ListPage listPage = new ListPage(gui, PUNISHMENT_GUI_FILLER, buildBackItem(player, homepage));

            storage.consumePunishmentInfoASync(targetInfo, punishmentInfoList -> {
                punishmentInfoList.forEach(punishmentInfo -> listPage.addItem(buildPunishmentDisplayItem(punishmentInfo)));

                gui.setPage(listPage);
                gui.show(player);
            });
        }
    }
}
