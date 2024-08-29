package miku.lib.jvm.hotspot.runtime;

import miku.lib.jvm.hotspot.oops.Symbol;

public class ResultTypeFinder extends SignatureInfo {
    public ResultTypeFinder(Symbol signature) {
        super(signature);
    }

    @Override
    protected void set(int size, int type) {
        if (this.isReturnType()) {
            this.type = type;
        }
    }
}
