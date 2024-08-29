package miku.lib.jvm.hotspot.oops;

import java.util.ArrayList;
import java.util.List;

public class CellTypeStateList {
    private List<CellTypeState> list;

    public CellTypeStateList(int size) {
        this.list = new ArrayList<>(size);

        for (int i = 0; i < size; ++i) {
            this.list.add(i, CellTypeState.makeBottom());
        }

    }

    public int size() {
        return this.list.size();
    }

    public CellTypeState get(int i) {
        return this.list.get(i);
    }

    public CellTypeStateList subList(int fromIndex, int toIndex) {
        return new CellTypeStateList(this.list.subList(fromIndex, toIndex));
    }

    private CellTypeStateList(List<CellTypeState> list) {
        this.list = list;
    }
}
