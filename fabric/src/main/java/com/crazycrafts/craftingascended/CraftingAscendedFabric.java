package com.crazycrafts.craftingascended;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.stats.Stats;

import java.util.List;

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

    @Override
    public void onInitialize() {
        register("enhanced_table", ENHANCED_TABLE, ENHANCED_TABLE_ITEM);
        register("super_table", SUPER_TABLE, SUPER_TABLE_ITEM);
        register("ultimate_table", ULTIMATE_TABLE, ULTIMATE_TABLE_ITEM);
        register("celestial_table", CELESTIAL_TABLE, CELESTIAL_TABLE_ITEM);
        register("forbidden_table", FORBIDDEN_TABLE, FORBIDDEN_TABLE_ITEM);

        CreativeModeTab tab = FabricItemGroup.builder()
                .title(Component.translatable("itemGroup.craftingascended"))
                .icon(FORBIDDEN_TABLE_ITEM::getDefaultInstance)
                .displayItems((parameters, output) -> {
                    output.accept(ENHANCED_TABLE_ITEM);
                    output.accept(SUPER_TABLE_ITEM);
                    output.accept(ULTIMATE_TABLE_ITEM);
                    output.accept(CELESTIAL_TABLE_ITEM);
                    output.accept(FORBIDDEN_TABLE_ITEM);
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
