package ing.prometheusreengineer.minecraft.util.java;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Child first, because the core adds its own jar to the game's class path so patches can use the lib. With the normal
 * parent-first order, classes asked for after that would come from the game's copy and mix with the ones already
 * loaded from here, which breaks package-private access between them.
 */
@NotNullByDefault
public final class ChildFirstClassLoader extends URLClassLoader {
    static {
        ClassLoader.registerAsParallelCapable();
    }

    public ChildFirstClassLoader(Path jar, ClassLoader parent) throws MalformedURLException {
        super(new URL[]{jar.toUri().toURL()}, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> type = findLoadedClass(name);
            if (type == null && !name.startsWith("java.")) {
                type = LambdaUtil.runCheckedOr(() -> findClass(name), null);
            }
            if (type == null) type = super.loadClass(name, false);
            if (resolve) resolveClass(type);
            return type;
        }
    }

    @Override
    public @Nullable URL getResource(String name) {
        return Optional.ofNullable(findResource(name)).orElse(super.getResource(name));
    }
}
