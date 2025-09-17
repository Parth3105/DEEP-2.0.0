package in.ac.daiict.deep.util.dataloader.excelHeaders;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.util.Iterator;

public class CourseSheetHeader {
    public int COURSE_ID = -1, COURSE_NAME = -1, CREDITS = -1, SLOT = -1;

    public CourseSheetHeader(Row headerRow) {
        Iterator<Cell> headerCells = headerRow.cellIterator();
        while (headerCells.hasNext()) {
            Cell headerCell = headerCells.next();
            String cellValue = "";
            if (headerCell.getCellType() != CellType.STRING)
                System.out.println("Please give the headers in the sheet!");
            else {
                cellValue = headerCell.getStringCellValue();
                if (cellValue.equalsIgnoreCase("CourseID")) COURSE_ID = headerCell.getColumnIndex();
                else if (cellValue.equalsIgnoreCase("CourseName")) COURSE_NAME = headerCell.getColumnIndex();
                else if (cellValue.equalsIgnoreCase("Credits")) CREDITS = headerCell.getColumnIndex();
                else if (cellValue.equalsIgnoreCase("Slot")) SLOT = headerCell.getColumnIndex();
            }
        }
    }
}
