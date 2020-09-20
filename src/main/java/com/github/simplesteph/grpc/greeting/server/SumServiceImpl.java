package com.github.simplesteph.grpc.greeting.server;

import com.proto.sum.Sum;
import com.proto.sum.SumRequest;
import com.proto.sum.SumResponse;
import com.proto.sum.SumServiceGrpc;
import io.grpc.stub.StreamObserver;

public class SumServiceImpl extends SumServiceGrpc.SumServiceImplBase {

    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {

        Sum sum = request.getSum();
        int sumResult = sum.getFirstNumber() + sum.getSecondNumber();

        SumResponse sumResponse = SumResponse.newBuilder()
                .setSum(sumResult)
                .build();

        responseObserver.onNext(sumResponse);

        responseObserver.onCompleted();
    }
}
