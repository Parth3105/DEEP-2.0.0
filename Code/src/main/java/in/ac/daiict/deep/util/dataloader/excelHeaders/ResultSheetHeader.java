package in.ac.daiict.deep.util.dataloader.excelHeaders;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ResultSheetHeader {
    public int STUDENT_ID = 0, PROGRAM = 1, SEMESTER = 2, COURSE_ID = 3, COURSE_NAME = 4, CATEGORY = 5, SLOT = 6, PRIORITY = 7, CUMULATIVE_PRIORITY = 8;

    public ResultSheetHeader(XSSFWorkbook outputWorkbook, XSSFSheet resultSheet) {
        Font headerFont = outputWorkbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);

        CellStyle headerStyle = outputWorkbook.createCellStyle();
        headerStyle.setFont(headerFont);

        Row row = resultSheet.createRow(0);
        Cell cell = row.createCell(STUDENT_ID);
        cell.setCellValue("Student ID");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(PROGRAM);
        cell.setCellValue("Program");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(SEMESTER);
        cell.setCellValue("Semester");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(COURSE_ID);
        cell.setCellValue("Course ID");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(COURSE_NAME);
        cell.setCellValue("Course Name");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(CATEGORY);
        cell.setCellValue("Category");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(SLOT);
        cell.setCellValue("Slot");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(PRIORITY);
        cell.setCellValue("Priority");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(CUMULATIVE_PRIORITY);
        cell.setCellValue("Cumulative Priority");
        cell.setCellStyle(headerStyle);

    }
}
