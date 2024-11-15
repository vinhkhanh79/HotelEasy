package com.datn.tourhotel.service;

import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.datn.tourhotel.model.dto.BookingDTO;
import com.datn.tourhotel.model.dto.HotelDTO;
import com.datn.tourhotel.model.dto.UserDTO;

import jakarta.servlet.http.HttpServletResponse;

public interface ExcelExportService {
	
	void exportHotelsToExcel(HttpServletResponse response, List<HotelDTO> hotelList) throws IOException;
	
	void exportBookingsToExcel(HttpServletResponse response, List<BookingDTO> bookings) throws IOException;
	
	void exportUsersToExcel(HttpServletResponse response, List<UserDTO> users) throws IOException;
	
	CellStyle createHeaderCellStyle(XSSFWorkbook workbook);
}
