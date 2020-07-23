import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.input.mouse.MouseSettings;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.prayer.Prayer;
import org.dreambot.api.methods.prayer.Prayers;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.Character;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;

@ScriptManifest(
        name = "Gargoyles Fighter",
        author = "Nico",
        version = 1.0,
        category = Category.COMBAT,
        description = "Kills gargoyles for money"
)
public class GargoylesFighter extends AbstractScript {
    /**
     * Notes:
     *      Spec if my HP is below 75, his HP is over 50%, my spec bar is over 50.
     *      Randomly hover mouse above potions for a few milliseconds.
     *      Add more elements of randomization. (random delays and mouse movements).
     *      Randomly right click ground items. (If close to me)
     *      Record loot and output it to log when scrip stops.
     *      If loot on ground is noted, right-click it. (If close to me)
     *      Small chance that I switch to Skills tab (then switch back to Inventory) while on combat.
     *      Small chance I hover mouse above potion with smallest dose for a few milliseconds.
     *      Small chance I take a 30-45 second break.
     */

    /**
     * Setup:
     *
     *      Equipment:
     *           HAT:    Helm of neitiznot OR Slayer helmet (i) (on task)
     *           CAPE:   Fire cape
     *           AMULET: Amulet of torture
     *           WEAPON: Abyssal whip
     *           CHEST:  Torag's platebody
     *           SHIELD: Avernic defender
     *           LEGS:   Verac's plateskirt
     *           HANDS:  Ferocious gloves
     *           FEET:   Primordial boots
     *           RING:   Berserker ring (i)
     *           ARROWS: Ancient blessing
     *
     *      Inventory:
     *           Saradomin godsword
     *           Super combat potion(4) x2
     *           Super restore(4) x2
     *           Slayer ring (any charge)
     *           Rune pouch (fire and nature runes)
     *           Teleport to house x10
     *           Slayer helm (i) (if off task to get past Aberrant Spectres)
     *           Rock hammer
     *           1 Chaos rune
     *           1 Death rune
     *           1 Noted gold ore
     *           1 Noted Steel bar
     *           1 Noted Mithril bar
     */

    /**
     * Loot
     */
    private final int GRANITE_MAUL_ID = 4153;
    private final int MYSTIC_ROBE_TOP_ID = 4101;
    private final int RUNE_FULL_HELM_ID = 1163;
    private final int RUNE_2H_SWORD_ID = 1319;
    private final int RUNE_BATTLEAXE_ID = 1373;
    private final int RUNE_PLATELEGS_ID = 1079;
    private final int CHAOS_RUNE_ID = 562;
    private final int DEATH_RUNE_ID = 560;
    private final int GOLD_ORE_NOTED_ID = 445;
    private final int STEEL_BAR_NOTED_ID = 2354;
    private final int MITHRIL_BAR_NOTED_ID = 2360;
    private final int RUNITE_ORE_ID = 451;
    private final int RUNE_SPEAR_ID = 1247;
    private final int SHIELD_LEFT_HALF_ID = 2366;
    private final int DRAGON_SPEAR_ID = 1249;
    private final int LOOP_HALF_OF_KEY_ID = 987;
    private final int TOOTH_HALF_OF_KEY_ID = 985;
    private final int COINS_ID = 995;

    /**
     * Inventory
     */
    private final int SARADOMIN_GODSWORD_ID = 11806;
    private final int ABYSSAL_BLUDGEON_ID = 13263;
    /*private final int ABYSSAL_WHIP_ID = 4151;*/
    /*private final int AVERNIC_DEFENDER_ID = 22322;*/
    private final int SUPER_COMBAT_4_ID = 12695;
    private final int SUPER_COMBAT_3_ID = 12697;
    private final int SUPER_COMBAT_2_ID = 12699;
    private final int SUPER_COMBAT_1_ID = 12701;
    private final int SUPER_RESTORE_4_ID = 3024;
    private final int SUPER_RESTORE_3_ID = 3026;
    private final int SUPER_RESTORE_2_ID = 3028;
    private final int SUPER_RESTORE_1_ID = 3030;
    private final int ROCK_HAMMER = 4162;
    private final int TELEPORT_TO_HOUSE_ID = 8013;

    private enum State {
        FIGHTING, IDLING, LOOTING
    }

    State state = State.IDLING;

    private final String GARGOYLE_NAME = "Gargoyle";

    Filter<NPC> availableGargoyle = npc -> {
        if (npc == null) {
            return false;
        }
        return !npc.isInCombat() && npc.getName().equals(GARGOYLE_NAME);
    };

    Filter<Item> alchableItem = item -> {
        if (item == null) {
            return false;
        }
        int id = item.getID();
        return (id == GRANITE_MAUL_ID ||
                id == MYSTIC_ROBE_TOP_ID ||
                id == RUNE_FULL_HELM_ID ||
                id == RUNE_2H_SWORD_ID ||
                id == RUNE_BATTLEAXE_ID ||
                id == RUNE_PLATELEGS_ID ||
                id == RUNE_SPEAR_ID ||
                id == SHIELD_LEFT_HALF_ID ||
                id == DRAGON_SPEAR_ID);
    };

    Filter<GroundItem> lootableItem = item -> {
        if (item == null) {
            return false;
        }
        Tile NEcorner = new Tile(3452, 3554, 2);
        Tile SWcorner = new Tile(3430, 3531, 2);
        Area lootableArea = new Area(NEcorner, SWcorner);
        if (!lootableArea.contains(item.getTile()) || item.distance() >= 10) {
            return false;
        }
        int id = item.getID();
        return (id == GRANITE_MAUL_ID ||
                id == MYSTIC_ROBE_TOP_ID ||
                id == RUNE_FULL_HELM_ID ||
                id == RUNE_2H_SWORD_ID ||
                id == RUNE_BATTLEAXE_ID ||
                id == RUNE_PLATELEGS_ID ||
                id == CHAOS_RUNE_ID ||
                id == DEATH_RUNE_ID ||
                id == GOLD_ORE_NOTED_ID ||
                id == STEEL_BAR_NOTED_ID ||
                id == MITHRIL_BAR_NOTED_ID ||
                id == RUNITE_ORE_ID ||
                id == RUNE_SPEAR_ID ||
                id == SHIELD_LEFT_HALF_ID ||
                id == DRAGON_SPEAR_ID ||
                id == LOOP_HALF_OF_KEY_ID ||
                id == TOOTH_HALF_OF_KEY_ID ||
                (id == COINS_ID && item.getAmount() >= 10000));
    };

    private NPC getNextGargoyle() {
        Character characterInteractingWithMe = getLocalPlayer().getCharacterInteractingWithMe();
        if (characterInteractingWithMe != null) {
            if (characterInteractingWithMe.getName().equals(GARGOYLE_NAME)) {
                return (NPC) characterInteractingWithMe;
            } else {
                return getNpcs().closest(availableGargoyle);
            }
        } else {
            return getNpcs().closest(availableGargoyle);
        }
    }

    private void attackNextGargoyle() {
        moveMouseIntoScreen();
        getNextGargoyle().interact("Attack");
        possiblyDoubleClick();
    }

    private void turnOnAutoRetaliateAndPrayer() {
        turnOnAutoRetaliate();
        turnOnPrayer();
        openTab(Tab.INVENTORY);
    }

    private void turnOnAutoRetaliate() {
        if (!getCombat().isAutoRetaliateOn()) {
            openTab(Tab.COMBAT);
            getCombat().toggleAutoRetaliate(true);
        }
    }

    private void turnOnPrayer() {
        Prayers prayers = getPrayer();
        if (!prayers.isActive(Prayer.PRESERVE) || !prayers.isActive(Prayer.RAPID_HEAL)) {
            openTab(Tab.PRAYER);
            prayers.toggle(true, Prayer.PRESERVE);
            prayers.toggle(true, Prayer.RAPID_HEAL);
        }
    }

    private void assureRequiredItems() {
        if (!getInventory().contains(ROCK_HAMMER) ||
                !getInventory().contains(SARADOMIN_GODSWORD_ID)) {
            logError("Rock hammer or Saradomin Godsword not in inventory.");
            homeTele();
        }
    }

    private void specSwitch() {
        int myHitpoints = getSkills().getBoostedLevels(Skill.HITPOINTS);
        int specPercentage = getCombat().getSpecialPercentage();
        Character characterInteractingWithMe = getLocalPlayer().getCharacterInteractingWithMe();
        if (characterInteractingWithMe != null &&
                characterInteractingWithMe.getName().equals(GARGOYLE_NAME) &&
                characterInteractingWithMe.getHealthPercent() > 50 &&
                specPercentage >= 50 &&
                myHitpoints < 75) {
            openTab(Tab.INVENTORY);
            wieldWeapon(SARADOMIN_GODSWORD_ID);
            doSpecialAttack();
            wieldWeapon(ABYSSAL_BLUDGEON_ID);
            if (getMouse().isMouseInScreen()) {
                moveMouseOutOfScreen();
            }
        }
    }

    private void wieldWeapon(int itemId) {
        Inventory inventory = getInventory();
        if (getInventory().contains(itemId)) {
            inventory.interact(itemId, "Wield");
        }
    }

    private void doSpecialAttack() {
        openTab(Tab.COMBAT);
        getCombat().toggleSpecialAttack(true);
        getLocalPlayer().getCharacterInteractingWithMe().interact();
        sleep(Calculations.random(75, 200));
        openTab(Tab.INVENTORY);
        getMouse().move(getInventory().slotBounds(0));
        sleep(Calculations.random(1000, 1300));
    }

    private void homeTele() {
        openTab(Tab.INVENTORY);
        getInventory().interact(TELEPORT_TO_HOUSE_ID, "Break");
        logError("Stopping script.");
        stop();
    }

    private void openTab(Tab tab) {
        if (getTabs().isOpen(tab)) {
            return;
        }
        int probabilityOfUsingFkey = Calculations.random(100);
        if (probabilityOfUsingFkey > 15) {
            getTabs().openWithFKey(tab);
        } else {
            getTabs().openWithMouse(tab);
        }
    }

    private void lootItem(GroundItem groundItem) {
        groundItem.interact("Take");
        sleep(Calculations.random(500, 1000));
    }

    private void lootItems() {
        List<GroundItem> loot = getGroundItems().all(lootableItem);
        if (!loot.isEmpty()) {
            sleep(Calculations.random(1000, 2000));
            moveMouseIntoScreen();
        } else {
            return;
        }
        Collections.reverse(loot);
        loot.forEach(this::lootItem);
        alchItems(getInventory().all(alchableItem));
        sleep(Calculations.random(750, 1000));
        /*moveMouseToTopAreaOfInventory();*/
        /*freeUpWeaponSlots();*/ // Not needed when using Bludgeon
    }

    private void peekSkillsTab() {
        openTab(Tab.SKILLS);
        sleep(Calculations.random(750, 1250));
        openTab(Tab.INVENTORY);
        sleep(Calculations.random(50, 200));
        moveMouseToTopAreaOfInventory();
    }

    private void moveMouseToTopAreaOfInventory() {
        Point topLeftPoint = new Point(580, 215);
        int width = 100;
        int height = 75;
        Dimension dimension = new Dimension(width, height);
        Rectangle topAreaOfInventory = new Rectangle(topLeftPoint, dimension);
        getMouse().move(topAreaOfInventory);
    }

    private void alchItems(List<Item> items) {
        if (items.isEmpty()) {
            return;
        }
        openTab(Tab.MAGIC);
        items.forEach(this::alchItem);
        openTab(Tab.INVENTORY);
    }

    private void alchItem(Item item) {
        getMagic().castSpell(Normal.HIGH_LEVEL_ALCHEMY);
        getInventory().get(item.getID()).interact();
        sleep(Calculations.random(50, 150));
        /*Point highAlchTopLeftCorner = new Point(706, 319);
        int highAlchWidthAndHeight = 17;
        Rectangle highAlchArea = new Rectangle(highAlchTopLeftCorner,
                new Dimension(highAlchWidthAndHeight, highAlchWidthAndHeight));
        getMouse().move(highAlchArea);*/
        openTab(Tab.INVENTORY);
        moveMouseToTopAreaOfInventory();
        sleep(Calculations.random(2700, 2900));
    }

    private void moveMouseIntoScreen() {
        if (!getMouse().isMouseInScreen()) {
            getMouse().hop(new Point(new Point(764, Calculations.random(75, 425))));
        }
    }

    private void moveMouseOutOfScreen() {
        getMouse().move(new Point(765, Calculations.random(75, 425)));
        sleep(Calculations.random(1000, 2000));
    }

    private void freeUpWeaponSlots() {
        MouseSettings.setSpeed(3);
        Item itemInFirstSlot = getInventory().getItemInSlot(0);
        Item itemInSecondSlot = getInventory().getItemInSlot(1);
        final int INVALID_SLOT = -1;
        int slotToMove = INVALID_SLOT;
        if (itemInFirstSlot != null && itemInFirstSlot.getID() != SARADOMIN_GODSWORD_ID) {
            slotToMove = 0;
        } else if (itemInSecondSlot != null && itemInSecondSlot.getID() != SARADOMIN_GODSWORD_ID) {
            slotToMove = 1;
        }
        if (slotToMove != INVALID_SLOT) {
            getMouse().move(getInventory().slotBounds(slotToMove));
            getMouse().drag(getInventory().slotBounds(getInventory().getFirstEmptySlot()));
        }
        getMouse().getMouseSettings().resetSpeed();
    }

    private void possiblyDoubleClick() {
        int probabilityOfDoubleClicking = Calculations.random(100);
        boolean shouldDoubleClick = probabilityOfDoubleClicking < 10;
        sleep(5, 15);
        if (shouldDoubleClick) {
            getMouse().click();
        }
    }

    private void drinkPotions() {
        drinkSuperCombatPotion();
        drinkSuperRestorePotion();
    }

    private void drinkSuperCombatPotion() {
        int boostedStrengthLevel = getSkills().getBoostedLevels(Skill.STRENGTH);
        if (boostedStrengthLevel >= 113) {
            return;
        }
        int probabilityOfPotting = Calculations.random(100);
        if (probabilityOfPotting > 10) {
            return;
        }
        Filter<Item> superCombatFilter = item -> {
            if (item == null) {
                return false;
            }
            int id = item.getID();
            return (id == SUPER_COMBAT_4_ID ||
                    id == SUPER_COMBAT_3_ID ||
                    id == SUPER_COMBAT_2_ID ||
                    id == SUPER_COMBAT_1_ID);
        };
        Inventory inventory = getInventory();
        boolean isPotionInInventory = inventory.contains(superCombatFilter);
        if (!isPotionInInventory) {
            return;
        }
        moveMouseIntoScreen();
        boolean interacted;
        interacted = inventory.interact(SUPER_COMBAT_1_ID, "Drink");
        if (!interacted) {
            interacted = inventory.interact(SUPER_COMBAT_2_ID, "Drink");
        }
        if (!interacted) {
            interacted = inventory.interact(SUPER_COMBAT_3_ID, "Drink");
        }
        if (!interacted) {
            interacted = inventory.interact(SUPER_COMBAT_4_ID, "Drink");
        }
        possiblyDoubleClick();
        sleep(Calculations.random(200, 600));
        if (getMouse().isMouseInScreen()) {
            moveMouseOutOfScreen();
        }
    }

    private void drinkSuperRestorePotion() {
        int boostedPrayerLevel = getSkills().getBoostedLevels(Skill.PRAYER);
        if (boostedPrayerLevel >= 10) {
            return;
        }
        int probabilityOfPotting = Calculations.random(100);
        if (probabilityOfPotting > 10) {
            return;
        }
        Filter<Item> superRestoreFilter = item -> {
            if (item == null) {
                return false;
            }
            int id = item.getID();
            return (id == SUPER_RESTORE_4_ID ||
                    id == SUPER_RESTORE_3_ID ||
                    id == SUPER_RESTORE_2_ID ||
                    id == SUPER_RESTORE_1_ID);
        };
        Inventory inventory = getInventory();
        boolean isPotionInInventory = inventory.contains(superRestoreFilter);
        if (!isPotionInInventory) {
            return;
        }
        moveMouseIntoScreen();
        boolean interacted;
        interacted = inventory.interact(SUPER_RESTORE_1_ID, "Drink");
        if (!interacted) {
            interacted = inventory.interact(SUPER_RESTORE_2_ID, "Drink");
        }
        if (!interacted) {
            interacted = inventory.interact(SUPER_RESTORE_3_ID, "Drink");
        }
        if (!interacted) {
            interacted = inventory.interact(SUPER_RESTORE_4_ID, "Drink");
        }
        possiblyDoubleClick();
        sleep(Calculations.random(200, 600));
        if (getMouse().isMouseInScreen()) {
            moveMouseOutOfScreen();
        }
    }

    private void possiblyTakeBreak() {
        int probabilityOfTakingABreak = Calculations.random(150);
        if (probabilityOfTakingABreak == 1) {
            int twentySeconds = 1000 * 20;
            int twoMinutes = 1000 * 60 * 2;
            int random = Calculations.random(twentySeconds, twoMinutes);
            logInfo("Taking a + " + random + " second break.");
            sleep(random);
        }
    }

    private void checkIfTripDone() {
        boolean isInventoryFull = getInventory().isFull();
        boolean isLowOnHealth = getSkills().getBoostedLevels(Skill.HITPOINTS) < 25;
        if (isInventoryFull || isLowOnHealth) {
            homeTele();
        }
    }

    @Override
    public int onLoop() {
        log("State: " + state.name());
        checkIfTripDone();
        switch (state) {
            case IDLING: {
                possiblyTakeBreak();
                attackNextGargoyle();
                if (getMouse().isMouseInScreen()) {
                    moveMouseOutOfScreen();
                }
                state = State.FIGHTING;
            }
            case FIGHTING: {
                lootItems();
                drinkPotions();
                specSwitch();
                if (!getLocalPlayer().isInCombat()) {
                    state = State.IDLING;
                }
            }
        }
        return Calculations.random(1000, 1500);
    }

    @Override
    public void onStart() {
        super.onStart();
        turnOnAutoRetaliateAndPrayer();
        assureRequiredItems();
    }
}
