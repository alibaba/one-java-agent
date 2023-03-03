package com.alibaba.oneagent.tunnel;

import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.oneagent.api.OneAgentService.Properties;
import com.alibaba.oneagent.api.OneAgentService.StringKey;
import com.alibaba.oneagent.api.OneAgentService.StringValue;
import com.alibaba.oneagent.api.SystemPropertyGrpc.SystemPropertyImplBase;
import com.google.protobuf.Empty;

import io.grpc.stub.StreamObserver;

public class SystemPropertyImpl extends SystemPropertyImplBase {

    @Override
    public void get(Empty request, StreamObserver<Properties> responseObserver) {
        Map<String, String> map = System.getProperties().entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toString(), entry -> entry.getValue().toString()));

        responseObserver.onNext(Properties.newBuilder().putAllProperties(map).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getByKey(StringKey request, StreamObserver<StringValue> responseObserver) {
        String value = (String) System.getProperties().get(request.getKey());

        responseObserver.onNext(StringValue.newBuilder().setValue(value).build());
        responseObserver.onCompleted();
    }

    @Override
    public void update(Properties request, StreamObserver<Properties> responseObserver) {
        request.getPropertiesMap().forEach(System::setProperty);

        Map<String, String> map = System.getProperties().entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toString(), entry -> entry.getValue().toString()));

        responseObserver.onNext(Properties.newBuilder().putAllProperties(map).build());
        responseObserver.onCompleted();
    }

}
