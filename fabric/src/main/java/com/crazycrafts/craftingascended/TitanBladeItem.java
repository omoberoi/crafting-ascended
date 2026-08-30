package com.crazycrafts.craftingascended;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.ChatFormatting;
import java.util.List;

public final class TitanBladeItem extends SwordItem {
    public TitanBladeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (target.isDeadOrDying()) {
            attacker.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 20, 1));
            attacker.heal(4.0F);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("16 attack damage • Kills heal and grant Strength").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
