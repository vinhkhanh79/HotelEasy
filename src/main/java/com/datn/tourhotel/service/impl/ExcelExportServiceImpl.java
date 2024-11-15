package com.datn.tourhotel.service.impl;

import com.datn.tourhotel.model.dto.BookingDTO;
import com.datn.tourhotel.model.dto.HotelDTO;
import com.datn.tourhotel.model.dto.UserDTO;
import com.datn.tourhotel.service.ExcelExportService;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ExcelExportServiceImpl implements ExcelExportService {

    public void exportHotelsToExcel(HttpServletResponse response, List<HotelDTO> hotelList) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Hotels");

        // Create Header Row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Name", "Single Room Price", "Double Room Price"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(createHeaderCellStyle(workbook));
        }

        // Populate Data Rows
        int rowNum = 1;
        for (HotelDTO hotel : hotelList) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(hotel.getId());
            row.createCell(1).setCellValue(hotel.getName());

            hotel.getRoomDTOs().forEach(room -> {
                if (room.getRoomType().name().equals("SINGLE")) {
                    row.createCell(2).setCellValue(room.getPricePerNight().toString() + " VND");
                } else if (room.getRoomType().name().equals("DOUBLE")) {
                    row.createCell(3).setCellValue(room.getPricePerNight().toString() + " VND");
                }
            });
        }

        // Adjust Column Widths
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Set the content type and header for the response
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=hotels.xlsx";
        response.setHeader(headerKey, headerValue);

        // Write Excel file to response
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
    
    public void exportBookingsToExcel(HttpServletResponse response, List<BookingDTO> bookings) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Bookings");

        // Create Header Row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Hotel", "Confirmation No.", "Customer", "Total Price", "Payment Status", "Booking Status"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(createHeaderCellStyle(workbook));
        }

        // Populate Data Rows
        int rowNum = 1;
        for (BookingDTO booking : bookings) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(booking.getHotelName());
            row.createCell(1).setCellValue(booking.getConfirmationNumber());
            row.createCell(2).setCellValue(booking.getCustomerName());
            row.createCell(3).setCellValue(booking.getTotalPrice().toString());
            row.createCell(4).setCellValue(booking.getPaymentStatus().toString());
            row.createCell(5).setCellValue(booking.getStatus().toString());
        }

        // Adjust Column Widths
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Set the content type and header for the response
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=bookings.xlsx";
        response.setHeader(headerKey, headerValue);

        // Write Excel file to response
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
    
    public void exportUsersToExcel(HttpServletResponse response, List<UserDTO> users) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        // Create Header Row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Username", "Name", "Last Name", "Role"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(createHeaderCellStyle(workbook));
        }

        // Populate Data Rows
        int rowNum = 1;
        for (UserDTO user : users) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(user.getId());
            row.createCell(1).setCellValue(user.getUsername());
            row.createCell(2).setCellValue(user.getName());
            row.createCell(3).setCellValue(user.getLastName().toString());
            row.createCell(4).setCellValue(user.getRole().toString());
        }

        // Adjust Column Widths
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Set the content type and header for the response
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=users.xlsx";
        response.setHeader(headerKey, headerValue);

        // Write Excel file to response
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    public CellStyle createHeaderCellStyle(XSSFWorkbook workbook) {
        CellStyle headerCellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerCellStyle.setFont(font);
        return headerCellStyle;
    }
}
