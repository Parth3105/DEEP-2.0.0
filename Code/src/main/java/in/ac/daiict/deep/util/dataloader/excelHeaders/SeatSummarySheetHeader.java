package in.ac.daiict.deep.util.dataloader.excelHeaders;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SeatSummarySheetHeader {
    public int COURSE_ID = 0, COURSE_NAME = 1, PROGRAM = 2, SEMESTER = 3, CATEGORY = 4, AVAILABLE_SEATS = 5;

    public SeatSummarySheetHeader(XSSFWorkbook outputWorkbook, XSSFSheet seatSummarySheet) {
        Font headerFont = outputWorkbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);

        CellStyle headerStyle = outputWorkbook.createCellStyle();
        headerStyle.setFont(headerFont);

        Row row = seatSummarySheet.createRow(0);
        Cell cell = row.createCell(COURSE_ID);
        cell.setCellValue("Course ID");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(COURSE_NAME);
        cell.setCellValue("Course Name");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(PROGRAM);
        cell.setCellValue("Program");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(SEMESTER);
        cell.setCellValue("Semester");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(CATEGORY);
        cell.setCellValue("Category");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(AVAILABLE_SEATS);
        cell.setCellValue("Available Seats");
        cell.setCellStyle(headerStyle);
    }
}
