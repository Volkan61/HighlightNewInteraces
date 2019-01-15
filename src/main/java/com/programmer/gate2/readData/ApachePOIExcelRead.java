package com.programmer.gate2.readData;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ApachePOIExcelRead {

  private static final String FILE_NAME = "/tmp/MyFirstExcel.xlsx";

  /**
   * @param args
   */
  /**
   * @param args
   */
  public static void main(String[] args) {

    String path = "excel/2019-01-04_Gesamtreleaseletter_2019-1-R.xlsm";
    List<String> column = ApachePOIExcelRead.getColumn(path, 8);

    for (int i = 0; i < column.size(); i++) {
      String currentColumn = column.get(i);
      System.out.println(currentColumn);
    }
    System.out.println("end");

  }

  public static List<String> getColumn(String path, int columnIndex) {

    LinkedList<String> output = new LinkedList<String>();

    try {
      FileInputStream file = new FileInputStream(new File(path));

      // Create Workbook instance holding reference to .xlsx file
      XSSFWorkbook workbook = new XSSFWorkbook(file);

      // Get first/desired sheet from the workbook
      XSSFSheet sheet = workbook.getSheetAt(1);

      for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
        Row row = sheet.getRow(rowIndex);
        if (row != null) {
          Cell cell = row.getCell(8);
          if (cell != null) {
            // Found column and there is value in the cell.
            String cellValueMaybeNull = cell.getStringCellValue();
            // System.out.println(cellValueMaybeNull);

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
