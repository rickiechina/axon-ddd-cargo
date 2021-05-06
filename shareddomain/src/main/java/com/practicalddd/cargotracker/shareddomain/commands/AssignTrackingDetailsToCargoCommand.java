package com.practicalddd.cargotracker.shareddomain.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

/**
 * Implementation class for the Assign Tracking Details to Cargo Command
 */
public class AssignTrackingDetailsToCargoCommand {
    private String bookingId;
    // 标识符，指示Axon框架需要其上处理命令的唯一实例
    @TargetAggregateIdentifier
    private String trackingId;
    public AssignTrackingDetailsToCargoCommand(String bookingId, String trackingId){
        this.bookingId = bookingId;
        this.trackingId = trackingId;
    }

    public void setBookingId(String bookingId){this.bookingId = bookingId;}
    public String getBookingId(){return this.bookingId;}
    public void setTrackingId(String trackingId){this.trackingId = trackingId;}
    public String getTrackingId(){return this.trackingId;}
}
