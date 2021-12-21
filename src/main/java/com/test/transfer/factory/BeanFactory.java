package com.test.transfer.factory;

import com.test.transfer.annotation.AutoWired;
import com.test.transfer.annotation.Component;
import com.test.transfer.annotation.Service;
import com.test.transfer.utils.ClazzUtils;
import com.test.transfer.utils.ConnectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author: terwer
 * @date: 2021/12/12 21:19
 * @description: 工厂类，使用反射 生产对象
 */
public class BeanFactory {
    /**
     * 任务一：读取指定包目录下的class文件，解析注解获取注册的beanId，然后通过反射实例化对象并存储，用map结构存储
     * 任务二：对外提供根据ID获取对象的接口
     */
    private static Map<String, Object> map = new HashMap<>();

    static {
        try {
            List<String> clazzNameList = ClazzUtils.getClazzName("com.test.transfer", true);

            // 1、先创建组件
            for (String clazzName : clazzNameList) {
                // 忽略掉不需要处理的类
                if (clazzName.startsWith("/") || clazzName.contains("Servlet")) {
                    continue;
                }
                // System.out.println(clazzName);


                Class clazz = Class.forName(clazzName);

                //获取类注解信息
                Annotation[] annotations = clazz.getAnnotations();
                for (Annotation annotation : annotations) {
                    String beanId = null;
                    if (clazz.isAnnotationPresent(Component.class)) {
                        Component component = (Component) clazz.getAnnotation(Component.class);
                        beanId = component.value();
                        // 如果没有设置，取类对应接口的类名并且首字母小写
                        if (null == component.value() || "".equals(component.value())) {
                            Class<?>[] interfaces = clazz.getInterfaces();
                            Class<?> inter = null;
                            if (interfaces.length > 0) {
                                inter = interfaces[0];
                            }
                            beanId = firstLowerName(inter.getName());
                        }
                        System.out.println("找到" + clazzName + ".component beanId=>" + beanId);
                    } else if (clazz.isAnnotationPresent(Service.class)) {
                        Service service = (Service) clazz.getAnnotation(Service.class);
                        beanId = service.value();
                        // 如果没有设置，取类对应接口的类名并且首字母小写
                        if (null == service.value() || "".equals(service.value())) {
                            Class<?>[] interfaces = clazz.getInterfaces();
                            Class<?> inter = null;
                            if (interfaces.length > 0) {
                                inter = interfaces[0];
                            }
                            beanId = firstLowerName(inter.getName());
                        }
                        System.out.println("找到" + clazzName + ".service beanId=>" + beanId);
                    }

                    // 没有标记注解的跳过
                    if (null == beanId) {
                        continue;
                    }

                    // 通过反射实例化对象
                    Object o = clazz.newInstance();
                    map.put(beanId, o);
                }
            }

            // 2、设置依赖
            for (String clazzName : clazzNameList) {
                // 忽略掉不需要处理的类
                if (clazzName.startsWith("/") || clazzName.contains("Servlet")) {
                    continue;
                }
                // System.out.println(clazzName);

                Class clazz = Class.forName(clazzName);

                // 获取所有属性，判断属性是否使用了注解
                Field[] allFields = clazz.getDeclaredFields();
                for (int i = 0; i < allFields.length; i++) {
                    Field field = allFields[i];
                    if (field.isAnnotationPresent(AutoWired.class)) {
                        // 这里判断自动注入。需要找到对应的class
                        // 在容器中查找类
                        Map.Entry<String, Object> objectEntry = findObjectByClass(clazz);
                        Object obj = objectEntry.getValue();

                        // 通过属性的类型查找实现类
                        Class<?> fieldClazz = field.getType();
                        Map.Entry<String, Object> fieldEntry = findObjectByInterface(fieldClazz);
                        Object fieldValue = fieldEntry.getValue();

                        field.setAccessible(true);
                        field.set(obj, fieldValue);

                        // 重新赋值回去
                        map.put(objectEntry.getKey(), obj);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //首字母小写
    public static String firstLowerName(String name) {
        // 去掉包名
        int dotIndex = name.lastIndexOf(".");
        name = name.substring(dotIndex + 1);

        // 首字母小写
        name = name.substring(0, 1).toLowerCase() + name.substring(1);//UpperCase大写
        return name;
    }

    /**
     * 从当前容器中查找指定class的实例
     *
     * @param clazz
     * @return
     */
    private static Map.Entry<String, Object> findObjectByClass(Class<?> clazz) {
        List<Map.Entry<String, Object>> objects = new ArrayList<>();

        Set<Map.Entry<String, Object>> entrySet = map.entrySet();
        for (Map.Entry<String, Object> entry : entrySet) {
            if (entry.getValue().getClass().getName().equals(clazz.getName())) {
                objects.add(entry);
            }
        }

        if (objects.size() > 0) {
            return objects.get(0);
        }
        return null;
    }

    /**
     * 获取当前接口的实现类
     *
     * @param clazz 接口
     * @return
     */
    private static Map.Entry<String, Object> findObjectByInterface(Class<?> clazz) {
        List<Map.Entry<String, Object>> objects = new ArrayList<>();

        Set<Map.Entry<String, Object>> entrySet = map.entrySet();
        for (Map.Entry<String, Object> entry : entrySet) {
            if (clazz.isAssignableFrom(entry.getValue().getClass())) {
                objects.add(entry);
            }
        }

        if (objects.size() > 0) {
            return objects.get(0);
        }
        return null;
    }

    /**
     * 提供根据ID获取对象的接口
     *
     * @param beanId
     * @return
     */
    public static Object getBean(String beanId) {
        return map.get(beanId);
    }
}
