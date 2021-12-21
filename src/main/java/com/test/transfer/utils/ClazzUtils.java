package com.test.transfer.utils;

import com.test.transfer.annotation.AutoWired;
import com.test.transfer.annotation.Component;
import com.test.transfer.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author: terwer
 * @date: 2021/12/16 08:10
 * @description: 读取指定包下面的class信息的工具类
 */
public class ClazzUtils {
    private static final String CLASS_SUFFIX = ".class";
    private static final String CLASS_FILE_PREFIX = File.separator + "classes" + File.separator;
    private static final String PACKAGE_SEPARATOR = ".";

    /**
     * 查找包下的所有类的名字
     *
     * @param packageName
     * @param showChildPackageFlag 是否需要显示子包内容
     * @return List集合，内容为类的全名
     */
    public static List<String> getClazzName(String packageName, boolean showChildPackageFlag) {
        List<String> result = new ArrayList<>();
        String suffixPath = packageName.replaceAll("\\.", "/");
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> urls = loader.getResources(suffixPath);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if (url != null) {
                    String protocol = url.getProtocol();
                    if ("file".equals(protocol)) {
                        String path = url.getPath();
                        System.out.println("当前扫描的包为:"+path);
                        result.addAll(getAllClassNameByFile(new File(path), showChildPackageFlag));
                    } else if ("jar".equals(protocol)) {
                        JarFile jarFile = null;
                        try {
                            jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (jarFile != null) {
                            result.addAll(getAllClassNameByJar(jarFile, packageName, showChildPackageFlag));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 递归获取所有class文件的名字
     *
     * @param file
     * @param flag 是否需要迭代遍历
     * @return List
     */
    private static List<String> getAllClassNameByFile(File file, boolean flag) {
        List<String> result = new ArrayList<>();
        if (!file.exists()) {
            return result;
        }
        if (file.isFile()) {
            String path = file.getPath();
            // 注意：这里替换文件分割符要用replace。因为replaceAll里面的参数是正则表达式,而windows环境中File.separator="\\"的,因此会有问题
            if (path.endsWith(CLASS_SUFFIX)) {
                path = path.replace(CLASS_SUFFIX, "");
                // 从"/classes/"后面开始截取
                String clazzName = path.substring(path.indexOf(CLASS_FILE_PREFIX) + CLASS_FILE_PREFIX.length())
                        .replace(File.separator, PACKAGE_SEPARATOR);
                if (-1 == clazzName.indexOf("$")) {
                    result.add(clazzName);
                }
            }
            return result;

        } else {
            File[] listFiles = file.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File f : listFiles) {
                    if (flag) {
                        result.addAll(getAllClassNameByFile(f, flag));
                    } else {
                        if (f.isFile()) {
                            String path = f.getPath();
                            if (path.endsWith(CLASS_SUFFIX)) {
                                path = path.replace(CLASS_SUFFIX, "");
                                // 从"/classes/"后面开始截取
                                String clazzName = path.substring(path.indexOf(CLASS_FILE_PREFIX) + CLASS_FILE_PREFIX.length())
                                        .replace(File.separator, PACKAGE_SEPARATOR);
                                if (-1 == clazzName.indexOf("$")) {
                                    result.add(clazzName);
                                }
                            }
                        }
                    }
                }
            }
            return result;
        }
    }

    /**
     * 递归获取jar所有class文件的名字
     *
     * @param jarFile
     * @param packageName 包名
     * @param flag        是否需要迭代遍历
     * @return List
     */
    private static List<String> getAllClassNameByJar(JarFile jarFile, String packageName, boolean flag) {
        List<String> result = new ArrayList<>();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            String name = jarEntry.getName();
            // 判断是不是class文件
            if (name.endsWith(CLASS_SUFFIX)) {
                name = name.replace(CLASS_SUFFIX, "").replace("/", ".");
                if (flag) {
                    // 如果要子包的文件,那么就只要开头相同且不是内部类就ok
                    if (name.startsWith(packageName) && -1 == name.indexOf("$")) {
                        result.add(name);
                    }
                } else {
                    // 如果不要子包的文件,那么就必须保证最后一个"."之前的字符串和包名一样且不是内部类
                    if (packageName.equals(name.substring(0, name.lastIndexOf("."))) && -1 == name.indexOf("$")) {
                        result.add(name);
                    }
                }
            }
        }
        return result;
    }

    private static List<Class<?>> getAllClassFromPackage(String packageName) throws IOException, ClassNotFoundException{
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace(".", "/");
        Enumeration<URL> enumeration = classLoader.getResources(path);
        List<String> classNames = getClazzName(packageName, true);

        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        for (int i = 0; i < classNames.size(); i++) {
            classes.add(Class.forName(classNames.get(i)));
        }

        return classes;
    }


    /*
     * 获取指定接口的所有实现实例
     */
    public static List<Object> getAllObjectByInterface(Class<?> c)
            throws InstantiationException, IllegalAccessException {
        List<Object> list = new ArrayList<Object>();
        List<Class<?>> classes = getAllClassByInterface(c);
        for (int i = 0; i < classes.size(); i++) {
            list.add(classes.get(i).newInstance());
        }
        return list;
    }

    /*
     * 获取指定接口的实例的Class对象
     */
    public static List<Class<?>> getAllClassByInterface(Class<?> c) {
        // 如果传入的参数不是接口直接结束
        if (!c.isInterface()) {
            return null;
        }

        // 获取当前包名
        String packageName = c.getPackage().getName();
        List<Class<?>> allClass = null;
        try {
            allClass = getAllClassFromPackage(packageName);
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        ArrayList<Class<?>> list = new ArrayList<Class<?>>();
        for (int i = 0; i < allClass.size(); i++) {
            if (c.isAssignableFrom(allClass.get(i))) {
                if (!c.equals(allClass.get(i))) {
                    list.add(allClass.get(i));
                }
            }
        }

        return list;
    }

    public static void main(String[] args) throws Exception {
        List<String> list = ClazzUtils.getClazzName("com.test.transfer", true);
//        for (String clazzName : list) {
//            if (clazzName.startsWith("/") || clazzName.contains("Servlet")) {
//                continue;
//            }
//            System.out.println(clazzName);

        String clazzName = "com.test.transfer.dao.impl.JdbcAccountDaoImpl";
        Class clazz = Class.forName(clazzName);

        //获取类注解信息
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation annotation : annotations) {
            if (clazz.isAnnotationPresent(Component.class)) {
                Component component = (Component) clazz.getAnnotation(Component.class);
                String beanId = component.value();
                System.out.println("beanId=>" + beanId);
            }
        }

        //获取所以方法注解信息 ps:这里需要使用 isAnnotationPresent 判断方法上是否使用了注解
        Method[] allMethods = clazz.getDeclaredMethods();
        for (int i = 0; i < allMethods.length; i++) {
            if (allMethods[i].isAnnotationPresent(Transactional.class)) {
                Transactional transactional = allMethods[i].getAnnotation(Transactional.class);
                String beanId = transactional.value();
                if(null == beanId || "".equals(beanId)){
                    // 提供一个默认的
                    beanId = "transactionManager";
                }
                System.out.println("transationManager=>" + beanId);
            }
        }

        // 获取所有属性，判断属性是否使用了注解
        Field[] allFields = clazz.getDeclaredFields();
        for (int i = 0; i < allFields.length; i++) {
            if (allFields[i].isAnnotationPresent(AutoWired.class)) {
                // 这里判断自动注入。需要找到对应的class
            }
        }
    }
}