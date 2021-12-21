package com.test.transfer.factory;

import com.alibaba.druid.sql.dialect.odps.ast.OdpsObject;
import com.test.transfer.annotation.AutoWired;
import com.test.transfer.annotation.Component;
import com.test.transfer.annotation.Transactional;
import com.test.transfer.utils.TransactionManager;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author: terwer
 * @date: 2021/12/14 22:58
 * @description: 代理对象工厂，主要用于生产对象
 */
@Component("proxyFactory")
public class ProxyFactory {

    @AutoWired
    private TransactionManager transactionManager;

    /**
     * 根据有没有实现接口来判断使用那种代理
     * @param obj
     * @return
     */
    public Object getProxy(Object obj) {
        // 实现了接口，使用jdk动态代理，否则使用cglib
        if (obj.getClass().getInterfaces().length > 0) {
            return getJdkProxy(obj);
        } else {
            return getCglibProxy(obj);
        }
    }

    /**
     * Jdk动态代理
     *
     * @param obj 委托对象
     * @return 代理对象
     */
    private Object getJdkProxy(Object obj) {

        // 获取代理对象
        return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(),
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Object result = null;

                        boolean useTransaction = false;
                        //获取所以方法注解信息 ps:这里需要使用 isAnnotationPresent 判断方法上是否使用了注解
                        Method[] allMethods = obj.getClass().getDeclaredMethods();
                        for (int i = 0; i < allMethods.length; i++) {
                            if (allMethods[i].isAnnotationPresent(Transactional.class)) {
                                useTransaction = true;
                                System.out.println("找到事务注解，此方法调用将开启事务");
                            }
                        }

                        // 判断方法的事务是否开启
                        if (useTransaction) {
                            try {
                                // 开启事务(关闭事务的自动提交)
                                transactionManager.beginTransaction();

                                result = method.invoke(obj, args);

                                // 提交事务

                                transactionManager.commit();
                            } catch (Exception e) {
                                e.printStackTrace();
                                // 回滚事务
                                transactionManager.rollback();

                                // 抛出异常便于上层servlet捕获
                                throw e;

                            }
                        } else {
                            // 不使用事务
                            System.out.println("未开启事务");
                            result = method.invoke(obj, args);
                        }


                        return result;
                    }
                });

    }


    /**
     * 使用cglib动态代理生成代理对象
     *
     * @param obj 委托对象
     * @return
     */
    private Object getCglibProxy(Object obj) {
        return Enhancer.create(obj.getClass(), new MethodInterceptor() {
            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                Object result = null;
                boolean useTransaction = false;
                //获取所以方法注解信息 ps:这里需要使用 isAnnotationPresent 判断方法上是否使用了注解
                Method[] allMethods = obj.getClass().getDeclaredMethods();
                for (int i = 0; i < allMethods.length; i++) {
                    if (allMethods[i].isAnnotationPresent(Transactional.class)) {
                        useTransaction = true;
                        System.out.println("找到事务注解，此方法调用将开启事务");
                    }
                }
                if (useTransaction) {
                    try {
                        // 开启事务(关闭事务的自动提交)
                        transactionManager.beginTransaction();

                        result = method.invoke(obj, objects);

                        // 提交事务

                        transactionManager.commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                        // 回滚事务
                        transactionManager.rollback();

                        // 抛出异常便于上层servlet捕获
                        throw e;

                    }
                } else {
                    result = method.invoke(obj, objects);
                }

                return result;
            }
        });
    }
}
