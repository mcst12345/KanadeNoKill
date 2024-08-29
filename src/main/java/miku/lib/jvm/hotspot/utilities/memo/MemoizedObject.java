package miku.lib.jvm.hotspot.utilities.memo;

public abstract class MemoizedObject {
    private boolean computed;
    private Object value;

    public MemoizedObject() {
    }

    protected abstract Object computeValue();

    public Object getValue() {
        if (!this.computed) {
            this.value = this.computeValue();
            this.computed = true;
        }

        return this.value;
    }
}
