package com.saama.parse;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

public class ParseSQL_old {

	public static void main(String[] args) throws JSQLParserException {

		//Statement stmt = CCJSqlParserUtil.parse("select max(salary),city from meta_schema.user group by city limit 100 ");
		//Statement stmt = CCJSqlParserUtil.parse("select max(salary),city from meta_schema.user having salary > 1000 limit 100");
		//Statement stmt = CCJSqlParserUtil.parse("select max(salary), avg(salary) from meta_schema.user group by city");
		//Statement stmt = CCJSqlParserUtil.parse("select max(salary), avg(salary) from meta_schema.user group by city");
		//Statement stmt = CCJSqlParserUtil.parse("select * from table1 where a = (select b from table2)");
		Statement stmt = CCJSqlParserUtil.parse("select * from meta_schema.user where salary = (select salary from meta_schema.user where first_name='Graciela')");
		
		Select selectStatement = (Select) stmt;

		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
		

		System.err.println("Table List ===> " + tableList);

		SelectBody selectBody = selectStatement.getSelectBody();

		System.err.println("selectBody ===> " + selectBody);

		System.err.println("" + selectBody.getClass().getName());

		PlainSelect plainSelect = (PlainSelect) selectBody;

		System.err.println("plainSelect.getFromItem() ==> " + plainSelect.getFromItem());
		
		System.err.println("plainSelect.getHaving() ==> " + plainSelect.getHaving());
		
		System.err.println("plainSelect.getLimit() ==> " + plainSelect.getLimit());
		
		System.err.println("plainSelect.getGroupByColumnReferences() ==> " + plainSelect.getGroupByColumnReferences());
		
		System.err.println("plainSelect.getSelectItems() ==> "+plainSelect.getSelectItems());
		
		System.err.println("plainSelect.getWhere() ===> "+plainSelect.getWhere());
			
		if (null != plainSelect.getWhere()) {
			String subSelectQuery = "";
			String whereClause = plainSelect.getWhere().toString();
			if (whereClause.contains("(")) {
				subSelectQuery = whereClause.substring(whereClause.indexOf("(") + 1, whereClause.indexOf(")"));

				System.err.println("-----------------------------------------------------------------");
				System.err.println("---------- " + subSelectQuery);

				Statement subQueryStmt = CCJSqlParserUtil.parse(subSelectQuery);
				Select subQuerySelectStmt = (Select) subQueryStmt;

				List<String> subQueryTableList = tablesNamesFinder.getTableList(subQuerySelectStmt);

				System.err.println("Sub Query Table List ===> " + subQueryTableList);

				SelectBody subQuerySelectBody = subQuerySelectStmt.getSelectBody();

				System.err.println("Sub Query selectBody ===> " + subQuerySelectBody);

				System.err.println("" + subQuerySelectBody.getClass().getName());

				PlainSelect subQueryPS = (PlainSelect) subQuerySelectBody;

				System.err.println("sub Query plainSelect.getHaving() ==> " + subQueryPS.getHaving());

				System.err.println("sub Query plainSelect.getLimit() ==> " + subQueryPS.getLimit());

				System.err.println("sub Query plainSelect.getGroupByColumnReferences() ==> "
						+ subQueryPS.getGroupByColumnReferences());

				System.err.println("sub Query plainSelect.getSelectItems() ==> " + subQueryPS.getSelectItems());

				System.err.println("sub Query plainSelect.getWhere() ===> " + subQueryPS.getWhere());

			}
		}

	}
}
