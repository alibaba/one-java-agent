package io.oneagent.tunnel;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.List;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.util.MutableHandlerRegistry;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.oneagent.api.impl.ServerReflectionImpl;
import io.oneagent.api.impl.SystemEnvImpl;
import io.oneagent.api.impl.SystemPropertyImpl;

/**
 * 
 * @author hengyunabc 2023-02-16
 *
 */
public class ServiceServer {
    private static final ProtoReflectionService reflectionService = (ProtoReflectionService) ProtoReflectionService
            .newInstance();
    private EventLoopGroup eventLoopGroup;
    private MutableHandlerRegistry registry = new MutableHandlerRegistry();
    private SocketAddress listenAddress;

    private Class<? extends ServerChannel> channelType;

    private Server server;

    private List<BindableService> services;

    public ServiceServer(Class<? extends ServerChannel> channelType, EventLoopGroup eventLoopGroup,
            SocketAddress listenAddress, List<BindableService> services) {
        this.channelType = channelType;
        this.eventLoopGroup = eventLoopGroup;
        this.listenAddress = listenAddress;
        this.services = services;
    }

    public void start() throws IOException {
        startServer();
    }

    private void startServer() throws IOException {

        NettyServerBuilder serverBuilder = NettyServerBuilder.forAddress(listenAddress).channelType(channelType)
                .workerEventLoopGroup(eventLoopGroup).bossEventLoopGroup(eventLoopGroup)
                .fallbackHandlerRegistry(registry).addService(reflectionService)
                .addService(new ServerReflectionImpl(reflectionService)).addService(new SystemPropertyImpl())
                .addService(new SystemEnvImpl());

        if (services != null) {
            for (BindableService service : services) {
                serverBuilder.addService(service);
            }
        }
        Server server = serverBuilder.build();

        server.start();
    }

    public void addService(BindableService bindableService) {
        registry.addService(bindableService);
    }

    public void stop() {
        server.shutdown();
    }

}
