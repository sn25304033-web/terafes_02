package com.hotel.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Reservation {
    private int id;
    private int userId;
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
    private String cardLast4;
    private String status;
    private Timestamp createdAt;
    private String hotelName;
    private int hotelId;
    private String hotelCity;
    private String roomTypeName;
    private String userEmail;
    private String imageUrl;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

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

    public String getCardLast4() { return cardLast4; }
    public void setCardLast4(String cardLast4) { this.cardLast4 = cardLast4; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    public int getHotelId() { return hotelId; }
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }

    public String getHotelCity() { return hotelCity; }
    public void setHotelCity(String hotelCity) { this.hotelCity = hotelCity; }

    public String getRoomTypeName() { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    private String userLang;
    private String hotelAddress;
    public String getUserLang() { return userLang; }
    public void setUserLang(String userLang) { this.userLang = userLang; }
    public String getHotelAddress() { return hotelAddress; }
    public void setHotelAddress(String hotelAddress) { this.hotelAddress = hotelAddress; }

    /** A taxi can be booked while the stay is still active (not cancelled / finished). */
    public boolean isTaxiBookable() {
        return ("PENDING".equals(status) || "CONFIRMED".equals(status) || "CHECKED_IN".equals(status))
                && checkOut != null && !checkOut.toLocalDate().isBefore(java.time.LocalDate.now());
    }

    /** True while the guest may still cancel (not yet checked in / finished / cancelled). */
    public boolean isCancellable() {
        return ("PENDING".equals(status) || "CONFIRMED".equals(status))
                && checkIn != null && !checkIn.toLocalDate().isBefore(java.time.LocalDate.now());
    }
}
