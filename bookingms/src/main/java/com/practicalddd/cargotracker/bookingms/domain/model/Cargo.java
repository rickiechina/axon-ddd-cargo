package com.practicalddd.cargotracker.bookingms.domain.model;

import java.lang.invoke.MethodHandles;
import com.practicalddd.cargotracker.bookingms.domain.commands.AssignRouteToCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.commands.BookCargoCommand;
import com.practicalddd.cargotracker.bookingms.domain.commands.ChangeDestinationCommand;
import com.practicalddd.cargotracker.bookingms.domain.events.CargoBookedEvent;
import com.practicalddd.cargotracker.bookingms.domain.events.CargoDestinationChangedEvent;
import com.practicalddd.cargotracker.bookingms.domain.events.CargoRoutedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
public class Cargo {

    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @AggregateIdentifier
    private String bookingId; // Aggregate Identifier
    private BookingAmount bookingAmount; //Booking Amount
    private Location origin; //Origin Location of the Cargo
    private RouteSpecification routeSpecification; //Route Specification of the Cargo
    private Itinerary itinerary; //Itinerary Assigned to the Cargo
    private RoutingStatus routingStatus; //Routing Status of the Cargo
    private TransportStatus transportStatus; //Transport Status of the Cargo


    protected Cargo() {
        logger.info("创建一个空的Cargo对象");
    }

    @CommandHandler
    public Cargo(BookCargoCommand bookCargoCommand) {

        logger.info("BookCargoCommand命令处理程序：bookingId {}", bookCargoCommand.getBookingId());
        if(bookCargoCommand.getBookingAmount() < 0){
            throw new IllegalArgumentException("Booking Amount cannot be negative");
        }

        apply(new CargoBookedEvent(bookCargoCommand.getBookingId(),
                                    new BookingAmount(bookCargoCommand.getBookingAmount()),
                                    new Location(bookCargoCommand.getOriginLocation()),
                                    new RouteSpecification(
                                            new Location(bookCargoCommand.getOriginLocation()),
                                            new Location(bookCargoCommand.getDestLocation()),
                                            bookCargoCommand.getDestArrivalDeadline())));
    }

    /**
     * Command Handler for Assigning the Route to a Cargo
     * @param assignRouteToCargoCommand
     */

    @CommandHandler
    public void handleAssigntoRoute(AssignRouteToCargoCommand assignRouteToCargoCommand) {
        logger.info("AssignRouteToCargoCommand命令处理程序：Booking Id=" +assignRouteToCargoCommand.getBookingId());
        logger.info("Assign Route to Command: "+ this.routingStatus);
        if(routingStatus.equals(RoutingStatus.ROUTED)){
            throw new IllegalArgumentException("Cargo already routed");
        }
        apply( new CargoRoutedEvent(assignRouteToCargoCommand.getBookingId(),
                                    new Itinerary(assignRouteToCargoCommand.getLegs())));

    }

    /**
     * Cargo Handler for changing the Destination of a Cargo
     * @param changeDestinationCommand
     */
    @CommandHandler
    public void handleChangeDestination(ChangeDestinationCommand changeDestinationCommand) {
        try {
            logger.info("ChangeDestinationCommand 命令处理程序");
            if (routingStatus.equals(RoutingStatus.ROUTED)) {
                throw new IllegalArgumentException("Cannot change destination of a Routed Cargo");
            }

            apply(new CargoDestinationChangedEvent(changeDestinationCommand.getBookingId(),
                    new RouteSpecification(origin,
                            new Location(changeDestinationCommand.getNewDestinationLocation()),
                            routeSpecification.getArrivalDeadline())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Event Handler for the Cargo Booked Event
     * @param cargoBookedEvent
     */
    @EventSourcingHandler
    // 表明聚合是事件溯源的，并且对BookCargoCommand命令引发的CargoBookedEvent事件感兴趣
    public void on(CargoBookedEvent cargoBookedEvent) {
        logger.info("事件溯源处理程序：Applying {}", cargoBookedEvent);
        // State being maintained
        bookingId = cargoBookedEvent.getBookingId();
        bookingAmount = cargoBookedEvent.getBookingAmount();
        origin = cargoBookedEvent.getOriginLocation();
        routeSpecification = cargoBookedEvent.getRouteSpecification();
        routingStatus = RoutingStatus.NOT_ROUTED;
        transportStatus = TransportStatus.NOT_RECEIVED;
        logger.info("Routing Status is "+routingStatus);
    }

    /**
     * Event Handler for the Cargo Routed Event
     * 表明聚合是事件溯源的，并且对BookCargoCommand引发的CargoRoutedEvent事件感兴趣
     * @param cargoRoutedEvent
     */
    @EventSourcingHandler
    public void on(CargoRoutedEvent cargoRoutedEvent) {
        logger.info("事件溯源处理程序： {}", cargoRoutedEvent);
        logger.info("bookingId= " + cargoRoutedEvent.getBookingId());
        bookingId = cargoRoutedEvent.getBookingId();
        itinerary = cargoRoutedEvent.getItinerary();
        routingStatus = RoutingStatus.ROUTED;
    }

    /**
     * Event Handler for the Change Destination Event
     * @param cargoDestinationChangedEvent
     */
    @EventSourcingHandler //Annotation indicating that the Aggregate is Event Sourced and is interested in the Cargo Booked Event raised by the Book Cargo Command
    public void on(CargoDestinationChangedEvent cargoDestinationChangedEvent) {
        logger.info("Applying {}", cargoDestinationChangedEvent);
        routingStatus = RoutingStatus.NOT_ROUTED;
        routeSpecification = cargoDestinationChangedEvent.getNewRouteSpecification();
    }

}
