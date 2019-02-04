package updatedinterfacesvis.poi;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author vhacimuf
 *
 */
public class ParseExcel {

  public static List<String> parse(int pSheet, String path, int columnIndex) {

    LinkedList<String> output = new LinkedList<String>();

    try {
      FileInputStream file = new FileInputStream(new File(path));

      // Create Workbook instance holding reference to .xlsx file
      XSSFWorkbook workbook = new XSSFWorkbook(file);

      // Get first/desired sheet from the workbook
      XSSFSheet sheet = workbook.getSheetAt(pSheet);

      for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
        Row row = sheet.getRow(rowIndex);
        if (row != null) {
          Cell cell = row.getCell(columnIndex);
          if (cell != null) {
            // Found column and there is value in the cell.
            String cellValueMaybeNull = cell.getStringCellValue();

            output.add(cellValueMaybeNull);
            // Do something with the cellValueMaybeNull here ...
          }
        }
      }

      // Iterate through each rows one by one
      file.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return output;
  }

}
