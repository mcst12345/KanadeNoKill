package kanade.kill;

import net.minecraft.item.Item;

public class Empty {
    Item item;

    public boolean isEmpty() {
        if (item == ModMain.kill_item) {
            return false;
        }
        return item == null;
    }
}
