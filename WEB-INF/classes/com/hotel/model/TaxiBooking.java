package com.hotel.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/** A taxi ride from the guest's pickup point to the hotel of one reservation. */
public class TaxiBooking {
    private int id;
    private int userId;
    private int reservationId;
    private String pickupType;
    private String pickupLocation;
    private Timestamp pickupTime;
    private int passengers;
    private int luggage;
    private String carType;
    private BigDecimal estimatedFare;
    private String notes;
    private String status;
    private String driverName;
    private String driverPhone;
    private Timestamp createdAt;
    private String hotelName;
    private String hotelAddress;
    private String guestName;
    private String guestPhone;
    private String userEmail;
    private String userLang;
    private Date checkIn;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public String getPickupType() { return pickupType; }
    public void setPickupType(String pickupType) { this.pickupType = pickupType; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public Timestamp getPickupTime() { return pickupTime; }
    public void setPickupTime(Timestamp pickupTime) { this.pickupTime = pickupTime; }

    public int getPassengers() { return passengers; }
    public void setPassengers(int passengers) { this.passengers = passengers; }

    public int getLuggage() { return luggage; }
    public void setLuggage(int luggage) { this.luggage = luggage; }

    public String getCarType() { return carType; }
    public void setCarType(String carType) { this.carType = carType; }

    public BigDecimal getEstimatedFare() { return estimatedFare; }
    public void setEstimatedFare(BigDecimal estimatedFare) { this.estimatedFare = estimatedFare; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getDriverPhone() { return driverPhone; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    public String getHotelAddress() { return hotelAddress; }
    public void setHotelAddress(String hotelAddress) { this.hotelAddress = hotelAddress; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserLang() { return userLang; }
    public void setUserLang(String userLang) { this.userLang = userLang; }

    public Date getCheckIn() { return checkIn; }
    public void setCheckIn(Date checkIn) { this.checkIn = checkIn; }

    /** The guest may cancel until 2 hours before pickup, while it's not finished/cancelled. */
    public boolean isCancellable() {
        return ("REQUESTED".equals(status) || "CONFIRMED".equals(status)) && pickupTime != null
                && pickupTime.toLocalDateTime().isAfter(java.time.LocalDateTime.now().plusHours(2));
    }
}
