package kanade.kill.util;

import kanade.kill.item.KillItem;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

public class KanadeArrayList<E> extends ArrayList<E> {
    public boolean add(E e) {
        if (e instanceof Entity && Util.isDead((Entity) e)) {
            return false;
        }
        return super.add(e);
    }

    public void add(int index, E element) {
        if (element instanceof Entity && Util.isDead((Entity) element)) {
            return;
        }
        super.add(index, element);
    }

    public E remove(int index) {
        E e = super.get(index);
        if (KillItem.inList(e) || Util.NoRemove(e)) {
            return e;
        }
        return super.remove(index);
    }

    public boolean remove(Object o) {
        if (KillItem.inList(o) || Util.NoRemove(o)) {
            return false;
        }
        return super.remove(o);
    }

    public void clear() {
        super.removeIf(o -> !(KillItem.inList(o) || Util.NoRemove(o)));
    }

    public void replaceAll(UnaryOperator<E> operator) {
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        List<E> list = new ArrayList<>();
        for (E e : c) {
            if (!(e instanceof Entity && Util.isDead((Entity) e))) {
                list.add(e);
            }
        }
        return super.addAll(index, list);
    }

    protected void removeRange(int fromIndex, int toIndex) {
    }

    public E set(int index, E e) {
        E o = super.get(index);
        if (Util.NoRemove(o) || KillItem.inList(o)) {
            return o;
        }
        return super.set(index, e);
    }
}
