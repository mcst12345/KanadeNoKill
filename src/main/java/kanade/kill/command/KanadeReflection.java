package kanade.kill.command;

import kanade.kill.reflection.ReflectionUtil;
import kanade.kill.util.ObjectUtil;
import kanade.kill.util.memory.MemoryHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class KanadeReflection extends CommandBase {
    private static final Object NULL = new Object();
    Stack<Object> stack = new Stack<>();

    private static Class getClass(String name) throws ClassNotFoundException {
        switch (name) {
            case "int":
                return int.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "short":
                return short.class;
            case "long":
                return long.class;
            case "char":
                return char.class;
            case "byte":
                return byte.class;
            case "boolean":
                return boolean.class;
            default:
                return Class.forName(name);
        }
    }

    @Override
    @Nonnull
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> ret = new ArrayList<>();
        if (args.length == 1) {
            ret.add("getStatic");
            ret.add("putStatic");
            ret.add("printStack");
            ret.add("invoke");
            ret.add("stack");
        } else if (args.length == 2) {
            if (args[0].equals("stack")) {
                ret.add("CONST_NULL");
                ret.add("CONST_INT");
                ret.add("CONST_BOOLEAN");
                ret.add("CONST_STRING");
                ret.add("CONST_LONG");
                ret.add("CONST_DOUBLE");
                ret.add("CONST_SHORT");
                ret.add("CONST_CHAR");
                ret.add("CONST_BYTE");
                ret.add("CONST_FLOAT");
                ret.add("POP");
                ret.add("POP2");
                ret.add("DUP");
                ret.add("DUP2");
                ret.add("SWAP");
            }
        }
        return getListOfStringsMatchingLastWord(args, ret);
    }

    @Override
    @Nonnull
    public String getName() {
        return "KanadeReflection";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "/KanadeReflection <operation> <arguments...>";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(new TextComponentString(getUsage(sender)));
            return;
        }
        String operation = args[0];
        switch (operation) {
            case "getStatic": {
                if (args.length < 3) {
                    sender.sendMessage(new TextComponentString("Too few arguments!"));
                    return;
                }
                String clz = args[1];
                String field = args[2];
                try {
                    Class<?> c = Class.forName(clz);
                    Field f = ReflectionUtil.getField(c, field);
                    Object o = ObjectUtil.getStatic(f);
                    sender.sendMessage(new TextComponentString("Successfully get static:" + o));
                    stack.push(o != null ? o : NULL);
                } catch (ClassNotFoundException e) {
                    sender.sendMessage(new TextComponentString("Class:" + clz + " not found!"));
                } catch (NoSuchFieldException e) {
                    sender.sendMessage(new TextComponentString("Field:" + field + " not found!"));
                }
                break;
            }
            case "putStatic": {
                if (args.length < 3) {
                    sender.sendMessage(new TextComponentString("Too few arguments!"));
                    return;
                }
                String clz = args[1];
                String field = args[2];
                Object o;
                if (args.length > 3) {
                    int index = Integer.parseInt(args[3]);
                    if (index > stack.size() - 1) {
                        sender.sendMessage(new TextComponentString("Index out of bound!"));
                        return;
                    }
                    o = stack.get(index);
                } else {
                    o = stack.pop();
                }
                try {
                    Class<?> c = Class.forName(clz);
                    Field f = ReflectionUtil.getField(c, field);
                    ObjectUtil.putStatic(f, o);
                    sender.sendMessage(new TextComponentString("Successfully put static:" + o));
                } catch (ClassNotFoundException e) {
                    sender.sendMessage(new TextComponentString("Class:" + clz + " not found!"));
                } catch (NoSuchFieldException e) {
                    sender.sendMessage(new TextComponentString("Field:" + field + " not found!"));
                }
                break;
            }
            case "printStack": {
                for (Object o : stack) {
                    sender.sendMessage(new TextComponentString(o != NULL ? MemoryHelper.getClassName(o.getClass()) + ":" + o : "null"));
                }
                break;
            }
            case "invoke": {
                if (args.length < 4) {
                    sender.sendMessage(new TextComponentString("Too few arguments!"));
                }
                String clz = args[1];
                String method = args[2];
                String desc = args[3];
                try {
                    Class<?> c = Class.forName(clz);
                    Type type = Type.getMethodType(desc);
                    Type[] arguments = type.getArgumentTypes();
                    if (arguments.length != 0 && stack.size() < arguments.length + 1) {
                        sender.sendMessage(new TextComponentString("Not enough objects!"));
                        return;
                    }
                    Class[] arg = new Class[arguments.length];
                    for (int i = 0; i < arguments.length; i++) {
                        arg[i] = getClass(arguments[i].getClassName());
                    }
                    Object obj = null;
                    Method m = ReflectionUtil.getMethod(c, method, arg);
                    Object[] vars = new Object[arguments.length];
                    for (int i = 0; i < arguments.length; i++) {
                        Object tmp = stack.pop();
                        vars[i] = tmp == NULL ? null : tmp;
                    }

                    Object result = ReflectionUtil.invoke(m, obj, vars);
                    if (result != null) {
                        stack.push(result);
                    }
                } catch (ClassNotFoundException e) {
                    sender.sendMessage(new TextComponentString("Class:" + clz + " not found!"));
                } catch (Exception e) {
                    sender.sendMessage(new TextComponentString("Method" + method + " not found!"));
                }
                break;
            }
            case "stack": {
                if (args.length < 2) {
                    sender.sendMessage(new TextComponentString("Too few arguments!"));
                    return;
                }
                operation = args[1];
                switch (operation) {
                    case "CONST_NULL": {
                        stack.push(NULL);
                        break;
                    }
                    case "CONST_INT": {
                        if (args.length < 3) {
                            sender.sendMessage(new TextComponentString("Please provide a value!"));
                            return;
                        }
                        int i = Integer.parseInt(args[2]);
                        stack.push(i);
                        sender.sendMessage(new TextComponentString("Pushed:" + i));
                        break;
                    }
                    case "CONST_FLOAT": {
                        if (args.length < 3) {
                            sender.sendMessage(new TextComponentString("Please provide a value!"));
                            return;
                        }
                        float i = Float.parseFloat(args[2]);
                        stack.push(i);
                        sender.sendMessage(new TextComponentString("Pushed:" + i));
                        break;
                    }
                    case "CONST_SHORT": {
                        if (args.length < 3) {
                            sender.sendMessage(new TextComponentString("Please provide a value!"));
                            return;
                        }
                        short i = Short.parseShort(args[2]);
                        stack.push(i);
                        sender.sendMessage(new TextComponentString("Pushed:" + i));
                        break;
                    }
                    case "CONST_BOOLEAN": {
                        if (args.length < 3) {
                            sender.sendMessage(new TextComponentString("Please provide a value!"));
                            return;
                        }
                        boolean i = Boolean.parseBoolean(args[2]);
                        stack.push(i);
                        sender.sendMessage(new TextComponentString("Pushed:" + i));
                        break;
                    }
                    case "CONST_LONG": {
                        if (args.length < 3) {
                            sender.sendMessage(new TextComponentString("Please provide a value!"));
                            return;
                        }
                        long i = Long.parseLong(args[2]);
                        stack.push(i);
                        sender.sendMessage(new TextComponentString("Pushed:" + i));
                        break;
                    }
                    case "CONST_STRING": {
                        if (args.length < 3) {
                            sender.sendMessage(new TextComponentString("Please provide a value!"));
                            return;
                        }
                        stack.push(args[2]);
                        sender.sendMessage(new TextComponentString("Pushed:" + args[2]));
                        break;
                    }
                    case "CONST_DOUBLE": {
                        if (args.length < 3) {
                            sender.sendMessage(new TextComponentString("Please provide a value!"));
                            return;
                        }
                        double i = Double.parseDouble(args[2]);
                        stack.push(i);
                        sender.sendMessage(new TextComponentString("Pushed:" + i));
                        break;
                    }
                    case "CONST_CHAR": {
                        if (args.length < 3) {
                            sender.sendMessage(new TextComponentString("Please provide a value!"));
                            return;
                        }
                        String s = args[2];
                        if (s.length() != 1) {
                            sender.sendMessage(new TextComponentString("This is not a char!"));
                            return;
                        }
                        char i = s.charAt(0);
                        stack.push(i);
                        sender.sendMessage(new TextComponentString("Pushed:" + i));
                        break;
                    }
                    case "CONST_BYTE": {
                        if (args.length < 3) {
                            sender.sendMessage(new TextComponentString("Please provide a value!"));
                            return;
                        }
                        byte i = Byte.parseByte(args[2]);
                        stack.push(i);
                        sender.sendMessage(new TextComponentString("Pushed:" + i));
                        break;
                    }
                    case "POP": {
                        Object o = stack.pop();
                        sender.sendMessage(new TextComponentString("popped object:" + o));
                        break;
                    }
                    case "DUP": {
                        stack.push(stack.peek());
                        sender.sendMessage(new TextComponentString("duped:" + stack.peek()));
                        break;
                    }
                    case "POP2": {
                        Object o1 = stack.pop();
                        Object o2 = stack.pop();
                        sender.sendMessage(new TextComponentString("popped object:" + o1 + "," + o2));
                        break;
                    }
                    case "DUP2": {
                        stack.push(stack.peek());
                        stack.push(stack.peek());
                        sender.sendMessage(new TextComponentString("duped:" + stack.peek()));
                        break;
                    }
                    case "SWAP": {
                        Object o1 = stack.pop();
                        Object o2 = stack.pop();
                        stack.push(o1);
                        stack.push(o2);
                        sender.sendMessage(new TextComponentString("swapped:" + o1 + ":" + o2));
                        break;
                    }
                    default: {
                        sender.sendMessage(new TextComponentString("Unknown:" + operation));
                        return;
                    }
                }
                break;
            }
            default: {
                sender.sendMessage(new TextComponentString("Unknown:" + operation));
            }
        }
    }
}
