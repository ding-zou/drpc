package top.dzou.drpc;


/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public interface HelloService {

    String sayHi(String name);

    Hello.HelloRes sayHi(Hello.HelloArg name);
}
