package com.crazycrafts.craftingascended;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class BridgeBuilderItem extends Item {
    public static final String ACTIVE_TAG = "craftingascended_bridge_builder";

    public BridgeBuilderItem(Properties properties) {
        super(properties.stacksTo(1).durability(2048));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            boolean active = player.getTags().contains(ACTIVE_TAG);
            if (active) player.removeTag(ACTIVE_TAG); else player.addTag(ACTIVE_TAG);
            player.displayClientMessage(Component.literal("Bridge Builder: " + (active ? "OFF" : "ON")), true);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
