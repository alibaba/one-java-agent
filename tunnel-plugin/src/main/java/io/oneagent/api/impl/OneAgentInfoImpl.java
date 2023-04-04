package io.oneagent.api.impl;

import java.util.Map;
import java.util.stream.Collectors;

import com.google.protobuf.Empty;

import io.grpc.stub.StreamObserver;
import io.oneagent.api.OneAgentInfoGrpc.OneAgentInfoImplBase;
import io.oneagent.api.OneAgentService.Properties;
import io.oneagent.api.OneAgentService.StringValue;
import io.oneagent.service.OneAgentInfoService;

/**
 * 
 * @author hengyunabc 2023-04-04
 *
 */
public class OneAgentInfoImpl extends OneAgentInfoImplBase {
    private OneAgentInfoService oneAgentInfoService;

    public OneAgentInfoImpl(OneAgentInfoService oneAgentInfoService) {
        this.oneAgentInfoService = oneAgentInfoService;
    }

    @Override
    public void appName(Empty request, StreamObserver<StringValue> responseObserver) {
        String appName = oneAgentInfoService.appName();

        responseObserver.onNext(StringValue.newBuilder().setValue(appName).build());
        responseObserver.onCompleted();
    }

    @Override
    public void version(Empty request, StreamObserver<StringValue> responseObserver) {
        String version = oneAgentInfoService.version();
        responseObserver.onNext(StringValue.newBuilder().setValue(version).build());
        responseObserver.onCompleted();
    }

    @Override
    public void config(Empty request, StreamObserver<Properties> responseObserver) {
        Map<String, String> map = oneAgentInfoService.config().entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toString(), entry -> entry.getValue().toString()));

        responseObserver.onNext(Properties.newBuilder().putAllProperties(map).build());
        responseObserver.onCompleted();

        super.config(request, responseObserver);
    }

}
