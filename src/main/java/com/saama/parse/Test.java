package com.saama.parse;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Test {
	private static FileInputStream inputFile;
	private static XSSFWorkbook workBook;
	private static Test testObject;
	private static String path;
	static {
		testObject = new Test();
	}

	private Test() {
		path = "C:\\Users\\sshende\\Desktop\\cd.xlsx";
	}

	public static Test getInstance() {
		return testObject;
	}

	private XSSFSheet readTab(String path, String sheet) throws IOException {
		inputFile = new FileInputStream(path);
		workBook = new XSSFWorkbook(inputFile);
		return workBook.getSheet(sheet);
	}

	private HashMap<String, ArrayList<ArrayList<String>>> mapPopulator(XSSFSheet xs, int keyColumnIndex) {
		HashMap<String, ArrayList<ArrayList<String>>> tableMap = new HashMap<>();

		for (Row row : xs) {
			if (row.getRowNum() > 0) {

				ArrayList<String> columns = new ArrayList<>();
				int columnCounter = keyColumnIndex;
				while (columnCounter >= 0) {
					columns.add(0, row.getCell(columnCounter).toString());
					columnCounter--;
				}
				ArrayList<String> entireRow = new ArrayList<>(columns);
				if (tableMap.containsKey(row.getCell(keyColumnIndex).toString()))
					tableMap.get(row.getCell(keyColumnIndex).toString()).add(entireRow);
				else {
					ArrayList<ArrayList<String>> rowsList = new ArrayList<>();
					rowsList.add(entireRow);
					tableMap.put(row.getCell(keyColumnIndex).toString(), rowsList);
				}
			}
		}
		return tableMap;
	}

	private HashMap<String, ArrayList<ArrayList<String>>> applyJoin(
			HashMap<String, ArrayList<ArrayList<String>>> targetTable,
			HashMap<String, ArrayList<ArrayList<String>>> sourceMap, int targetKeyColumnIndex) {
		HashMap<String, ArrayList<ArrayList<String>>> joinTableMap = new HashMap<>();
		ArrayList<ArrayList<String>> modifiedTarget = new ArrayList<>();
		for (String key : sourceMap.keySet()) {
			if (targetTable.containsKey(key)) {
				for (ArrayList<String> value : targetTable.get(key)) {
					value.remove(targetKeyColumnIndex);
					for (ArrayList<String> val : sourceMap.get(key)) {
						val.forEach(cell -> value.add(cell));
					}
					modifiedTarget.add(value);
				}

				joinTableMap.put(key, modifiedTarget);
			}
		}
		System.out.println("modifiedTarget ===> " + modifiedTarget);
		return joinTableMap;
	}

	public static void main(String[] args) throws IOException {
		Test t1 = getInstance();
		XSSFSheet xs1 = t1.readTab(path, "Sheet1");
		HashMap<String, ArrayList<ArrayList<String>>> targetMap = t1.mapPopulator(xs1, 2);
		System.out.println("targetMap before ===> "+targetMap);
		XSSFSheet xs2 = t1.readTab(path, "Sheet2");
		HashMap<String, ArrayList<ArrayList<String>>> sourceMap = t1.mapPopulator(xs2, 1);
		HashMap<String, ArrayList<ArrayList<String>>> joinedMap = t1.applyJoin(targetMap, sourceMap, 2);
		System.out.println("targetMap ===> "+targetMap);
		System.out.println("sourceMap ===> "+sourceMap);
		System.out.println("joinedMap ===> "+joinedMap);

	}

}