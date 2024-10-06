package com.datn.tourhotel.initialize;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.datn.tourhotel.model.*;
import com.datn.tourhotel.model.enums.RoleType;
import com.datn.tourhotel.model.enums.RoomType;
import com.datn.tourhotel.repository.*;

import java.time.LocalDate;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;
    private final HotelManagerRepository hotelManagerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AddressRepository addressRepository;
    private final HotelRepository hotelRepository;
    private final AvailabilityRepository availabilityRepository;

    @Override
    @Transactional
    public void run(String... args) {

        try {
            log.warn("Checking if test data persistence is required...");

            if (roleRepository.count() == 0 && userRepository.count() == 0) {
                log.info("Initiating test data persistence");

                Role adminRole = new Role(RoleType.ADMIN);
                Role customerRole = new Role(RoleType.CUSTOMER);
                Role hotelManagerRole = new Role(RoleType.HOTEL_MANAGER);

                roleRepository.save(adminRole);
                roleRepository.save(customerRole);
                roleRepository.save(hotelManagerRole);
                log.info("Role data persisted");

                User user1 = User.builder().email("admin@hotel.com").username("admin@hotel.com").password(passwordEncoder.encode("1")).name("Admin").lastName("Admin").img("admin_profile.png").role(adminRole).build();
                User user2 = User.builder().email("customer1@hotel.com").username("customer1@hotel.com").password(passwordEncoder.encode("1")).name("Kaya Alp").lastName("Koker").img("customer1_profile.png").role(customerRole).build();
                User user3 = User.builder().email("manager1@hotel.com").username("manager1@hotel.com").password(passwordEncoder.encode("1")).name("John").lastName("Doe").img("manager1_profile.png").role(hotelManagerRole).build();
                User user4 = User.builder().email("manager2@hotel.com").username("manager2@hotel.com").password(passwordEncoder.encode("1")).name("Max").lastName("Mustermann").img("manager2_profile.png").role(hotelManagerRole).build();
                User user5 = User.builder().email("manager3@hotel.com").username("manager3@hotel.com").password(passwordEncoder.encode("1")).name("Nguyễn").lastName("Văn An").img("manager3_profile.png").role(hotelManagerRole).build();

                userRepository.save(user1);
                userRepository.save(user2);
                userRepository.save(user3);
                userRepository.save(user4);
                userRepository.save(user5);


                Admin admin1 = Admin.builder().user(user1).build();
                Customer c1 = Customer.builder().user(user2).build();
                HotelManager hm1 = HotelManager.builder().user(user3).build();
                HotelManager hm2 = HotelManager.builder().user(user4).build();
                HotelManager hm3 = HotelManager.builder().user(user5).build();

                adminRepository.save(admin1);
                customerRepository.save(c1);
                hotelManagerRepository.save(hm1);
                hotelManagerRepository.save(hm2);
                hotelManagerRepository.save(hm3);
                log.info("User data persisted");

                Address addressIst1 = Address.builder().addressLine("Acısu Sokağı No:19, 34357").city("Istanbul")
                        .country("Turkey").location("https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3918.454437462182!2d106.62420897512922!3d10.852999257776432!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31752bee0b0ef9e5%3A0x5b4da59e47aa97a8!2zQ8O0bmcgVmnDqm4gUGjhuqduIE3hu4FtIFF1YW5nIFRydW5n!5e0!3m2!1svi!2s!4v1727949271625!5m2!1svi!2s").build();
                Address addressIst2 = Address.builder().addressLine("Çırağan Cd. No:28, 34349 Beşiktaş").city("Istanbul")
                        .country("Turkey").location("https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3918.454437462182!2d106.62420897512922!3d10.852999257776432!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31752bee0b0ef9e5%3A0x5b4da59e47aa97a8!2zQ8O0bmcgVmnDqm4gUGjhuqduIE3hu4FtIFF1YW5nIFRydW5n!5e0!3m2!1svi!2s!4v1727949271625!5m2!1svi!2s").build();
                Address addressIst3 = Address.builder().addressLine("Çırağan Cd. No:32, 34349 Beşiktaş").city("Istanbul")
                        .country("Turkey").location("https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3918.454437462182!2d106.62420897512922!3d10.852999257776432!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31752bee0b0ef9e5%3A0x5b4da59e47aa97a8!2zQ8O0bmcgVmnDqm4gUGjhuqduIE3hu4FtIFF1YW5nIFRydW5n!5e0!3m2!1svi!2s!4v1727949271625!5m2!1svi!2s").build();

                Address addressBerlin1 = Address.builder().addressLine("Unter den Linden 77").city("Berlin")
                        .country("Germany").location("https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3918.454437462182!2d106.62420897512922!3d10.852999257776432!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31752f2566780947%3A0x1f7e6b2d3b72e6b9!2zVHJ1bmcgVMOibSBUcmnhu4NuIEzDo20gSOG7mWkgTmdo4buLIFF14buRYyBU4bq_IFNreSBFeHBvIFZp4buHdCBOYW0!5e0!3m2!1svi!2s!4v1727949389998!5m2!1svi!2s").build();
                Address addressBerlin2 = Address.builder().addressLine("Potsdamer Platz 3, Mitte, 10785").city("Berlin")
                        .country("Germany").location("https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3918.454437462182!2d106.62420897512922!3d10.852999257776432!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31752f2566780947%3A0x1f7e6b2d3b72e6b9!2zVHJ1bmcgVMOibSBUcmnhu4NuIEzDo20gSOG7mWkgTmdo4buLIFF14buRYyBU4bq_IFNreSBFeHBvIFZp4buHdCBOYW0!5e0!3m2!1svi!2s!4v1727949389998!5m2!1svi!2s").build();
                Address addressBerlin3 = Address.builder().addressLine("Budapester Str. 2, Mitte, 10787").city("Berlin")
                        .country("Germany").location("https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3918.454437462182!2d106.62420897512922!3d10.852999257776432!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31752f2566780947%3A0x1f7e6b2d3b72e6b9!2zVHJ1bmcgVMOibSBUcmnhu4NuIEzDo20gSOG7mWkgTmdo4buLIFF14buRYyBU4bq_IFNreSBFeHBvIFZp4buHdCBOYW0!5e0!3m2!1svi!2s!4v1727949389998!5m2!1svi!2s").build();
                Address addressHCM1 = Address.builder().addressLine("50 Lê Lợi, Quận 1").city("Hồ Chí Minh").country("Việt Nam").location("https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3918.454437462182!2d106.62420897512922!3d10.852999257776432!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31752bee0b0ef9e5%3A0x5b4da59e47aa97a8!2zQ8O0bmcgVmnDqm4gUGjhuqduIE3hu4FtIFF1YW5nIFRydW5n!5e0!3m2!1svi!2s!4v1727949271625!5m2!1svi!2s").build();
                Address addressHCM2 = Address.builder().addressLine("35 Nguyễn Huệ, Quận 1").city("Hồ Chí Minh").country("Việt Nam").location("https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3918.454437462182!2d106.62420897512922!3d10.852999257776432!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31752bee0b0ef9e5%3A0x5b4da59e47aa97a8!2zQ8O0bmcgVmnDqm4gUGjhuqduIE3hu4FtIFF1YW5nIFRydW5n!5e0!3m2!1svi!2s!4v1727949271625!5m2!1svi!2s").build();
                Address addressHCM3 = Address.builder().addressLine("22 Lý Tự Trọng, Quận 1").city("Hồ Chí Minh").country("Việt Nam").location("https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3918.454437462182!2d106.62420897512922!3d10.852999257776432!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31752bee0b0ef9e5%3A0x5b4da59e47aa97a8!2zQ8O0bmcgVmnDqm4gUGjhuqduIE3hu4FtIFF1YW5nIFRydW5n!5e0!3m2!1svi!2s!4v1727949271625!5m2!1svi!2s").build();

                addressRepository.save(addressHCM1);
                addressRepository.save(addressHCM2);
                addressRepository.save(addressHCM3);
                addressRepository.save(addressIst1);
                addressRepository.save(addressIst2);
                addressRepository.save(addressIst3);
                addressRepository.save(addressBerlin1);
                addressRepository.save(addressBerlin2);
                addressRepository.save(addressBerlin3);	

                Hotel hotelIst1 = Hotel.builder().name("Swissotel The Bosphorus Istanbul").img("swissotel_istanbul.jpg").img2("swissotel_istanbul_2.jpg").img3("swissotel_istanbul_3.jpg").describe("A luxurious hotel located in the heart of Istanbul.").address(addressIst1).hotelManager(hm1).build();
                Hotel hotelIst2 = Hotel.builder().name("Four Seasons Hotel Istanbul").img("four_seasons_istanbul.jpg").img2("four_seasons_istanbul_2.jpg").img3("four_seasons_istanbul_3.jpg").describe("An elegant hotel with stunning views of the Bosphorus.").address(addressIst2).hotelManager(hm1).build();
                Hotel hotelIst3 = Hotel.builder().name("Ciragan Palace Kempinski Istanbul").img("ciragan_palace_kempinski.jpg").img2("ciragan_palace_kempinski_2.jpg").img3("ciragan_palace_kempinski_3.jpg").describe("A former Ottoman palace transformed into a luxury hotel.").address(addressIst3).hotelManager(hm1).build();
                Hotel hotelBerlin1 = Hotel.builder().name("Hotel Adlon Kempinski Berlin").img("hotel_adlon_kempinski.jpg").img2("hotel_adlon_kempinski_2.jpg").img3("hotel_adlon_kempinski_3.jpg").describe("A historic hotel located near the Brandenburg Gate.").address(addressBerlin1).hotelManager(hm2).build();
                Hotel hotelBerlin2 = Hotel.builder().name("The Ritz-Carlton Berlin").img("ritz_carlton_berlin.jpg").img2("ritz_carlton_berlin_2.jpg").img3("ritz_carlton_berlin_3.jpg").describe("A sophisticated hotel with a spa and fine dining.").address(addressBerlin2).hotelManager(hm2).build();
                Hotel hotelBerlin3 = Hotel.builder().name("InterContinental Berlin").img("intercontinental_berlin.jpg").img2("intercontinental_berlin_2.jpg").img3("intercontinental_berlin_3.jpg").describe("A modern hotel in the city center with excellent amenities.").address(addressBerlin3).hotelManager(hm2).build();
                Hotel hotelHCM1 = Hotel.builder().name("Rex Hotel Saigon").img("rex_hotel_saigon.jpg").img2("rex_hotel_saigon_2.jpg").img3("rex_hotel_saigon_3.jpg").describe("A historic hotel with a rooftop bar and views of the city.").address(addressHCM1).hotelManager(hm3).build();
                Hotel hotelHCM2 = Hotel.builder().name("Park Hyatt Saigon").img("park_hyatt_saigon.jpg").img2("park_hyatt_saigon_2.jpg").img3("park_hyatt_saigon_3.jpg").describe("A luxury hotel with a beautiful garden and swimming pool.").address(addressHCM2).hotelManager(hm3).build();
                Hotel hotelHCM3 = Hotel.builder().name("Pullman Saigon").img("pullman_saigon.jpg").img2("pullman_saigon_2.jpg").img3("pullman_saigon_3.jpg").describe("A stylish hotel in the heart of Saigon with modern amenities.").address(addressHCM3).hotelManager(hm3).build();

                Room singleRoomIst1 = Room.builder().roomType(RoomType.SINGLE).pricePerNight(370).roomCount(35).img("single_room_ist1.jpg").img2("single_room_ist1_2.jpg").img3("single_room_ist1_3.jpg").hotel(hotelIst1).build();
                Room doubleRoomIst1 = Room.builder().roomType(RoomType.DOUBLE).pricePerNight(459).roomCount(45).img("double_room_ist1.jpg").img2("double_room_ist1_2.jpg").img3("double_room_ist1_3.jpg").hotel(hotelIst1).build();
                Room singleRoomIst2 = Room.builder().roomType(RoomType.SINGLE).pricePerNight(700).roomCount(25).img("single_room_ist2.jpg").img2("single_room_ist2_2.jpg").img3("single_room_ist2_3.jpg").hotel(hotelIst2).build();
                Room doubleRoomIst2 = Room.builder().roomType(RoomType.DOUBLE).pricePerNight(890).roomCount(30).img("double_room_ist2.jpg").img2("double_room_ist2_2.jpg").img3("double_room_ist2_3.jpg").hotel(hotelIst2).build();
                Room singleRoomIst3 = Room.builder().roomType(RoomType.SINGLE).pricePerNight(691).roomCount(30).img("single_room_ist3.jpg").img2("single_room_ist3_2.jpg").img3("single_room_ist3_3.jpg").hotel(hotelIst3).build();
                Room doubleRoomIst3 = Room.builder().roomType(RoomType.DOUBLE).pricePerNight(800).roomCount(75).img("double_room_ist3.jpg").img2("double_room_ist3_2.jpg").img3("double_room_ist3_3.jpg").hotel(hotelIst3).build();
                Room singleRoomBerlin1 = Room.builder().roomType(RoomType.SINGLE).pricePerNight(120.0).roomCount(25).img("single_room_berlin1.jpg").img2("single_room_berlin1_2.jpg").img3("single_room_berlin1_3.jpg").hotel(hotelBerlin1).build();
                Room doubleRoomBerlin1 = Room.builder().roomType(RoomType.DOUBLE).pricePerNight(250.0).roomCount(15).img("double_room_berlin1.jpg").img2("double_room_berlin1_2.jpg").img3("double_room_berlin1_3.jpg").hotel(hotelBerlin1).build();
                Room singleRoomBerlin2 = Room.builder().roomType(RoomType.SINGLE).pricePerNight(300).roomCount(50).img("single_room_berlin2.jpg").img2("single_room_berlin2_2.jpg").img3("single_room_berlin2_3.jpg").hotel(hotelBerlin2).build();
                Room doubleRoomBerlin2 = Room.builder().roomType(RoomType.DOUBLE).pricePerNight(400).roomCount(50).img("double_room_berlin2.jpg").img2("double_room_berlin2_2.jpg").img3("double_room_berlin2_3.jpg").hotel(hotelBerlin2).build();
                Room singleRoomBerlin3 = Room.builder().roomType(RoomType.SINGLE).pricePerNight(179).roomCount(45).img("single_room_berlin3.jpg").img2("single_room_berlin3_2.jpg").img3("single_room_berlin3_3.jpg").hotel(hotelBerlin3).build();
                Room doubleRoomBerlin3 = Room.builder().roomType(RoomType.DOUBLE).pricePerNight(256).roomCount(25).img("double_room_berlin3.jpg").img2("double_room_berlin3_2.jpg").img3("double_room_berlin3_3.jpg").hotel(hotelBerlin3).build();
                Room singleRoomHCM1 = Room.builder().roomType(RoomType.SINGLE).pricePerNight(3000000).roomCount(20).img("single_room_hcm1.jpg").img2("single_room_hcm1_2.jpg").img3("single_room_hcm1_3.jpg").hotel(hotelHCM1).build();
                Room doubleRoomHCM1 = Room.builder().roomType(RoomType.DOUBLE).pricePerNight(5000000).roomCount(10).img("double_room_hcm1.jpg").img2("double_room_hcm1_2.jpg").img3("double_room_hcm1_3.jpg").hotel(hotelHCM1).build();
                Room singleRoomHCM2 = Room.builder().roomType(RoomType.SINGLE).pricePerNight(1500000).roomCount(30).img("single_room_hcm2.jpg").img2("single_room_hcm2_2.jpg").img3("single_room_hcm2_3.jpg").hotel(hotelHCM2).build();
                Room doubleRoomHCM2 = Room.builder().roomType(RoomType.DOUBLE).pricePerNight(3000000).roomCount(25).img("double_room_hcm2.jpg").img2("double_room_hcm2_2.jpg").img3("double_room_hcm2_3.jpg").hotel(hotelHCM2).build();
                Room singleRoomHCM3 = Room.builder().roomType(RoomType.SINGLE).pricePerNight(2000000).roomCount(15).img("single_room_hcm3.jpg").img2("single_room_hcm3_2.jpg").img3("single_room_hcm3_3.jpg").hotel(hotelHCM3).build();
                Room doubleRoomHCM3 = Room.builder().roomType(RoomType.DOUBLE).pricePerNight(3500000).roomCount(20).img("double_room_hcm3.jpg").img2("double_room_hcm3_2.jpg").img3("double_room_hcm3_3.jpg").hotel(hotelHCM3).build();


                hotelIst1.getRooms().addAll(Arrays.asList(singleRoomIst1,doubleRoomIst1));
                hotelIst2.getRooms().addAll(Arrays.asList(singleRoomIst2,doubleRoomIst2));
                hotelIst3.getRooms().addAll(Arrays.asList(singleRoomIst3,doubleRoomIst3));
                hotelBerlin1.getRooms().addAll(Arrays.asList(singleRoomBerlin1,doubleRoomBerlin1));
                hotelBerlin2.getRooms().addAll(Arrays.asList(singleRoomBerlin2,doubleRoomBerlin2));
                hotelBerlin3.getRooms().addAll(Arrays.asList(singleRoomBerlin3,doubleRoomBerlin3));
                

                hotelHCM1.getRooms().addAll(Arrays.asList(singleRoomHCM1, doubleRoomHCM1));
                hotelHCM2.getRooms().addAll(Arrays.asList(singleRoomHCM2, doubleRoomHCM2));
                hotelHCM3.getRooms().addAll(Arrays.asList(singleRoomHCM3, doubleRoomHCM3));
                hotelRepository.save(hotelIst1);
                hotelRepository.save(hotelIst2);
                hotelRepository.save(hotelIst3);
                hotelRepository.save(hotelBerlin1);
                hotelRepository.save(hotelBerlin2);
                hotelRepository.save(hotelBerlin3);
                hotelRepository.save(hotelHCM1);
                hotelRepository.save(hotelHCM2);
                hotelRepository.save(hotelHCM3);

                log.info("Hotel data persisted");

                Availability av1Berlin1 = Availability.builder().hotel(hotelBerlin1)
                        .date(LocalDate.of(2023,9,1)).room(singleRoomBerlin1).availableRooms(5).build();
                Availability av2Berlin1 = Availability.builder().hotel(hotelBerlin1)
                        .date(LocalDate.of(2023,9,2)).room(doubleRoomBerlin1).availableRooms(7).build();
                Availability av1HCM1 = Availability.builder().hotel(hotelHCM1)
                		.date(LocalDate.of(2024, 10, 1)).room(singleRoomHCM1).availableRooms(10).build();
                Availability av2HCM1 = Availability.builder().hotel(hotelHCM1)
                		.date(LocalDate.of(2024, 10, 2)).room(doubleRoomHCM1).availableRooms(8).build();

                availabilityRepository.save(av1HCM1);
                availabilityRepository.save(av2HCM1);


                availabilityRepository.save(av1Berlin1);
                availabilityRepository.save(av2Berlin1);
                log.info("Availability data persisted");

            } else {
                log.info("Test data persistence is not required");
            }
            log.warn("App ready");
        } catch (DataAccessException e) {
            log.error("Exception occurred during data persistence: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected exception occurred: " + e.getMessage());
        }
    }
}
