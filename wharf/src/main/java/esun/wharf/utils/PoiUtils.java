package esun.wharf.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解析Excel工具类
 */
public class PoiUtils {

    /**
     * 得到单元格数据
     *
     * @param cell
     * @return
     */
    public static Object getCellValue(Cell cell) {
        Object data = "";
        if(cell == null){
            return null;
        }
        int cellType = cell.getCellType();
        if (cellType == Cell.CELL_TYPE_NUMERIC) {
            data = cell.getNumericCellValue();
        }
        if (cellType == Cell.CELL_TYPE_STRING) {
            data = cell.getStringCellValue();
        }
        return data;
    }

    /**
     * 得到单元格数据字符串
     *
     * @param cell
     * @return
     */
    public static String getCellValueString(Cell cell) {
        Object data = "";
        if(cell == null){
            return null;
        }
        int cellType = cell.getCellType();
        if (cellType == Cell.CELL_TYPE_NUMERIC) {
            data = cell.getNumericCellValue();
        }
        if (cellType == Cell.CELL_TYPE_STRING) {
            data = cell.getStringCellValue();
        }
        return data.toString();
    }

    /**
     * 得到单行数据
     *
     * @param row
     * @return
     */
    public static List<Object> getRowData(Row row) {
        List<Object> result = new ArrayList<>();
        for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
            Cell cell = row.getCell(i);
            Object value = getCellValue(cell);
            result.add(value);
        }
        return result;
    }

    /**
     * 按照指定列数得到单行数据
     *
     * @param row
     * @return
     */
    public static List<Object> getRowData(Row row, int cellSize) {
        List<Object> result = new ArrayList<>();
        for (int i = 0; i < cellSize; i++) {
            Cell cell = row.getCell(i);
            if (cell == null) {
                result.add("");
                continue;
            }
            Object value = getCellValue(cell);
            result.add(value);
        }
        return result;
    }

    /**
     * 获取表头行所有数据
     *
     * @param row
     * @return
     */
    public static List<String> getTitleList(Row row) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            String value = getCellValue(cell).toString();
            result.add(value);
        }
        return result;
    }

    /**
     * 得到单行数据,并与标题行对应
     *
     * @param row
     * @param titleList
     * @return
     */
    public static Map<String, Object> getRowData(Row row, List<String> titleList) {
        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < titleList.size(); i++) {
            String title = titleList.get(i);
            Cell cell = row.getCell(i);
            String value = getCellValueString(cell);
            result.put(title, value);
        }
        return result;
    }

    /**
     * 获取指定行以下数据(按指定列长度取数据，没有数据的格子为空值)
     *
     * @param sheet      数据
     * @param startIndex 开始行数索引
     * @return
     */
    public static List<List<Object>> getAllData(Sheet sheet, int startIndex, int cellSize) {
        List<List<Object>> result = new ArrayList<>();
        for (int i = startIndex; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            result.add(getRowData(row, cellSize));
        }
        return result;
    }

    /**
     * 获取指定行以下数据
     *
     * @param sheet      数据
     * @param startIndex 开始行数索引
     * @return
     */
    public static List<List<Object>> getAllData(Sheet sheet, int startIndex) {
        List<List<Object>> result = new ArrayList<>();
        for (int i = startIndex; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            result.add(getRowData(row));
        }
        return result;
    }

    /**
     * 获得指定行以下数据,并与标题行对应
     *
     * @param sheet
     * @param startIndex
     * @param titleList
     * @return
     */
    public static List<Map<String, Object>> getAllData(Sheet sheet, int startIndex, List<String> titleList) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = startIndex; i <= sheet.getLastRowNum(); i++) {
            Map<String, Object> rowData = new HashMap<>();
            Row row = sheet.getRow(i);
            List<Object> rowDataList = getRowData(row,titleList.size());
            for (int j = 0; j < titleList.size(); j++) {
                String title = titleList.get(j);
                Object data = rowDataList.get(j);
                rowData.put(title, data);
            }
            result.add(rowData);
        }
        return result;
    }

    /**
     * 获得总行数
     *
     * @param sheet
     * @return
     */
    public static int getRowSize(Sheet sheet, int startIndex) {
        int rowSize = sheet.getLastRowNum() + 1 - startIndex;
        return rowSize;
    }

    /**
     * 获得总列数
     *
     * @param row
     * @return
     */
    public static int getCellSize(Row row) {
        int cellSize = row.getLastCellNum();
        return cellSize;
    }

    /**
     * 获取行对象
     *
     * @param sheet
     * @param rowIndex
     * @return
     */
    public static Row getRow(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        return row;
    }

    /**
     * 获得列对象
     *
     * @param sheet
     * @param rowIndex
     * @param cellIndex
     * @return
     */
    public static Cell getCell(Sheet sheet, int rowIndex, int cellIndex) {
        Cell cell = sheet.getRow(rowIndex).getCell(cellIndex);
        return cell;
    }
}
