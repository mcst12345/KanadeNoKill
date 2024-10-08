package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface DebugVC50SymbolTypes {
    int S_COMPILE = 1;
    int S_SSEARCH = 5;
    int S_END = 6;
    int S_SKIP = 7;
    int S_CVRESERVE = 8;
    int S_OBJNAME = 9;
    int S_ENDARG = 10;
    int S_COBOLUDT = 11;
    int S_MANYREG = 12;
    int S_RETURN = 13;
    int S_ENTRYTHIS = 14;
    int S_REGISTER = 4097;
    int S_CONSTANT = 4098;
    int S_UDT = 4099;
    int S_COBOLUDT2 = 4100;
    int S_MANYREG2 = 4101;
    int S_BPREL32 = 4102;
    int S_LDATA32 = 4103;
    int S_GDATA32 = 4104;
    int S_PUB32 = 4105;
    int S_LPROC32 = 4106;
    int S_GPROC32 = 4107;
    int S_THUNK32 = 518;
    int S_BLOCK32 = 519;
    int S_WITH32 = 520;
    int S_LABEL32 = 521;
    int S_CEXMODEL32 = 522;
    int S_VFTTABLE32 = 4108;
    int S_REGREL32 = 4109;
    int S_LTHREAD32 = 4110;
    int S_GTHREAD32 = 4111;
    int S_LPROCMIPS = 4112;
    int S_GPROCMIPS = 4113;
    int S_PROCREF = 1024;
    int S_DATAREF = 1025;
    int S_ALIGN = 1026;
}
