package com.crazycrafts.craftingascended;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public final class FlightItem extends ElytraItem {
    public static final String CREATIVE_TAG = "craftingascended_creative_flight";
    public static final String POWERED_TAG = "craftingascended_powered_elytra";

    public FlightItem(Properties properties) {
        super(properties.durability(4096).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return super.use(level, player, hand);
        }
        if (!level.isClientSide) {
            boolean creative = player.getTags().contains(CREATIVE_TAG);
            player.removeTag(CREATIVE_TAG);
            player.removeTag(POWERED_TAG);
            if (creative) {
                player.addTag(POWERED_TAG);
                player.getAbilities().mayfly = player.isCreative();
                player.displayClientMessage(Component.literal("Flight Mode: Powered Elytra"), true);
            } else {
                player.addTag(CREATIVE_TAG);
                player.getAbilities().mayfly = true;
                player.displayClientMessage(Component.literal("Flight Mode: Creative"), true);
            }
            player.onUpdateAbilities();
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Creative flight or self-powered Elytra flight").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Right-click: equip • Crouch-right-click: toggle mode").withStyle(ChatFormatting.GOLD));
    }
}
