package in.ac.daiict.deep.util.dataloader.excelHeaders;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.util.Iterator;

public class StudentSheetHeader {
    public int STUDENT_ID = -1, NAME = -1, SEMESTER = -1, PROGRAM = -1;

    public StudentSheetHeader(Row headerRow) {
        Iterator<Cell> headerCells = headerRow.cellIterator();
        while (headerCells.hasNext()) {
            Cell headerCell = headerCells.next();
            String cellValue = "";
            if (headerCell.getCellType() != CellType.STRING)
                System.out.println("Please give the headers in the sheet!");
            else {
                cellValue = headerCell.getStringCellValue();
                if (cellValue.equalsIgnoreCase("StudentID")) STUDENT_ID = headerCell.getColumnIndex();
                else if (cellValue.equalsIgnoreCase("Name")) NAME = headerCell.getColumnIndex();
                else if (cellValue.equalsIgnoreCase("Program")) PROGRAM = headerCell.getColumnIndex();
                else if (cellValue.equalsIgnoreCase("SEMESTER")) SEMESTER = headerCell.getColumnIndex();
            }
        }
    }
}
