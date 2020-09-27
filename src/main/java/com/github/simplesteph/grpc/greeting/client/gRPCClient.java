package com.github.simplesteph.grpc.greeting.client;

import com.proto.sum.*;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import javax.net.ssl.SSLException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
        doUnaryCallWithDeadline(channel);
        doServerStreamingCall(channel);
        doClientStreamingCall(channel);
        doBiDiStreamingCall(channel);

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

        SumServiceGrpc.SumServiceBlockingStub sumServiceBlockingStub = SumServiceGrpc.newBlockingStub(channel);

        PrimeNumber primeNumber = PrimeNumber.newBuilder()
                .setNumber(120)
                .build();

        PrimeNumberRequest primeNumberRequest = PrimeNumberRequest.newBuilder()
                .setPrimeNumber(primeNumber)
                .build();

        sumServiceBlockingStub.getPrimeNumberDecomposition(primeNumberRequest)
                .forEachRemaining(primeNumberResponseStream -> {
                    System.out.println(primeNumberResponseStream.getResult());
                });

    }

    private void doClientStreamingCall(ManagedChannel channel) {

        SumServiceGrpc.SumServiceStub sumServiceStub = SumServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<AvgNumberRequestStream> requestStreamStreamObserver =
                sumServiceStub.getAvgNumber(new StreamObserver<AvgNumberResponse>() {
                    @Override
                    public void onNext(AvgNumberResponse value) {

                        // we get a response from the server
                        // since this is client streming, onnext will called once

                        System.out.println("Average is: " + value.getResult());
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {

                        // onCompleted called right after onNext
                        // server done sending message
                        System.out.println("completed");
                        latch.countDown();
                    }
                });

        System.out.println("sending message 1");
        requestStreamStreamObserver.onNext(AvgNumberRequestStream.newBuilder()
                .setNumber(1).build());

        System.out.println("sending message 2");
        requestStreamStreamObserver.onNext(AvgNumberRequestStream.newBuilder()
                .setNumber(2).build());

        System.out.println("sending message 3");
        requestStreamStreamObserver.onNext(AvgNumberRequestStream.newBuilder()
                .setNumber(3).build());

        System.out.println("sending message 4");
        requestStreamStreamObserver.onNext(AvgNumberRequestStream.newBuilder()
                .setNumber(4).build());

        requestStreamStreamObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doBiDiStreamingCall(ManagedChannel channel) {
        SumServiceGrpc.SumServiceStub sumServiceStub = SumServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<FindMaximumRequest> requestStreamObserver =
                sumServiceStub.findMaximum(new StreamObserver<FindMaximumResponse>() {

                    @Override
                    public void onNext(FindMaximumResponse value) {

                        System.out.println("new max number -> " + value.getNumber());
                    }

                    @Override
                    public void onError(Throwable t) {

                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {

                        System.out.println("server is done sending messages");
                        //latch.countDown();
                    }
                });


        Arrays.asList(1, 5, 3, 6, 2, 20).forEach(number -> {
            System.out.println("sending new value: " + number);
            requestStreamObserver.onNext(FindMaximumRequest.newBuilder()
                    .setNumber(number)
                    .build());

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });

        requestStreamObserver.onCompleted();

        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doUnaryCallWithDeadline(ManagedChannel channel) {

        SumServiceGrpc.SumServiceBlockingStub sumServiceBlockingStub = SumServiceGrpc.newBlockingStub(channel);


        try {
            SumResponse sumResponse = sumServiceBlockingStub.withDeadline(Deadline.after(5L, TimeUnit.SECONDS))
                    .sumWithDeadline(SumRequest.newBuilder()
                            .setSum(Sum.newBuilder()
                                    .setFirstNumber(2)
                                    .setSecondNumber(7)
                                    .build())
                            .build());

            System.out.println(sumResponse.getSum());
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
        }


    }

}
