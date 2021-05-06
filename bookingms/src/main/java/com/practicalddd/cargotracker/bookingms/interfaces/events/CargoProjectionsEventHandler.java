package com.practicalddd.cargotracker.bookingms.interfaces.events;

import com.practicalddd.cargotracker.bookingms.application.internal.querygateways.CargoProjectionService;
import com.practicalddd.cargotracker.bookingms.domain.events.CargoBookedEvent;
import com.practicalddd.cargotracker.bookingms.domain.events.CargoDestinationChangedEvent;
import com.practicalddd.cargotracker.bookingms.domain.events.CargoRoutedEvent;
import com.practicalddd.cargotracker.bookingms.domain.model.RoutingStatus;
import com.practicalddd.cargotracker.bookingms.domain.model.TransportStatus;
import com.practicalddd.cargotracker.bookingms.domain.projections.CargoSummary;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

/**
 * Event Handlers for all events raised by the Cargo Aggregate
 */
//@Service
@Component
public class CargoProjectionsEventHandler {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private CargoProjectionService cargoProjectionService; //Dependencies

    public CargoProjectionsEventHandler(CargoProjectionService cargoProjectionService){
        this.cargoProjectionService = cargoProjectionService;
    }
    /**
     * Event Handler for the Cargo Booked Event.
     * Converts the Event Data to the corresponding Aggregate Projection Model
     * and delegates to the Application Service to process it further
     * @param cargoBookedEvent
     */
    @EventHandler
    public void cargoBookedEventHandler(CargoBookedEvent cargoBookedEvent) {
        logger.info("CargoBookedEvent事件处理程序：BookingId= {}", cargoBookedEvent.getBookingId());

        CargoSummary cargoSummary = new CargoSummary(cargoBookedEvent.getBookingId(),
                TransportStatus.NOT_RECEIVED.toString(),RoutingStatus.NOT_ROUTED,
                cargoBookedEvent.getRouteSpecification().getOrigin().getUnLocCode(),
                cargoBookedEvent.getRouteSpecification().getDestination().getUnLocCode(),
                cargoBookedEvent.getRouteSpecification().getArrivalDeadline());
        try {
            cargoProjectionService.storeCargoSummary(cargoSummary);
        } catch (Exception e) {
            logger.info("=== 出现异常 ===");
            e.printStackTrace();
        }
    }

    /**
     * Event Handler for the Cargo Routed Event
     * @param cargoRoutedEvent
     */
    @EventHandler
    public void cargoRoutedEventhandler(CargoRoutedEvent cargoRoutedEvent){
        logger.info("CargoRoutedEvent事件处理程序： bookingId= {}",cargoRoutedEvent.getBookingId());
        CargoSummary cargoSummary = cargoProjectionService.getCargoSummary(cargoRoutedEvent.getBookingId());
        cargoSummary.setRouting_status(RoutingStatus.ROUTED);
    }

    @EventHandler
    public void changeDestEventhandler(CargoDestinationChangedEvent cargoDestinationChangedEvent) {
        logger.info("changeDestEventhandler事件处理程序");
        CargoSummary cargoSummary = cargoProjectionService.getCargoSummary(cargoDestinationChangedEvent.getBookingId());
        cargoSummary.setSpec_destination_id(cargoDestinationChangedEvent.getNewRouteSpecification().getDestination().getUnLocCode());
    }
}
