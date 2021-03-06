package com.practicalddd.cargotracker.bookingms.domain.queries;

/**
 * Implementation of Cargo Summary Query class. It takes in a Booking Id which is the criteria for the query
 * CargoSummaryQuery类的实现，接收BookingId参数作为查询条件
 */
public class CargoSummaryQuery {

    private String bookingId; //Criteria of the Query
    public CargoSummaryQuery(String bookingId){
        this.bookingId = bookingId;
    }

    public String getBookingId(){return this.bookingId;}
    @Override
    public String toString() { return "Cargo Summary for Booking Id" + bookingId; }
}
