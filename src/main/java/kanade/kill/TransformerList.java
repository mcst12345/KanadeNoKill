package kanade.kill;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("unchecked")
public class TransformerList<E> extends ArrayList<E> {
    public TransformerList(Collection<? extends E> var1){
        super(var1);
        super.add((E) Transformer.instance);
    }

    @Override
    public boolean add(E var1) {
        super.add(var1);
        super.set(super.size()-2,var1);
        super.set(super.size()-1, (E) Transformer.instance);
        return true;
    }
}
