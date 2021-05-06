package com.practicalddd.cargotracker.bookingms.application.internal.sagaparticipants;

import com.practicalddd.cargotracker.bookingms.application.internal.commandgateways.CargoBookingService;
import com.practicalddd.cargotracker.bookingms.domain.commands.AssignRouteToCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.events.CargoBookedEvent;
import com.practicalddd.cargotracker.bookingms.domain.events.CargoRoutedEvent;
import com.practicalddd.cargotracker.shareddomain.events.CargoTrackedEvent;
import com.practicalddd.cargotracker.shareddomain.commands.AssignTrackingDetailsToCargoCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.UUID;

/**
 * BookingSagaManager是Booking Saga的实现。
 * 当CargoBookedEvent事件触发时，Saga启动。
 * 当CargoTrackedEvent事件触发时，Saga结束。
 * The Booking Saga Manager is the implementation of the Booking saga.
 * The Saga starts when the Cargo Booked Event is raised
 * The Saga ends when the Tracking Details have been assigned to the Cargo
 */

@Saga
public class BookingSagaManager {

    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    private transient CommandGateway commandGateway;
    @Inject
    private transient CargoBookingService cargoBookingService;

    public BookingSagaManager(){}

    /**
     * Dependencies for the Saga Manager
     * @param commandGateway
     */
//    public BookingSagaManager(CommandGateway commandGateway,CargoBookingService cargoBookingService){
//        this.commandGateway = commandGateway;
//        this.cargoBookingService = cargoBookingService;
//    }

    /**
     * Handle the Cargo Booked Event, Start the Saga and invoke the Assign Route to Cargo Command
     * @param cargoBookedEvent
     */
    @StartSaga
    @SagaEventHandler(associationProperty = "bookingId")
    public void handle(CargoBookedEvent cargoBookedEvent){
        logger.info("处理Saga中的CargoBookedEvent事件：" + cargoBookedEvent.getBookingId());

        //Send the Command to assign a route to the Cargo
        commandGateway.send(new AssignRouteToCargoCommand(cargoBookedEvent.getBookingId(),
                                            cargoBookingService.getLegsForRoute(cargoBookedEvent.getRouteSpecification())));
    }

    /**
     * Handle the Cargo Routed Event and invoke the Assign Tracking Details to Cargo Command
     * @param cargoRoutedEvent
     */
    @SagaEventHandler(associationProperty = "bookingId")
    public void handle(CargoRoutedEvent cargoRoutedEvent){
        logger.info("处理Saga中的CargoRoutedEvent事件 bookingId= " + cargoRoutedEvent.getBookingId());

        String trackingId = UUID.randomUUID().toString(); // Generate a random tracking identifier

        SagaLifecycle.associateWith("trackingId",trackingId);
        //Send the Command to assign tracking details to the Cargo
        commandGateway.send(new AssignTrackingDetailsToCargoCommand(
                        cargoRoutedEvent.getBookingId(),trackingId));
    }

    /**
     * Handle the Cargo Tracked Event and end the Saga
     * @param cargoTrackedEvent
     */
    @SagaEventHandler(associationProperty = "trackingId")
    public void handle(CargoTrackedEvent cargoTrackedEvent) {
        logger.info("=== end ===");
        SagaLifecycle.end();
    }

}
