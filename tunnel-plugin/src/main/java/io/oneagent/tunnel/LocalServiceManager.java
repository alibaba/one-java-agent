package io.oneagent.tunnel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import io.grpc.BindableService;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 
 * @author hengyunabc 2023-02-16
 *
 */
public class LocalServiceManager {
    private LocalAddress localAddress = new LocalAddress("one-agent-local-address");

    private ServiceServer memoryServer;
    private ServiceServer listenServer;

    public LocalServiceManager(String listenAddress, List<BindableService> services) {
        if (listenAddress != null) {
            String[] parts = listenAddress.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
            listenServer = new ServiceServer(NioServerSocketChannel.class, new NioEventLoopGroup(), inetSocketAddress, services);
        }
        memoryServer = new ServiceServer(LocalServerChannel.class, new DefaultEventLoop(), localAddress, services);
    }

    public void start() throws IOException {

        memoryServer.start();

        if (listenServer != null) {
            listenServer.start();
        }

    }

    public void addService(BindableService bindableService) {
        memoryServer.addService(bindableService);
        if (listenServer != null) {
            listenServer.addService(bindableService);
        }
    }

    public void stop() {
        memoryServer.stop();
        if (listenServer != null) {
            listenServer.stop();
        }
    }

}
