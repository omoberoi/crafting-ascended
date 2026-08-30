package com.crazycrafts.craftingascended;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.ChatFormatting;
import java.util.List;

public final class InfinityBowItem extends BowItem {
    public InfinityBowItem(Properties properties) {
        super(properties.durability(4096).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void releaseUsing(ItemStack bow, Level level, LivingEntity user, int timeLeft) {
        if (!(user instanceof Player player)) return;
        int charged = getUseDuration(bow, user) - timeLeft;
        float power = getPowerForTime(charged);
        if (power < 0.1F) return;
        if (!level.isClientSide) {
            Arrow arrow = new Arrow(level, player, new ItemStack(Items.ARROW), bow);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.6F, 0.25F);
            arrow.setCritArrow(power == 1.0F);
            arrow.setBaseDamage(5.0D);
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            level.addFreshEntity(arrow);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Fires powerful arrows without consuming ammunition").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
