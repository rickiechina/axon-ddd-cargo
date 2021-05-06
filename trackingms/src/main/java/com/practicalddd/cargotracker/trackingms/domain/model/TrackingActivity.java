package com.practicalddd.cargotracker.trackingms.domain.model;

import com.practicalddd.cargotracker.shareddomain.commands.AssignTrackingDetailsToCargoCommand;
import com.practicalddd.cargotracker.shareddomain.events.CargoTrackedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.lang.invoke.MethodHandles;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;


@Aggregate
public class TrackingActivity {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    //@TargetAggregateIdentifier
    @AggregateIdentifier
    private String trackingId; // Aggregate Identifier

    protected TrackingActivity(){
        logger.info("创建一个空的TrackingActivity对象");
    }

    @CommandHandler
    public TrackingActivity(AssignTrackingDetailsToCargoCommand assignTrackingDetailsToCargoCommand) {
        trackingId = assignTrackingDetailsToCargoCommand.getTrackingId();

        logger.info("AssignTrackingDetailsToCargoCommand 命令处理程序 bookingId= " +
                assignTrackingDetailsToCargoCommand.getBookingId() +
                " trackingId=" + trackingId);
        apply(new CargoTrackedEvent(
                    assignTrackingDetailsToCargoCommand.getBookingId(),
                    assignTrackingDetailsToCargoCommand.getTrackingId()
                ));
    }

    /**
     * Event Handler for the Cargo Booked Event
     * @param cargoTrackedEvent
     */
    @EventSourcingHandler
    // 表明聚合是事件溯源的，并且对BookCargoCommand命令引发的CargoBookedEvent事件感兴趣
    public void on(CargoTrackedEvent cargoTrackedEvent) {
        logger.info("事件溯源处理程序：Applying {}", cargoTrackedEvent);
        // State being maintained
        trackingId = cargoTrackedEvent.getTrackingId();
    }
}
