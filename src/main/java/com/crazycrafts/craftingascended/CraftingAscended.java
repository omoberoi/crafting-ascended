package com.crazycrafts.craftingascended;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(CraftingAscended.MOD_ID)
public final class CraftingAscended {
    public static final String MOD_ID = "craftingascended";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredBlock<Block> ENHANCED_TABLE = registerTable("enhanced_table");
    public static final DeferredBlock<Block> SUPER_TABLE = registerTable("super_table");
    public static final DeferredBlock<Block> ULTIMATE_TABLE = registerTable("ultimate_table");
    public static final DeferredBlock<Block> CELESTIAL_TABLE = registerTable("celestial_table");
    public static final DeferredBlock<Block> FORBIDDEN_TABLE = registerTable("forbidden_table");

    public static final DeferredItem<BlockItem> ENHANCED_TABLE_ITEM = registerTableItem("enhanced_table", ENHANCED_TABLE);
    public static final DeferredItem<BlockItem> SUPER_TABLE_ITEM = registerTableItem("super_table", SUPER_TABLE);
    public static final DeferredItem<BlockItem> ULTIMATE_TABLE_ITEM = registerTableItem("ultimate_table", ULTIMATE_TABLE);
    public static final DeferredItem<BlockItem> CELESTIAL_TABLE_ITEM = registerTableItem("celestial_table", CELESTIAL_TABLE);
    public static final DeferredItem<BlockItem> FORBIDDEN_TABLE_ITEM = registerTableItem("forbidden_table", FORBIDDEN_TABLE);

    private static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.craftingascended"))
                    .withTabsBefore(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                    .icon(() -> FORBIDDEN_TABLE_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ENHANCED_TABLE_ITEM.get());
                        output.accept(SUPER_TABLE_ITEM.get());
                        output.accept(ULTIMATE_TABLE_ITEM.get());
                        output.accept(CELESTIAL_TABLE_ITEM.get());
                        output.accept(FORBIDDEN_TABLE_ITEM.get());
                    })
                    .build());

    public CraftingAscended(IEventBus modBus, ModContainer container) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        TABS.register(modBus);
    }

    private static DeferredBlock<Block> registerTable(String name) {
        return BLOCKS.register(name, () -> new CraftingTableBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)
                        .strength(2.5F)));
    }

    private static DeferredItem<BlockItem> registerTableItem(String name, DeferredBlock<Block> block) {
        return ITEMS.registerSimpleBlockItem(name, block);
    }
}
