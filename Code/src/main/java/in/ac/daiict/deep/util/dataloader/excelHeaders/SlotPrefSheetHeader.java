package in.ac.daiict.deep.util.dataloader.excelHeaders;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SlotPrefSheetHeader {
    public int STUDENT_ID = 0, SLOT_NO = 1, PREFERENCE_INDEX = 2;

    public SlotPrefSheetHeader(XSSFWorkbook outputWorkbook, XSSFSheet resultSheet) {
        Font headerFont = outputWorkbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);

        CellStyle headerStyle = outputWorkbook.createCellStyle();
        headerStyle.setFont(headerFont);

        Row row = resultSheet.createRow(0);
        Cell cell = row.createCell(STUDENT_ID);
        cell.setCellValue("Student ID");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(SLOT_NO);
        cell.setCellValue("Slot No");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(PREFERENCE_INDEX);
        cell.setCellValue("Preference Index");
        cell.setCellStyle(headerStyle);
    }
}
