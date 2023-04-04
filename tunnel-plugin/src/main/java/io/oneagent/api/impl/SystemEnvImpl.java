package io.oneagent.api.impl;

import com.google.protobuf.Empty;

import io.grpc.stub.StreamObserver;
import io.oneagent.api.OneAgentService.Envs;
import io.oneagent.api.OneAgentService.StringKey;
import io.oneagent.api.OneAgentService.StringValue;
import io.oneagent.api.SystemEnvGrpc.SystemEnvImplBase;

public class SystemEnvImpl extends SystemEnvImplBase {

    @Override
    public void get(Empty request, StreamObserver<Envs> responseObserver) {
        responseObserver.onNext(Envs.newBuilder().putAllEnv(System.getenv()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getByKey(StringKey request, StreamObserver<StringValue> responseObserver) {
        String value = System.getenv().get(request.getKey());
        responseObserver.onNext(StringValue.newBuilder().setValue(value).build());
        responseObserver.onCompleted();
    }

}
