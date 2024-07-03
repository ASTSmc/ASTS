package tw.asts.mc.asts.util;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class PluginPermission {
    public static final Permission admin() {
        Permission adminPermission = new Permission("asts.admin", "伺服器管理員");
        adminPermission.setDefault(PermissionDefault.OP);
        return adminPermission;
    }
}
