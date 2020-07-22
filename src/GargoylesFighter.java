import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.input.mouse.MouseSettings;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.prayer.Prayer;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.Item;

import java.awt.Point;

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
     *      Drink super combat potion when stats are below 113.
     *      Drink super restore potion when prayer is below 10.
     *      Home tele if HP is below 30.
     *      Spec if my HP is below 75, his HP is over 50%, my spec bar is 50 or above.
     *      Randomly double click them when attacking.
     *      Randomly hover mouse above potions for a few milliseconds.
     *      Nechryals (adjacent room) have overlapping drops. Assure I only try looting stuff in Gargoyles room.
     *
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
     *
     *      Drops:
     *          Granite maul [alch] -> 4153
     *          Mystic robe top (dark) [alch] -> 4101
     *          Rune full helm [alch] -> 1163
     *          Rune 2h sword [alch] -> 1319
     *          Rune battleaxe [alch] -> 1373
     *          Rune platelegs [alch] -> 1079
     *          Chaos rune -> 562
     *          Death rune -> 560
     *          Gold ore (noted) -> 445
     *          Steel bar (noted) -> 2354
     *          Runite ore -> 451
     *          Coins (only pick up 10k stack) -> 995
     */

    /**
     * Main differences from Abyssal Demons:
     *      They are aggresive.
     *      You need to finish them off with a rock hammer.
     *      Have a lot more drops.
     *      Not praying boosts so I can take random breaks.
     *      A lot more people around me.
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
    private final int GOLD_ORE_NOTED_ID = 445;
    private final int STEEL_BAR_NOTED_ID = 2354;
    private final int RUNITE_ORE_ID = 451;
    private final int COINS_ID = 995;

    private final String GRANITE_MAUL_NAME = "Granite maul";
    private final String MYSTIC_ROBE_TOP_NAME = "Mystic robe top (dark)";
    private final String RUNE_FULL_HELM_NAME = "Rune full helm";
    private final String RUNE_2H_SWORD_NAME = "Rune 2h sword";
    private final String RUNE_BATTLEAXE_NAME = "Rune battleaxe";
    private final String RUNE_PLATELEGS_NAME = "Rune platelegs";
    private final String CHAOS_RUNE_NAME = "Chaos rune";
    private final String GOLD_ORE_NOTED_NAME = "Gold ore";
    private final String STEEL_BAR_NOTED_NAME = "Steel bar";
    private final String RUNITE_ORE_NAME = "Runite ore";
    private final String COINS_NAME = "Coins";

    /**
     * Inventory
     */
    private final int SARADOMIN_GODSWORD_ID = 11806;
    private final int ABYSSAL_WHIP_ID = 4151;
    private final int AVERNIC_DEFENDER_ID = 22322;
    private final int SUPER_COMBAT_4_ID = 12695;
    private final int SUPER_COMBAT_3_ID = 12697;
    private final int SUPER_COMBAT_2_ID = 12699;
    private final int SUPER_COMBAT_1_ID = 12701;
    private final int SUPER_RESTORE_4_ID = 3024;
    private final int SUPER_RESTORE_3_ID = 3026;
    private final int SUPER_RESTORE_2_ID = 3028;
    private final int SUPER_RESTORE_1_ID = 3030;
    private final int TELEPORT_TO_HOUSE_ID = 8013;

    private final String SARADOMIN_GODSWORD_NAME = "Saradomin godsword";
    private final String ABYSSAL_WHIP_NAME = "Abyssal whip";
    private final String AVERNIC_DEFENDER_NAME = "Avernic defender";
    private final String SUPER_COMBAT_4_NAME = "Super combat potion(4)";
    private final String SUPER_COMBAT_3_NAME = "Super combat potion(3)";
    private final String SUPER_COMBAT_2_NAME = "Super combat potion(2)";
    private final String SUPER_COMBAT_1_NAME = "Super combat potion(1)";
    private final String SUPER_RESTORE_4_NAME = "Super restore potion(4)";
    private final String SUPER_RESTORE_3_NAME = "Super restore potion(3)";
    private final String SUPER_RESTORE_2_NAME = "Super restore potion(2)";
    private final String SUPER_RESTORE_1_NAME = "Super restore potion(1)";
    private final String TELEPORT_TO_HOUSE_NAME = "Teleport to house";

    private enum State {
        FIGHTING, IDLING, LOOTING
    }

    Filter<NPC> availableGargoyle = npc -> npc != null && npc.getName().equals("Gargoyle") && !npc.isInCombat();

    Filter<Item> alchableItem = item -> {
        int id = item.getID();
        return (id == GRANITE_MAUL_ID ||
                id == MYSTIC_ROBE_TOP_ID ||
                id == RUNE_FULL_HELM_ID ||
                id == RUNE_2H_SWORD_ID ||
                id == RUNE_BATTLEAXE_ID ||
                id == RUNE_PLATELEGS_ID);
    };

    Filter<Item> lootableItem = item -> {
        if (item.getID() == COINS_ID && item.getAmount() >= 10000) {
            return true;
        }
        int id = item.getID();
        return (id == GRANITE_MAUL_ID ||
                id == MYSTIC_ROBE_TOP_ID ||
                id == RUNE_FULL_HELM_ID ||
                id == RUNE_2H_SWORD_ID ||
                id == RUNE_BATTLEAXE_ID ||
                id == RUNE_PLATELEGS_ID ||
                id == CHAOS_RUNE_ID ||
                id == GOLD_ORE_NOTED_ID ||
                id == STEEL_BAR_NOTED_ID ||
                id == RUNITE_ORE_ID);
    };

    private void turnOnAutoRetaliateAndPreserve() {
        turnOnAutoRetaliate();
        turnOnPreserve();
        openTab(Tab.INVENTORY);
    }

    private void turnOnAutoRetaliate() {
        openTab(Tab.COMBAT);
        getCombat().toggleAutoRetaliate(true);
    }

    private void turnOnPreserve() {
        openTab(Tab.PRAYER);
        getPrayer().toggle(true, Prayer.PRESERVE);
    }

    private void homeTele() {
        openTab(Tab.INVENTORY);
        getInventory().interact(TELEPORT_TO_HOUSE_ID, "Break");
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

    private void alchItem(Item item) {
        openTab(Tab.MAGIC);
        getMagic().castSpell(Normal.HIGH_LEVEL_ALCHEMY);
        getInventory().get(item.getID()).interact();
        openTab(Tab.INVENTORY);
    }

    private void moveMouseIntoScreen() {
        getMouse().hop(new Point(new Point(764, Calculations.random(75, 425))));
    }

    private void moveMouseOutOfScreen() {
        getMouse().move(new Point(765, Calculations.random(75, 425)));
    }

    private void freeUpWeaponSlots() {
        MouseSettings.setSpeed(5);
        Item itemInFirstSlot = getInventory().getItemInSlot(0);
        Item itemInSecondSlot = getInventory().getItemInSlot(1);
        int slotToMove = -1;
        if (itemInFirstSlot != null && itemInFirstSlot.getID() != SARADOMIN_GODSWORD_ID) {
            slotToMove = 0;
        } else if (itemInSecondSlot != null && itemInSecondSlot.getID() != SARADOMIN_GODSWORD_ID) {
            slotToMove = 1;
        }
        if (slotToMove != -1) {
            getMouse().move(getInventory().slotBounds(slotToMove));
            getMouse().drag(getInventory().slotBounds(getInventory().getFirstEmptySlot()));
        }
        getMouse().getMouseSettings().resetSpeed();
    }

    @Override
    public int onLoop() {
        return Calculations.random(1000, 20000);
    }

    @Override
    public void onStart() {
        super.onStart();
        turnOnAutoRetaliateAndPreserve();
    }
}
