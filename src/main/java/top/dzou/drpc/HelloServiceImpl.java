package top.dzou.drpc;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public class HelloServiceImpl implements HelloService {

    public String sayHi(String name) {
        return "Hi, " + name;
    }

}
