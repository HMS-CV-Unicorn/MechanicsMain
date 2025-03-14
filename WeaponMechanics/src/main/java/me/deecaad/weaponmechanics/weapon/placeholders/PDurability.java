package me.deecaad.weaponmechanics.weapon.placeholders;

import me.deecaad.core.placeholder.NumericPlaceholderHandler;
import me.deecaad.core.placeholder.PlaceholderData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PDurability extends NumericPlaceholderHandler {

    public PDurability() {
        super("durability");
    }

    @Override
    public @Nullable Number requestValue(@NotNull PlaceholderData data) {
        ItemStack item = data.item();
        if (item == null)
            return null;

        if (item.getItemMeta() instanceof Damageable damageable && damageable.hasMaxDamage()) {
            int maxDurability = damageable.getMaxDamage();
            int durability = maxDurability - damageable.getDamage();
            return durability;
        }

        return null;
    }
}
