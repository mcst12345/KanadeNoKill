package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface OptionalHeader {
    short MAGIC_PE32 = 267;
    short MAGIC_PE32_PLUS = 523;
    short MAGIC_ROM_IMAGE = 263;

    short getMagicNumber();

    OptionalHeaderStandardFields getStandardFields();

    OptionalHeaderWindowsSpecificFields getWindowsSpecificFields();

    OptionalHeaderDataDirectories getDataDirectories();
}
