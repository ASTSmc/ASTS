package tw.asts.mc.asts.util;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;

final public class ExternalClass {
    private Class<?> clazz = null;
    private Object instance = null;
    private boolean isEnabledPlugin = true;

    public ExternalClass(@NotNull Server server, @NotNull String requiredPlugin, @NotNull String name) {
        this(server, List.of(requiredPlugin), name);
    }
    public ExternalClass(@NotNull Server server, @NotNull List<String> requiredPlugins, @NotNull String name) {
        this(server, requiredPlugins, name, null, null);
    }
    public ExternalClass(@NotNull Server server, @NotNull String requiredPlugin, @NotNull String name, List<Class<?>> parameterTypes, List<Object> args) {
        this(server, List.of(requiredPlugin), name, parameterTypes, args);
    }
    public ExternalClass(@NotNull Server server, @NotNull List<String> requiredPlugins, @NotNull String name, List<Class<?>> parameterTypes, List<Object> args) {
        for (String plugin : requiredPlugins) {
            if (server.getPluginManager().getPlugin(plugin) == null) {
                isEnabledPlugin = false;
                break;
            }
        }
        if (parameterTypes == null) {
            parameterTypes = List.of();
        }
        if (args == null) {
            args = List.of();
        }
        if (isEnabledPlugin) {
            try {
                clazz = Class.forName(name);
                instance = clazz
                        .getConstructor(parameterTypes.toArray(new Class<?>[0]))
                        .newInstance(args.toArray(new Object[0]));
            } catch (Exception e) {
                Log.get().log(Level.WARNING, "無法載入外部類別: " + name, e);
            }
        }
    }

    public Object runMethod(@NotNull String name) {
        return runMethod(name, null, null);
    }

    public Object runMethod(@NotNull String name, List<Class<?>> parameterTypes, List<Object> args) {
        if (clazz == null || instance == null || !isEnabledPlugin) {
            return null;
        }
        if (parameterTypes == null) {
            parameterTypes = List.of();
        }
        if (args == null) {
            args = List.of();
        }
        try {
            Method method = clazz.getMethod(name, parameterTypes.toArray(new Class<?>[0]));
            return method.invoke(instance, args.toArray(new Object[0]));
        } catch (Exception e) {
            Log.get().log(Level.WARNING, "無法執行外部類別方法: " + name, e);
            return null;
        }
    }

    static public ExternalClass plugin(@NotNull Plugin plugin, @NotNull String requiredPlugin, @NotNull String name) {
        return plugin(plugin, List.of(requiredPlugin), name);
    }
    static public ExternalClass plugin(@NotNull Plugin plugin, @NotNull List<String> requiredPlugins, @NotNull String name) {
        return plugin(plugin, requiredPlugins, name, null, null);
    }
    static public ExternalClass plugin(@NotNull Plugin plugin, @NotNull String requiredPlugin, @NotNull String name, List<Class<?>> parameterTypes, List<Object> args) {
        return plugin(plugin, List.of(requiredPlugin), name, parameterTypes, args);
    }
    static public ExternalClass plugin(@NotNull Plugin plugin, @NotNull List<String> requiredPlugins, @NotNull String name, List<Class<?>> parameterTypes, List<Object> args) {
        return new ExternalClass(
                plugin.getServer(),
                requiredPlugins,
                name,
                parameterTypes,
                args
        );
    }
}
