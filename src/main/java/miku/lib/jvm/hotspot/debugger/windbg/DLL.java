package miku.lib.jvm.hotspot.debugger.windbg;

import me.xdark.shell.JVMUtil;
import miku.lib.jvm.hotspot.debugger.win32.coff.COFFFile;
import miku.lib.jvm.hotspot.debugger.win32.coff.COFFFileParser;
import miku.lib.jvm.hotspot.debugger.win32.coff.ExportDirectoryTable;

public class DLL {
    public static long lookupSymbolOffset(String symbol) {
        ExportDirectoryTable exports = getExportDirectoryTable();
        return lookupSymbol(symbol, exports, 0, exports.getNumberOfNamePointers() - 1);
    }

    private static long lookupSymbol(String symbol, ExportDirectoryTable exports, int loIdx, int hiIdx) {
        do {
            int curIdx = loIdx + hiIdx >> 1;
            String cur = exports.getExportName(curIdx);
            if (symbol.equals(cur)) {
                return (long) exports.getExportAddress(exports.getExportOrdinal(curIdx)) & 4294967295L;
            }

            if (symbol.compareTo(cur) < 0) {
                if (hiIdx == curIdx) {
                    hiIdx = curIdx - 1;
                } else {
                    hiIdx = curIdx;
                }
            } else if (loIdx == curIdx) {
                loIdx = curIdx + 1;
            } else {
                loIdx = curIdx;
            }
        } while (loIdx <= hiIdx);

        return 0L;
    }

    private static ExportDirectoryTable getExportDirectoryTable() {
        return file.getHeader().getOptionalHeader().getDataDirectories().getExportDirectoryTable();
    }

    private static final COFFFile file = COFFFileParser.getParser().parse(JVMUtil.LIBJVM.toAbsolutePath().toString());
}
