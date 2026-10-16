package com.hotel.model;

import java.math.BigDecimal;
import java.sql.Date;

public class PendingBooking implements java.io.Serializable {
    private int roomTypeId;
    private Date checkIn;
    private Date checkOut;
    private int roomsCount;
    private int guests;
    private int nights;
    private BigDecimal totalAmount;
    private String guestName;
    private String guestPhone;
    private String specialRequests;
    private String paymentMethod;
    private String cardHolderName;
    private String cardLast4;

    public int getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(int roomTypeId) { this.roomTypeId = roomTypeId; }

    public Date getCheckIn() { return checkIn; }
    public void setCheckIn(Date checkIn) { this.checkIn = checkIn; }

    public Date getCheckOut() { return checkOut; }
    public void setCheckOut(Date checkOut) { this.checkOut = checkOut; }

    public int getRoomsCount() { return roomsCount; }
    public void setRoomsCount(int roomsCount) { this.roomsCount = roomsCount; }

    public int getGuests() { return guests; }
    public void setGuests(int guests) { this.guests = guests; }

    public int getNights() { return nights; }
    public void setNights(int nights) { this.nights = nights; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

    public String getCardLast4() { return cardLast4; }
    public void setCardLast4(String cardLast4) { this.cardLast4 = cardLast4; }
    /** Serializable so it can live in the HttpSession between the booking and review steps. */
    private static final long serialVersionUID = 1L;
}
