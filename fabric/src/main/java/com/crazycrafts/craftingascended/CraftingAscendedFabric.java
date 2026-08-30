package com.crazycrafts.craftingascended;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.stats.Stats;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CraftingAscendedFabric implements ModInitializer {
    public static final String MOD_ID = "craftingascended";

    public static final Block ENHANCED_TABLE = table("container.craftingascended.enhanced");
    public static final Block SUPER_TABLE = table("container.craftingascended.super");
    public static final Block ULTIMATE_TABLE = table("container.craftingascended.ultimate");
    public static final Block CELESTIAL_TABLE = table("container.craftingascended.celestial");
    public static final Block FORBIDDEN_TABLE = table("container.craftingascended.forbidden");

    public static final BlockItem ENHANCED_TABLE_ITEM = item(ENHANCED_TABLE, 1);
    public static final BlockItem SUPER_TABLE_ITEM = item(SUPER_TABLE, 2);
    public static final BlockItem ULTIMATE_TABLE_ITEM = item(ULTIMATE_TABLE, 3);
    public static final BlockItem CELESTIAL_TABLE_ITEM = item(CELESTIAL_TABLE, 4);
    public static final BlockItem FORBIDDEN_TABLE_ITEM = item(FORBIDDEN_TABLE, 5);

    private static final Map<String, Item> POWER_ITEMS = new LinkedHashMap<>();

    @Override
    public void onInitialize() {
        register("enhanced_table", ENHANCED_TABLE, ENHANCED_TABLE_ITEM);
        register("super_table", SUPER_TABLE, SUPER_TABLE_ITEM);
        register("ultimate_table", ULTIMATE_TABLE, ULTIMATE_TABLE_ITEM);
        register("celestial_table", CELESTIAL_TABLE, CELESTIAL_TABLE_ITEM);
        register("forbidden_table", FORBIDDEN_TABLE, FORBIDDEN_TABLE_ITEM);

        registerPowerItems();
        ServerTickEvents.END_SERVER_TICK.register(server -> server.getPlayerList().getPlayers().forEach(player -> {
            boolean wearingFlightItem = player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof FlightItem;
            if (wearingFlightItem && player.getTags().contains(FlightItem.CREATIVE_TAG)) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            } else if (!wearingFlightItem && !player.isCreative() && player.getTags().contains(FlightItem.CREATIVE_TAG)) {
                player.getAbilities().flying = false;
                player.getAbilities().mayfly = false;
                player.onUpdateAbilities();
            }
            if (wearingFlightItem && player.getTags().contains(FlightItem.POWERED_TAG) && player.isFallFlying()) {
                player.setDeltaMovement(player.getDeltaMovement().scale(0.85).add(player.getLookAngle().scale(0.12)));
                player.hurtMarked = true;
            }
            if (player.getTags().contains(BridgeBuilderItem.ACTIVE_TAG) && player.level() instanceof ServerLevel serverLevel) {
                BlockPos below = player.blockPosition().below();
                if (serverLevel.getBlockState(below).isAir()) {
                    for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
                        ItemStack candidate = player.getInventory().items.get(slot);
                        if (candidate.getItem() instanceof BlockItem blockItem && blockItem.getBlock() != Blocks.AIR) {
                            serverLevel.setBlockAndUpdate(below, blockItem.getBlock().defaultBlockState());
                            if (!player.isCreative()) candidate.shrink(1);
                            break;
                        }
                    }
                }
            }
        }));

        CreativeModeTab tab = FabricItemGroup.builder()
                .title(Component.translatable("itemGroup.craftingascended"))
                .icon(FORBIDDEN_TABLE_ITEM::getDefaultInstance)
                .displayItems((parameters, output) -> {
                    output.accept(ENHANCED_TABLE_ITEM);
                    output.accept(SUPER_TABLE_ITEM);
                    output.accept(ULTIMATE_TABLE_ITEM);
                    output.accept(CELESTIAL_TABLE_ITEM);
                    output.accept(FORBIDDEN_TABLE_ITEM);
                    POWER_ITEMS.values().forEach(output::accept);
                })
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("main"), tab);
    }

    private static Block table(String titleKey) {
        return new TieredCraftingTableBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE).strength(2.5F), titleKey);
    }

    private static BlockItem item(Block block, int tier) {
        return new TieredTableItem(block, new Item.Properties(), tier);
    }

    private static void register(String name, Block block, BlockItem item) {
        ResourceLocation id = id(name);
        Registry.register(BuiltInRegistries.BLOCK, id, block);
        Registry.register(BuiltInRegistries.ITEM, id, item);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private static void registerPowerItems() {
        power("ore_vacuum", "tooltip.craftingascended.ore_vacuum");
        power("lumber_axe", "tooltip.craftingascended.lumber_axe");
        special("miners_boots", new MinersBootsItem(new Item.Properties()));
        special("quick_furnace", new QuickFurnaceItem(Blocks.BLAST_FURNACE, new Item.Properties()));
        power("vein_pickaxe", "tooltip.craftingascended.vein_pickaxe");
        power("mob_magnet", "tooltip.craftingascended.mob_magnet");
        power("auto_farmer", "tooltip.craftingascended.auto_farmer");
        power("super_shield", "tooltip.craftingascended.super_shield");
        special("titan_blade", new TitanBladeItem(Tiers.NETHERITE,
                new Item.Properties().attributes(SwordItem.createAttributes(Tiers.NETHERITE, 12, -2.2F)).fireResistant()));
        power("chunk_pickaxe", "tooltip.craftingascended.chunk_pickaxe");
        special("infinity_bow", new InfinityBowItem(new Item.Properties()));
        power("ultimate_armor", "tooltip.craftingascended.ultimate_armor");
        power("time_staff", "tooltip.craftingascended.time_staff");
        flight("celestial_wings");
        power("star_cannon", "tooltip.craftingascended.star_cannon");
        power("life_totem", "tooltip.craftingascended.life_totem");
        power("world_breaker", "tooltip.craftingascended.world_breaker");
        power("admin_apple", "tooltip.craftingascended.admin_apple");
        power("duplication_core", "tooltip.craftingascended.duplication_core");
        flight("void_armor");
        special("bridge_builder", new BridgeBuilderItem(new Item.Properties()));
    }

    private static void power(String name, String tooltipKey) {
        Item item = new PowerItem(name, tooltipKey, new Item.Properties().durability(1024).fireResistant());
        POWER_ITEMS.put(name, Registry.register(BuiltInRegistries.ITEM, id(name), item));
    }

    private static void flight(String name) {
        Item item = new FlightItem(new Item.Properties());
        POWER_ITEMS.put(name, Registry.register(BuiltInRegistries.ITEM, id(name), item));
    }

    private static void special(String name, Item item) {
        POWER_ITEMS.put(name, Registry.register(BuiltInRegistries.ITEM, id(name), item));
    }

    private static final class TieredCraftingTableBlock extends CraftingTableBlock {
        private final Component title;

        private TieredCraftingTableBlock(BlockBehaviour.Properties properties, String titleKey) {
            super(properties);
            this.title = Component.translatable(titleKey);
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                   Player player, BlockHitResult hitResult) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }

            player.openMenu(createMenuProvider(level, pos));
            player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
            return InteractionResult.CONSUME;
        }

        @Override
        protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
            return createMenuProvider(level, pos);
        }

        private MenuProvider createMenuProvider(Level level, BlockPos pos) {
            ContainerLevelAccess access = ContainerLevelAccess.create(level, pos);
            return new SimpleMenuProvider(
                    (containerId, inventory, player) ->
                            new TieredCraftingMenu(containerId, inventory, access, this),
                    title);
        }
    }

    private static final class TieredCraftingMenu extends CraftingMenu {
        private final ContainerLevelAccess tableAccess;
        private final Block tableBlock;

        private TieredCraftingMenu(int containerId, Inventory inventory,
                                  ContainerLevelAccess access, Block tableBlock) {
            super(containerId, inventory, access);
            this.tableAccess = access;
            this.tableBlock = tableBlock;
        }

        @Override
        public boolean stillValid(Player player) {
            return stillValid(tableAccess, player, tableBlock);
        }
    }

    private static final class TieredTableItem extends BlockItem {
        private final int tier;

        private TieredTableItem(Block block, Properties properties, int tier) {
            super(block, properties);
            this.tier = tier;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context,
                                    List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("tooltip.craftingascended.tier", tier)
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("tooltip.craftingascended.unlocks." + tier)
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
