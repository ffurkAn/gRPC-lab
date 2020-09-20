package com.github.simplesteph.grpc.greeting.client;

import com.proto.sum.*;
import io.grpc.*;

import javax.net.ssl.SSLException;

public class gRPCClient {

    public static void main(String[] args) throws SSLException {
        System.out.println("Hello I'm a gRPC client");

        gRPCClient main = new gRPCClient();
        main.run();
    }

    private void run() throws SSLException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();


        doUnaryCall(channel);

        System.out.println("Shutting down channel");
        channel.shutdown();

    }

    private void doUnaryCall(ManagedChannel channel) {
        // created a greet service client (blocking - synchronous)
        SumServiceGrpc.SumServiceBlockingStub sumServiceBlockingStub = SumServiceGrpc.newBlockingStub(channel);

        // Unary
        // created a protocol buffer sum message
        Sum sum = Sum.newBuilder()
                .setFirstNumber(5)
                .setSecondNumber(10)
                .build();

        // do the same for a SumRequest
        SumRequest sumRequest = SumRequest.newBuilder()
                .setSum(sum)
                .build();


        // call the RPC and get back a SumResponse (protocol buffers)
        SumResponse sumResponse = sumServiceBlockingStub.sum(sumRequest);

        System.out.println("Sum is: " + sumResponse);

    }

    private void doServerStreamingCall(ManagedChannel channel) {


    }

    private void doClientStreamingCall(ManagedChannel channel) {

    }

    private void doBiDiStreamingCall(ManagedChannel channel) {

    }

    private void doUnaryCallWithDeadline(ManagedChannel channel) {
    }

}
