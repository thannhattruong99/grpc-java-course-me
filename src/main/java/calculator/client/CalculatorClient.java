package calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {

  private static void doSum(ManagedChannel channel){
    System.out.println("Enter doSum");
    CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);
    SumResponse response = stub.sum(SumRequest.newBuilder().setFirstNumber(1).setSecondNumber(2).build());
    System.out.println("Response: " + response.getResult());
  }

  private static void doPrimes(ManagedChannel channel){
    System.out.println("Enter doPrimes");
    CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);
    stub.primes(PrimeRequest.newBuilder().setNumber(567890).build())
        .forEachRemaining(
            primeResponse -> {
              System.out.println(primeResponse.getPrimeFactor());
            });
  }

  private static void doAvg(ManagedChannel channel) throws InterruptedException {
    System.out.println("Enter doAvg");
    CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);
    CountDownLatch latch = new CountDownLatch(1);
    StreamObserver<AvgRequest> stream = stub.avg(new StreamObserver<AvgResponse>() {
      @Override
      public void onNext(AvgResponse value) {
        System.out.println("Avg: " + value.getResult());
      }

      @Override
      public void onError(Throwable t) {

      }

      @Override
      public void onCompleted() {
        latch.countDown();
      }
    });

    Arrays.asList(1, 2, 3, 4).forEach(number -> {
      stream.onNext(AvgRequest.newBuilder().setNumber(number).build());
    });
    stream.onCompleted();
    latch.await(3, TimeUnit.SECONDS);
  }

  private static void doMax(ManagedChannel channel) throws InterruptedException {
    System.out.println("Enter doMax");
    CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);
    CountDownLatch latch = new CountDownLatch(1);
    StreamObserver<MaxRequest> streamObserver = stub.max(new StreamObserver<MaxResponse>() {
      @Override
      public void onNext(MaxResponse value) {
        System.out.println("Max: " + value.getMax());
      }

      @Override
      public void onError(Throwable t) {

      }

      @Override
      public void onCompleted() {
        latch.countDown();
      }
    });

    Arrays.asList(1, 5, 3, 6, 2, 20)
        .forEach(
            number -> {
              streamObserver.onNext(MaxRequest.newBuilder().setNumber(number).build());
            });
    streamObserver.onCompleted();
    latch.await(3, TimeUnit.SECONDS);
  }

  public static void main(String[] args) throws InterruptedException {
    if (args.length == 0) {
      System.out.println("Need one argument to work");
      return;
    }

    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052).usePlaintext().build();
    switch (args[0]){
      case "sum": doSum(channel); break;
      case "primes": doPrimes(channel); break;
      case "avg": doAvg(channel); break;
      case "max": doMax(channel); break;
      default:
        System.out.println("Keyword invalid: " + args[0]);
    }
  }
}
