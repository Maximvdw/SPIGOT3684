package be.maximvdw.spigot3684;


import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class ReflectionUtil {
    private static volatile Map<Class<?>, Class<?>> CORRESPONDING_TYPES = new HashMap();
    public static Class<?> obcPlayer = getOBCClass("entity.CraftPlayer");
    public static Method methodPlayerGetHandle;

    public ReflectionUtil() {
    }

    private static Class<?> getPrimitiveType(Class<?> clazz) {
        return CORRESPONDING_TYPES.containsKey(clazz)?(Class)CORRESPONDING_TYPES.get(clazz):clazz;
    }

    private static Class<?>[] toPrimitiveTypeArray(Object[] objects) {
        int a = objects != null?objects.length:0;
        Class[] types = new Class[a];

        for(int i = 0; i < a; ++i) {
            types[i] = getPrimitiveType(objects[i].getClass());
        }

        return types;
    }

    private static Class<?>[] toPrimitiveTypeArray(Class<?>[] classes) {
        int a = classes != null?classes.length:0;
        Class[] types = new Class[a];

        for(int i = 0; i < a; ++i) {
            types[i] = getPrimitiveType(classes[i]);
        }

        return types;
    }

    private static boolean equalsTypeArray(Class<?>[] a, Class<?>[] o) {
        if(a.length != o.length) {
            return false;
        } else {
            for(int i = 0; i < a.length; ++i) {
                if(!a[i].equals(o[i]) && !a[i].isAssignableFrom(o[i])) {
                    return false;
                }
            }

            return true;
        }
    }

    public static Class<?> getClass(String name, String namespace) throws Exception {
        return Class.forName(namespace + "." + name);
    }

    public static Object getHandle(Object obj) {
        try {
            return getMethod("getHandle", obj.getClass(), new Class[0]).invoke(obj, new Object[0]);
        } catch (Exception var2) {
            var2.printStackTrace();
            return null;
        }
    }

    public static Object getHandle(Player player) {
        try {
            return methodPlayerGetHandle.invoke(player, new Object[0]);
        } catch (Exception var2) {
            var2.printStackTrace();
            return null;
        }
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Class... paramTypes) {
        Class[] t = toPrimitiveTypeArray(paramTypes);
        Constructor[] var3 = clazz.getConstructors();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Constructor c = var3[var5];
            Class[] types = toPrimitiveTypeArray(c.getParameterTypes());
            if(equalsTypeArray(types, t)) {
                return c;
            }
        }

        return null;
    }

    public static Object newInstance(Class<?> clazz, Object... args) throws Exception {
        return getConstructor(clazz, toPrimitiveTypeArray(args)).newInstance(args);
    }

    public static Method getMethod(String name, Class<?> clazz, Class... paramTypes) {
        Class[] t = toPrimitiveTypeArray(paramTypes);
        Method[] var4 = clazz.getMethods();
        int var5 = var4.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            Method m = var4[var6];
            Class[] types = toPrimitiveTypeArray(m.getParameterTypes());
            if(m.getName().equals(name) && equalsTypeArray(types, t)) {
                return m;
            }
        }

        return null;
    }

    public static Object invokeMethod(String name, Class<?> clazz, Object obj, Object... args) throws Exception {
        return getMethod(name, clazz, toPrimitiveTypeArray(args)).invoke(obj, args);
    }

    public static Field getField(String name, Class<?> clazz) throws Exception {
        return clazz.getDeclaredField(name);
    }

    public static Object getValue(String name, Object obj) throws Exception {
        Field f = getField(name, obj.getClass());
        if(!f.isAccessible()) {
            f.setAccessible(true);
        }

        return f.get(obj);
    }

    public static Object getValueFromClass(String name, Object obj, Class<?> clazz) throws Exception {
        Field f = getField(name, clazz);
        if(!f.isAccessible()) {
            f.setAccessible(true);
        }

        return f.get(obj);
    }

    public static Object getValue(String name, Class<?> clazz) throws Exception {
        Field f = getField(name, clazz);
        if(!f.isAccessible()) {
            f.setAccessible(true);
        }

        return f.get(clazz);
    }

    public static void setValue(Object obj, ReflectionUtil.FieldEntry entry) throws Exception {
        Field f = getField(entry.getKey(), obj.getClass());
        if(!f.isAccessible()) {
            f.setAccessible(true);
        }

        f.set(obj, entry.getValue());
    }

    public static void setValue(String name, Object value, Object obj) throws Exception {
        Field f = getField(name, obj.getClass());
        if(!f.isAccessible()) {
            f.setAccessible(true);
        }

        f.set(obj, value);
    }

    public static void setFinalValue(String name, Object value, Object obj) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        if(!f.isAccessible()) {
            f.setAccessible(true);
        }

        f.set(obj, value);
    }

    public static void setValues(Object obj, ReflectionUtil.FieldEntry... entrys) throws Exception {
        ReflectionUtil.FieldEntry[] var2 = entrys;
        int var3 = entrys.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            ReflectionUtil.FieldEntry f = var2[var4];
            setValue(obj, f);
        }

    }

    public static String getVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String version = name.substring(name.lastIndexOf(46) + 1) + ".";
        return version;
    }

    public static Class<?> getNMSClass(String className) {
        String fullName = "net.minecraft.server." + getVersion() + className;
        Class clazz = null;

        try {
            clazz = Class.forName(fullName);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return clazz;
    }

    public static Class<?> getOBCClass(String className) {
        String fullName = "org.bukkit.craftbukkit." + getVersion() + className;
        Class clazz = null;

        try {
            clazz = Class.forName(fullName);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return clazz;
    }

    public static Class<?> getNMSClassWithException(String className) throws Exception {
        String fullName = "net.minecraft.server." + getVersion() + className;
        Class clazz = Class.forName(fullName);
        return clazz;
    }

    public static Field getField(Class<?> clazz, String name) {
        try {
            Field e = clazz.getDeclaredField(name);
            e.setAccessible(true);
            return e;
        } catch (Exception var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public static Method getMethod(Class<?> clazz, String name, Class... args) {
        Method[] var3 = clazz.getMethods();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Method m = var3[var5];
            if(m.getName().equals(name) && (args.length == 0 || ClassListEqual(args, m.getParameterTypes()))) {
                m.setAccessible(true);
                return m;
            }
        }

        return null;
    }

    public static Field setAccessible(Field f) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        f.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(f, f.getModifiers() & -17);
        return f;
    }

    public static Method setAccessible(Method m) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        m.setAccessible(true);
        return m;
    }

    public static boolean ClassListEqual(Class<?>[] l1, Class<?>[] l2) {
        boolean equal = true;
        if(l1.length != l2.length) {
            return false;
        } else {
            for(int i = 0; i < l1.length; ++i) {
                if(l1[i] != l2[i]) {
                    equal = false;
                    break;
                }
            }

            return equal;
        }
    }

    static {
        methodPlayerGetHandle = getMethod("getHandle", obcPlayer, new Class[0]);
        CORRESPONDING_TYPES.put(Byte.class, Byte.TYPE);
        CORRESPONDING_TYPES.put(Short.class, Short.TYPE);
        CORRESPONDING_TYPES.put(Integer.class, Integer.TYPE);
        CORRESPONDING_TYPES.put(Long.class, Long.TYPE);
        CORRESPONDING_TYPES.put(Character.class, Character.TYPE);
        CORRESPONDING_TYPES.put(Float.class, Float.TYPE);
        CORRESPONDING_TYPES.put(Double.class, Double.TYPE);
        CORRESPONDING_TYPES.put(Boolean.class, Boolean.TYPE);
    }

    public static class FieldEntry {
        String key;
        Object value;

        public FieldEntry(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return this.key;
        }

        public Object getValue() {
            return this.value;
        }
    }
}