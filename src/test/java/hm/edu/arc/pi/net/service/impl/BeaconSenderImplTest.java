package hm.edu.arc.pi.net.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import hm.edu.arc.pi.net.service.RateAdaptionService;
import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BeaconSenderImplTest {

  @Mock private RateAdaptionService rateAdaptionService;
  @Mock private ObjectMapper objectMapper;
  @Mock private ScheduledExecutorService scheduler;
  @Mock private NetworkInterface networkInterface;
  @Mock private ScheduledFuture scheduledFuture;

  private MockedStatic<NetworkInterface> mockedNetworkInterface;
  private MockedConstruction<DatagramSocket> mockedDatagramSocket;

  private BeaconSenderImpl beaconSender;

  @BeforeEach
  void setUp() throws Exception {
    // Setup static mocking for NetworkInterface
    mockedNetworkInterface = mockStatic(NetworkInterface.class);

    // Mock network interface and address
    InetAddress mockAddress = InetAddress.getByName("192.168.42.1");
    Enumeration<InetAddress> addresses =
        Collections.enumeration(Collections.singletonList(mockAddress));
    when(networkInterface.getInetAddresses()).thenReturn(addresses);
    mockedNetworkInterface
        .when(() -> NetworkInterface.getByName("wlan0"))
        .thenReturn(networkInterface);

    // Mock DatagramSocket construction
    mockedDatagramSocket =
        mockConstruction(
            DatagramSocket.class,
            (mock, context) -> {
              // Mock the socket methods that will be called
              doNothing().when(mock).setBroadcast(true);
              doNothing().when(mock).bind(any(InetSocketAddress.class));
            });

    // Create instance with mocked dependencies
    beaconSender = new BeaconSenderImpl(rateAdaptionService, objectMapper, scheduler);
  }

  @AfterEach
  void tearDown() {
    mockedNetworkInterface.close();
    mockedDatagramSocket.close();
  }

  @Test
  void whenStartSending_thenSchedulerIsCalled() {
    // Given
    double initialInterval = 1000.0;
    when(rateAdaptionService.obtainSendingInterval()).thenReturn(initialInterval);
    when(scheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any()))
        .thenReturn(scheduledFuture);

    // When
    beaconSender.startSending();

    // Then
    verify(scheduler)
        .scheduleAtFixedRate(
            any(Runnable.class), eq(0L), eq((long) initialInterval), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void whenStartSendingWhileRunning_thenDoNotStartAgain() {
    // Given
    when(scheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any()))
        .thenReturn(scheduledFuture);
    beaconSender.startSending();

    // When
    beaconSender.startSending();

    // Then
    verify(scheduler, times(1))
        .scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any());
  }

  @Test
  void whenStopSending_thenTaskIsCancelled() {
    // Given
    when(scheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any()))
        .thenReturn(scheduledFuture);
    beaconSender.startSending();

    // When
    beaconSender.stopSending();

    // Then
    verify(scheduledFuture).cancel(false);
  }

  @Test
  void whenStopSendingWhileNotRunning_thenWarningLogged() {
    // When
    beaconSender.stopSending();

    // Then
    verify(scheduler, never())
        .scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any());
  }

  @Test
  void whenIntervalIsZero_thenScheduledForImmediateSending() throws Exception {
    // Given
    when(rateAdaptionService.obtainSendingInterval()).thenReturn(0.0);
    when(scheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any()))
        .thenReturn(scheduledFuture);
    when(scheduler.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class)))
        .thenReturn(scheduledFuture);
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");

    // When
    beaconSender.startSending();

    // Then
    verify(scheduler)
        .scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(0L), eq(TimeUnit.MILLISECONDS));

    // Simulate the sendBeacon method being called
    Method sendBeaconMethod = BeaconSenderImpl.class.getDeclaredMethod("sendBeacon");
    sendBeaconMethod.setAccessible(true);
    sendBeaconMethod.invoke(beaconSender);

    // Verify that the fixed rate task was cancelled and a one-time immediate task was scheduled
    verify(scheduledFuture).cancel(false);
    verify(scheduler).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void whenIntervalIsPositive_thenScheduledWithCorrectInterval() {
    // Given
    double interval = 500.0;
    when(rateAdaptionService.obtainSendingInterval()).thenReturn(interval);
    when(scheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any()))
        .thenReturn(scheduledFuture);

    // When
    beaconSender.startSending();

    // Then
    verify(scheduler)
        .scheduleAtFixedRate(
            any(Runnable.class), eq(0L), eq((long) interval), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  void whenNetworkInterfaceNotFound_thenExceptionThrown() {
    // Given
    mockedNetworkInterface.when(() -> NetworkInterface.getByName("wlan0")).thenReturn(null);

    // When/Then
    assertThrows(
        RuntimeException.class,
        () -> new BeaconSenderImpl(rateAdaptionService, objectMapper, scheduler));
  }
}
