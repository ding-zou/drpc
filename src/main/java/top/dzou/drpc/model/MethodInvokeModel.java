package top.dzou.drpc.model;

import java.io.Serializable;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */

/**
 * 调用方法信息
 */
public class MethodInvokeModel implements Serializable {
    private String interfaceName;
    private String methodName;
    private Class[] paramsType;
    private Object[] params;

    public MethodInvokeModel(String interfaceName, String methodName, Class[] paramsType, Object[] params) {
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.paramsType = paramsType;
        this.params = params;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class[] getParamsType() {
        return paramsType;
    }

    public void setParamsType(Class[] paramsType) {
        this.paramsType = paramsType;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }
}
