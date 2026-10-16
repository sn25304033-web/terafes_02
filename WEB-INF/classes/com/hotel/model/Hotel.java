package com.hotel.model;

import java.math.BigDecimal;

public class Hotel {
    private int id;
    private String name;
    private String city;
    private String address;
    private String description;
    private int stars;
    private BigDecimal rating;
    private String imageUrl;
    private String amenities;
    private boolean active;
    private BigDecimal minPrice;
    private int reviewCount;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    /** Amenities as a list, for c:forEach in JSPs. */
    public java.util.List<String> getAmenityList() {
        java.util.List<String> list = new java.util.ArrayList<>();
        if (amenities != null) for (String a : amenities.split(",")) if (!a.isBlank()) list.add(a.trim());
        return list;
    }
}
