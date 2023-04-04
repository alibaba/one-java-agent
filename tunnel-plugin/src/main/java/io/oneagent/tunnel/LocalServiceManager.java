package io.oneagent.tunnel;

import java.io.IOException;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.util.MutableHandlerRegistry;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalServerChannel;
import io.oneagent.api.impl.SystemEnvImpl;
import io.oneagent.api.impl.SystemPropertyImpl;

/**
 * 
 * @author hengyunabc 2023-02-16
 *
 */
public class LocalServiceManager {
    private DefaultEventLoopGroup eventLoopGroup = new DefaultEventLoopGroup();
    private MutableHandlerRegistry registry = new MutableHandlerRegistry();
    private LocalAddress localAddress = new LocalAddress("one-agent-local-address");

    private Server server;

    public void start() throws IOException {
        Server server = NettyServerBuilder.forAddress(localAddress).channelType(LocalServerChannel.class)
                .workerEventLoopGroup(eventLoopGroup).bossEventLoopGroup(eventLoopGroup)
                .fallbackHandlerRegistry(registry).addService(new SystemPropertyImpl()).addService(new SystemEnvImpl()).build();

        server.start();
    }

    public void addService(BindableService bindableService) {
        registry.addService(bindableService);
    }

    public void stop() {
        server.shutdown();
    }

}
