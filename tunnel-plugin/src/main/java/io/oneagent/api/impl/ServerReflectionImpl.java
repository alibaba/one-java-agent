package io.oneagent.api.impl;

import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.reflection.v1alpha.ServerReflectionRequest;
import io.grpc.reflection.v1alpha.ServerReflectionResponse;
import io.grpc.stub.StreamObserver;
import io.oneagent.api.ServerReflectionGrpc.ServerReflectionImplBase;

public class ServerReflectionImpl extends ServerReflectionImplBase {

    private ProtoReflectionService reflectionService;

    public ServerReflectionImpl(ProtoReflectionService protoReflectionService) {
        this.reflectionService = protoReflectionService;
    }

    @Override
    public void serverReflectionInfo(ServerReflectionRequest request,
            StreamObserver<ServerReflectionResponse> responseObserver) {

        StreamObserver<ServerReflectionRequest> requestObserver = reflectionService
                .serverReflectionInfo(responseObserver);
        requestObserver.onNext(request);
        requestObserver.onCompleted();

    }

}
