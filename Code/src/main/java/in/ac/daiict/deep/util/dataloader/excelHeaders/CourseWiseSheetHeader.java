package in.ac.daiict.deep.util.dataloader.excelHeaders;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class CourseWiseSheetHeader {
    public int STUDENT_ID = 0, STUDENT_NAME = 1;

    public CourseWiseSheetHeader(XSSFWorkbook outputWorkbook, XSSFSheet resultSheet) {
        Font headerFont = outputWorkbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);

        CellStyle headerStyle = outputWorkbook.createCellStyle();
        headerStyle.setFont(headerFont);

        Row row = resultSheet.createRow(0);
        Cell cell = row.createCell(STUDENT_ID);
        cell.setCellValue("Student ID");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(STUDENT_NAME);
        cell.setCellValue("Student Name");
        cell.setCellStyle(headerStyle);
    }
}