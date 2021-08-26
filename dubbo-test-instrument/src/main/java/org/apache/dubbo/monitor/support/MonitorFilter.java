package org.apache.dubbo.monitor.support;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;

import com.alibaba.bytekit.agent.inst.Instrument;
import com.alibaba.bytekit.agent.inst.InstrumentApi;
import com.test.dubbo.RpcUtils;

/**
 * 
 * @see org.apache.dubbo.monitor.support.MonitorFilter
 * @author hengyunabc
 *
 */
@Instrument(Class = "org.apache.dubbo.monitor.support.MonitorFilter")
public abstract class MonitorFilter {

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException  {
        
        try {
            RpcUtils.print(invoker);
            RpcContext rpcContext = RpcContext.getContext();
            boolean isConsumer = rpcContext.isConsumerSide();
            URL requestURL = invoker.getUrl();
            String host = requestURL.getHost();
            int port = requestURL.getPort();
            
            System.err.println("isConsumer: " + isConsumer);
            System.err.println("requestURL: " + requestURL);
            System.err.println("host: " + host);
            System.err.println("port: " + port);
            
            Result result = InstrumentApi.invokeOrigin();
            
            System.err.println("result: " + result);
            
            if (result != null && result.getException() != null) {
                System.err.println("exception: " + result.getException());
            }
            
            return result;
        } catch (RpcException e) {
            throw e;
        }

    }
}
