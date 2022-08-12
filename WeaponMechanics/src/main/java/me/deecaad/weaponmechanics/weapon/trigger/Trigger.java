package me.deecaad.weaponmechanics.weapon.trigger;

import me.deecaad.core.file.SerializeData;
import me.deecaad.core.file.Serializer;
import me.deecaad.core.file.SerializerException;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.MainHand;

import javax.annotation.Nonnull;

import static me.deecaad.weaponmechanics.WeaponMechanics.getBasicConfigurations;

public class Trigger implements Serializer<Trigger> {

    private TriggerType mainhand;
    private TriggerType offhand;
    private Circumstance circumstance;
    private TriggerType dualWieldMainHand;
    private TriggerType dualWieldOffHand;

    /**
     * Default constructor for serializer
     */
    public Trigger() {
    }

    public Trigger(TriggerType mainhand, TriggerType offhand, Circumstance circumstance, TriggerType dualWieldMainHand, TriggerType dualWieldOffHand) {
        this.mainhand = mainhand;
        this.offhand = offhand;
        this.circumstance = circumstance;
        this.dualWieldMainHand = dualWieldMainHand;
        this.dualWieldOffHand = dualWieldOffHand;
    }

    /**
     * Checks if trigger is valid
     *
     * @param triggerType the trigger type
     * @param slot the slot used
     * @param entityWrapper the entity's wrapper from whom to check
     * @return true if trigger is valid
     */
    public boolean check(TriggerType triggerType, EquipmentSlot slot, EntityWrapper entityWrapper) {
        TriggerType typeCheck;
        boolean isDual = entityWrapper.isDualWielding();
        LivingEntity livingEntity = entityWrapper.getEntity();

        if (slot == EquipmentSlot.HAND) {
            if (isDual && dualWieldMainHand != null) {
                typeCheck = dualWieldMainHand;
                if (typeCheck.isRightOrLeft() && livingEntity.getType() == EntityType.PLAYER &&((Player) livingEntity).getMainHand() == MainHand.LEFT) {
                    // Invert if player has inverted main hand...
                    typeCheck = typeCheck == TriggerType.RIGHT_CLICK ? TriggerType.LEFT_CLICK : TriggerType.RIGHT_CLICK;
                }
            } else {
                typeCheck = mainhand;
            }
        } else {
            if (isDual && dualWieldOffHand != null) {
                typeCheck = dualWieldOffHand;
                if (typeCheck.isRightOrLeft() && livingEntity.getType() == EntityType.PLAYER &&((Player) livingEntity).getMainHand() == MainHand.LEFT) {
                    // Invert if player has inverted main hand...
                    typeCheck = typeCheck == TriggerType.RIGHT_CLICK ? TriggerType.LEFT_CLICK : TriggerType.RIGHT_CLICK;
                }
            } else {
                typeCheck = offhand;
            }
        }

        if (typeCheck == null || typeCheck != triggerType) return false;

        return circumstance == null || !circumstance.deny(entityWrapper);
    }

    /**
     * @return the trigger for main hand
     */
    public TriggerType getMainhand() {
        return this.mainhand;
    }

    /**
     * @return the trigger for off hand
     */
    public TriggerType getOffhand() {
        return this.offhand;
    }

    @Override
    public String getKeyword() {
        return "Trigger";
    }

    @Override
    @Nonnull
    public Trigger serialize(SerializeData data) throws SerializerException {
        TriggerType main = data.of("Main_Hand").getEnum(TriggerType.class, null);
        TriggerType off = data.of("Off_Hand").getEnum(TriggerType.class, null);

        if (main == null && off == null) {
            throw data.exception(null, "At least one of Main_Hand or Off_Hand should be used");
        }

        TriggerType dualMain = data.of("Dual_Wield.Main_Hand").getEnum(TriggerType.class, null);
        TriggerType dualOff = data.of("Dual_Wield.Off_Hand").getEnum(TriggerType.class, null);

        if (isDisabled(main)) throw data.exception("Main_Hand", "Tried to use trigger which is disabled in config.yml");
        if (isDisabled(off)) throw data.exception("Off_Hand", "Tried to use trigger which is disabled in config.yml");
        if (isDisabled(dualMain)) throw data.exception("Dual_Wield.Main_Hand", "Tried to use trigger which is disabled in config.yml");
        if (isDisabled(dualOff)) throw data.exception("Dual_Wield.Off_Hand", "Tried to use trigger which is disabled in config.yml");

        Circumstance circumstance = data.of("Circumstance").serialize(Circumstance.class);

        // Check to make sure the gun denies swapping hands, otherwise this
        // won't work.
        if (dualMain == TriggerType.SWAP_HANDS || dualOff == TriggerType.SWAP_HANDS) {
            String weaponTitle = data.key.split("\\.")[0];

            if (!data.config.getBoolean(weaponTitle + ".Info.Cancel.Swap_Hands", false)) {
                throw data.exception(null, "When using 'SWAP_HANDS', make sure that '" + weaponTitle + ".Info.Cancel.Swap_Hands: true'",
                        SerializerException.forValue(dualMain) + " & " + SerializerException.forValue(dualOff));
            }
        }

        return new Trigger(main, off, circumstance, dualMain, dualOff);
    }

    private boolean isDisabled(TriggerType trigger) {
        if (trigger == null) return false;
        switch (trigger) {
            case START_SNEAK:
            case END_SNEAK:
            case DOUBLE_SNEAK:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.Sneak");
            case START_SPRINT:
            case END_SPRINT:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.Sprint");
            case RIGHT_CLICK:
            case LEFT_CLICK:
            case MELEE:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.Right_And_Left_Click");
            case DROP_ITEM:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.Drop_Item");
            case JUMP:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.Jump");
            case DOUBLE_JUMP:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.Double_Jump");
            case START_SWIM:
            case END_SWIM:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.Swim");
            case START_GLIDE:
            case END_GLIDE:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.Glide");
            case SWAP_HANDS:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.Swap_Hand_Items");
            case START_WALK:
            case END_WALK:
            case START_STAND:
            case END_STAND:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.Standing_And_Walking");
            case START_IN_MIDAIR:
            case END_IN_MIDAIR:
                return getBasicConfigurations().getBool("Disabled_Trigger_Checks.In_Midair");
        }
        return false;
    }
}
