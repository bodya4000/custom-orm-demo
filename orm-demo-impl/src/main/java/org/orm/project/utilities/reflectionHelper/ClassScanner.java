package org.orm.project.utilities.reflectionHelper;

import org.orm.project.core.annotations.Table;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;

public class ClassScanner {

    public static ArrayList<Class<?>> getEntityClasses() throws IOException, ClassNotFoundException {
        String packageName = "org.orm.project";

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace(".", "/");
        Enumeration<URL> resources = classLoader.getResources(path);


        ArrayList<Class<?>> classes = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if(resource.getProtocol().equals("file")){
                File packageDir = new File(resource.getFile());
                classes.addAll(findClasses(packageName, packageDir));
            }
        }
        return classes;
    }

    private static Collection<Class<?>> findClasses(String packageName, File directory) throws ClassNotFoundException {
        var classes = new ArrayList<Class<?>>();
        if(!directory.exists()){
            return classes;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }

        for (var file: files) {
            if (file.isDirectory()){
                classes.addAll(findClasses(packageName+"." + file.getName(), file));
            }
            if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                var type = Class.forName(className);
                if(ifClassIsEntity(type)){
                    classes.add(type);
                }
            }
        }

        return classes;
    }


    private static boolean ifClassIsEntity(Class<?> type) {
        return Arrays.stream(type.getAnnotations()).anyMatch(annotation -> annotation.annotationType() == Table.class);
    }
}
