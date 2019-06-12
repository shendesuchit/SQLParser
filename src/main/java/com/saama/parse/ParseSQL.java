package com.saama.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class ParseSQL {
	
	public static String[] aggregateArray = {"sum","avg","max","min","count"};

	public static void main(String[] args) throws JSQLParserException {

		String sql = "select count(salary) as sal_count,city from meta_schema.user group by city order by sal_count asc limit 100";
		//String sql = "select salary,first_name,lastName from meta_schema.user where first_name='James'";
		//String sql = "select count(*) from meta_schema.user where salary = (select salary from meta_schema.user where first_name='James')";
		//String sql ="select * from meta_schema.user where salary = (select salary from meta_schema.user where first_name = (select first_name from meta_schema.user where last_name='Rim'))";
		
		Map<String, Object> parsedResult = getParsedResult(sql);
		
		System.err.println("------ Main Query Information ------ ");
		System.err.println(" Main Table ===> "+parsedResult.get("tableList"));
		System.err.println(" Main select Item ===> "+parsedResult.get("selectItem"));
		System.err.println(" Main From Item ===> "+parsedResult.get("fromItem"));
		System.err.println(" Main Having Item  ===> "+parsedResult.get("havingItem"));
		System.err.println(" Main Limit Item ===> "+parsedResult.get("limitItem"));
		System.err.println(" Main Group By Item ===> "+parsedResult.get("groupByItem"));
		System.err.println(" Main where Table ===> "+parsedResult.get("whereItem"));
		System.err.println(" Order By where Table ===> "+parsedResult.get("orderByItem"));

		
		if(null != parsedResult.get("whereItem")) {
			int numberOfQueries =  (int) parsedResult.get("numberOfSubQueries");
			String havingSubQuery = parsedResult.get("havingSubQuery").toString();
			if(numberOfQueries==1 && havingSubQuery.equalsIgnoreCase("Yes")) {
				numberOfQueries = numberOfQueries +1;
			}
			for (int counter = 1; counter < numberOfQueries; counter++) {
				System.err.println("------ Sub Query Information ------ ");
				System.err.println(" Sub Table ===> "+parsedResult.get("subQuery_"+counter+"_tableList"));
				System.err.println(" Sub select Item ===> "+parsedResult.get("subQuery_"+counter+"_selectItem"));
				System.err.println(" Sub From Item ===> "+parsedResult.get("subQuery_"+counter+"_fromItem"));
				System.err.println(" Sub Having Item  ===> "+parsedResult.get("subQuery_"+counter+"_havingItem"));
				System.err.println(" Sub Limit Item ===> "+parsedResult.get("subQuery_"+counter+"_limitItem"));
				System.err.println(" Sub Group By Item ===> "+parsedResult.get("subQuery_"+counter+"_groupByItem"));
				System.err.println(" Sub where Table ===> "+parsedResult.get("subQuery_"+counter+"_whereItem"));
			}
		}
	}

	/**
	 * @throws JSQLParserException
	 */
	public static Map<String,Object> getParsedResult(String sql) throws JSQLParserException {
		
		Map<String,Object> returnMap = new HashMap<String,Object>();
		
		Statement stmt = CCJSqlParserUtil.parse(sql);
		Select selectStatement = (Select) stmt;
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
		SelectBody selectBody = selectStatement.getSelectBody();
		PlainSelect plainSelect = (PlainSelect) selectBody;

		returnMap.put("tableList", tableList != null ? tableList.toString() : null);
		returnMap.put("selectBody", selectBody != null ? selectBody.toString() : null);
		returnMap.put("fromItem", plainSelect.getFromItem() != null ? plainSelect.getFromItem().toString() : plainSelect.getFromItem());
		returnMap.put("havingItem", plainSelect.getHaving() != null ? plainSelect.getHaving() : plainSelect.getHaving());
		returnMap.put("limitItem", plainSelect.getLimit() != null ? plainSelect.getLimit().toString() : plainSelect.getLimit());
		returnMap.put("groupByItem", plainSelect.getGroupByColumnReferences() != null ? plainSelect.getGroupByColumnReferences().toString()
						: plainSelect.getGroupByColumnReferences());
		returnMap.put("selectItem", plainSelect.getSelectItems() != null ? plainSelect.getSelectItems().toString() : plainSelect.getSelectItems());
		returnMap.put("whereItem", plainSelect.getWhere() != null ? plainSelect.getWhere().toString() : plainSelect.getWhere());
		returnMap.put("orderByItem", plainSelect.getOrderByElements() != null ? plainSelect.getOrderByElements().toString() : plainSelect.getOrderByElements());
		returnMap.put("containsAggregate", checkForAggregateFunction(sql.toLowerCase()));
		
		if (null != plainSelect.getWhere()) {
			String whereClause = plainSelect.getWhere().toString();
			
			long numberOfSubQueries = Pattern.compile("select".toLowerCase()).splitAsStream(whereClause.toLowerCase()).count()-1;
			
			if(numberOfSubQueries>1) {
				for(int i=1;i<=numberOfSubQueries;i++) {
					returnMap = setSubQueryDetails(sql,i,numberOfSubQueries,returnMap, tablesNamesFinder, whereClause);
				}
			}else {
				returnMap = setSubQueryDetails(sql,1,numberOfSubQueries,returnMap, tablesNamesFinder, whereClause);
			}
		}
		return returnMap;
	}

	/**
	 * @param returnMap
	 * @param tablesNamesFinder
	 * @param whereClause
	 * @return 
	 * @throws JSQLParserException
	 */
	private static Map<String, Object> setSubQueryDetails(String sql, int counter, long numberOfSubQueries,Map<String, Object> returnMap, TablesNamesFinder tablesNamesFinder,
			String whereClause) throws JSQLParserException {
		
		PlainSelect subQueryPS = new PlainSelect();
		String subSelectQuery = "";
		Statement subQueryStmt;
		if(whereClause.contains("(")){
			if(numberOfSubQueries>1) {
				subSelectQuery = whereClause.substring(whereClause.indexOf("(") + 1, whereClause.indexOf(")")+1);
			}else {
				subSelectQuery = whereClause.substring(whereClause.indexOf("(") + 1, whereClause.indexOf(")"));
			}
			subQueryStmt = CCJSqlParserUtil.parse(subSelectQuery);
			returnMap.put("havingSubQuery", "Yes");
		}else {
			subQueryStmt = CCJSqlParserUtil.parse(sql);
			returnMap.put("havingSubQuery", "No");
		}
		Select subQuerySelectStmt = (Select) subQueryStmt;
		List<String> subQueryTableList = tablesNamesFinder.getTableList(subQuerySelectStmt);
		SelectBody subQuerySelectBody = subQuerySelectStmt.getSelectBody();
		subQueryPS = (PlainSelect) subQuerySelectBody;
			
		if(counter==1) {
			
			returnMap.put("subQuery_" + counter + "_tableList", subQueryTableList.toString() != null ? subQueryTableList.toString() : subQueryTableList);
			returnMap.put("subQuery_" + counter + "_selectBody", subQuerySelectBody != null ? subQuerySelectBody.toString() : subQuerySelectBody);
			returnMap.put("subQuery_" + counter + "_fromItem", subQueryPS.getFromItem() != null ? subQueryPS.getFromItem().toString() : subQueryPS.getFromItem());
			returnMap.put("subQuery_" + counter + "_havingItem", subQueryPS.getHaving() != null ? subQueryPS.getHaving().toString() : subQueryPS.getHaving());
			returnMap.put("subQuery_" + counter + "_limitItem", subQueryPS.getLimit() != null ? subQueryPS.getLimit().toString() : subQueryPS.getLimit());
			returnMap.put("subQuery_" + counter + "_groupByItem", subQueryPS.getGroupByColumnReferences() != null ? subQueryPS.getGroupByColumnReferences().toString()
							: subQueryPS.getGroupByColumnReferences());
			returnMap.put("subQuery_" + counter + "_selectItem", subQueryPS.getSelectItems() != null ? subQueryPS.getSelectItems().toString()
							: subQueryPS.getSelectItems());
			returnMap.put("subQuery_" + counter + "_whereItem",subQueryPS.getWhere() != null ? subQueryPS.getWhere().toString() : subQueryPS.getWhere());
			returnMap.put("subQuery_" + counter + "_orderByItem",subQueryPS.getOrderByElements() != null ? subQueryPS.getOrderByElements().toString() : subQueryPS.getOrderByElements());
		}
		
		
		if(numberOfSubQueries>1) {
			String subWherClause = subQueryPS.getWhere().toString().toLowerCase();
			String subSubSelectQuery = subWherClause.substring(subWherClause.indexOf("(") + 1, subWherClause.indexOf(")"));
			Statement subSubQueryStmt = CCJSqlParserUtil.parse(subSubSelectQuery);
			Select subSubQuerySelectStmt = (Select) subSubQueryStmt;
			List<String> subSubQueryTableList = tablesNamesFinder.getTableList(subSubQuerySelectStmt);
			SelectBody subSubQuerySelectBody = subSubQuerySelectStmt.getSelectBody();
			PlainSelect subSubQueryPS = (PlainSelect) subSubQuerySelectBody;

			counter = counter+1;
			returnMap.put("subQuery_"+counter+"_tableList", subSubQueryTableList);
			returnMap.put("subQuery_"+counter+"_selectBody", subSubQuerySelectBody);
			returnMap.put("subQuery_"+counter+"_fromItem", subSubQueryPS.getFromItem());
			returnMap.put("subQuery_"+counter+"_havingItem", subSubQueryPS.getHaving());
			returnMap.put("subQuery_"+counter+"_limitItem", subSubQueryPS.getLimit());
			returnMap.put("subQuery_"+counter+"_groupByItem", subSubQueryPS.getGroupByColumnReferences());
			returnMap.put("subQuery_"+counter+"_selectItem", subSubQueryPS.getSelectItems());
			returnMap.put("subQuery_"+counter+"_whereItem",subSubQueryPS.getWhere());
			
		}
		returnMap.put("numberOfSubQueries", counter);
		
		return returnMap;
	}
	
	public static ArrayList<String> checkForAggregateFunction(String query) {
		ArrayList<String> retList = new ArrayList<String>();
		for (int i = 0; i < aggregateArray.length; i++) {
			if (query.contains(aggregateArray[i])) {
				System.err.println("aggregateArray[i] =====> "+aggregateArray[i]);
				retList.add(aggregateArray[i]);
			}
		}
		return retList;
	}
}
