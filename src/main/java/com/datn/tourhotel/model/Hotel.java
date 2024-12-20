package com.datn.tourhotel.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String img;
    
    private String img2; 
    
    private String img3; 
    
    @Column(unique = true, nullable = true, length = 5000)
    private String describe;
    
    @OneToOne(cascade = CascadeType.ALL)
    private Address address;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Room> rooms = new ArrayList<>();

    @OneToMany(mappedBy = "hotel")
    private List<Booking> bookings = new ArrayList<>();

    @ManyToOne
    @JoinColumn(nullable = false)
    private HotelManager hotelManager;
    
    @Column(nullable = true)
    private Boolean isDelete; // Trạng thái (ACTIVE, INACTIVE)

    @Override
    public String toString() {
        return "Hotel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", img='" + img + '\'' +
                ", img2='" + img2 + '\'' +
                ", img3='" + img3 + '\'' +
                ", describe='" + describe + '\'' +
                ", address=" + address +
                ", rooms=" + rooms +
                ", hotelManager=" + hotelManager +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hotel hotel = (Hotel) o;
        return Objects.equals(id, hotel.id) &&
                Objects.equals(name, hotel.name) &&
                Objects.equals(img, hotel.img) &&
                Objects.equals(img2, hotel.img2) &&
                Objects.equals(img3, hotel.img3) &&
                Objects.equals(describe, hotel.describe) &&
                Objects.equals(hotelManager, hotel.hotelManager);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, img, img2, img3, describe, hotelManager);
    }
}
