package org.apache.dubbo.rpc.test;

import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

import com.alibaba.bytekit.agent.inst.Instrument;
import com.alibaba.bytekit.agent.inst.InstrumentApi;

/**
 * @see org.apache.dubbo.rpc.Invoker
 * @author hengyunabc 2020-11-26
 *
 */
@Instrument(Interface = "org.apache.dubbo.rpc.Invoker")
public abstract class Invoker {

    /**
     * invoke.
     *
     * @param invocation
     * @return result
     * @throws RpcException
     */
    public Result invoke(Invocation invocation) throws RpcException {
        System.err.println("invoker class: " + this.getClass().getName());
        Result result = InstrumentApi.invokeOrigin();
        System.err.println("result:" + result + ", invoker class: " + this.getClass().getName());
        return result;
    }

}
