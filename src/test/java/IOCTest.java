import com.test.transfer.factory.BeanFactory;
import com.test.transfer.factory.ProxyFactory;
import com.test.transfer.service.TransferService;
import org.junit.Test;

/**
 * 作业：
 * 学员自定义@Service、@Autowired、@Transactional注解类，完成基于注解的IOC容器（Bean对象创建及依赖注入维护）和声明式事务控制，写到转账工程中，并且可以实现转账成功和转账异常时事务回滚
 * <p>
 * 注意考虑以下情况：
 * <p>
 * 1）注解有无value属性值【@service（value=""） @Repository（value=""）】
 * <p>
 * 2）service层是否实现接口的情况【jdk还是cglib】
 * <p>
 * 题目分析
 * <p>
 * 将对应的xml实现用对象的注解替代即可。
 * <p>
 * 实现思路
 * 1、新建三个注解类，分别标记Service、AutoWired、Transactional
 * 2、在BeanFactory里面读取注解，根据注解进行对应功能的实现，为了方便，使用了自定义ClassUtils工具类
 * <p>
 * 代码讲解
 */

/**
 * @author: terwer
 * @date: 2021/12/16 07:41
 * @description:
 */
public class IOCTest {
    @Test
    public void test() {
        ProxyFactory proxyFactory = (ProxyFactory) BeanFactory.getBean("proxyFactory");
        TransferService transferService = (TransferService) proxyFactory.getJdkProxy(BeanFactory.getBean("transferService"));

        System.out.println(transferService);

//        Object accountDao = BeanFactory.getBean("accountDao");
//        System.out.println(accountDao);
    }
}
