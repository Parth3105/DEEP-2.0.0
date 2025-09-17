package in.ac.daiict.deep.util.dataloader.excelHeaders;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.util.Iterator;

public class CourseOfferSheetHeader {
    public int COURSE_ID = -1, PROGRAM = -1, SEMESTER = -1, CATEGORY = -1, SEATS = -1;

    public CourseOfferSheetHeader(Row headerRow) {
        Iterator<Cell> headerCells = headerRow.cellIterator();
        while (headerCells.hasNext()) {
            Cell headerCell = headerCells.next();
            String cellValue = "";
            if (headerCell.getCellType() != CellType.STRING)
                System.out.println("Please give the headers in the sheet!");
            else {
                cellValue = headerCell.getStringCellValue();
                if (cellValue.equalsIgnoreCase("CourseID")) COURSE_ID = headerCell.getColumnIndex();
                else if (cellValue.equalsIgnoreCase("Program")) PROGRAM = headerCell.getColumnIndex();
                else if (cellValue.equalsIgnoreCase("Semester")) SEMESTER = headerCell.getColumnIndex();
                else if (cellValue.equalsIgnoreCase("Category")) CATEGORY = headerCell.getColumnIndex();
                else if (cellValue.equalsIgnoreCase("Seats")) SEATS = headerCell.getColumnIndex();
            }
        }
    }
}
