package com.saama.parse;

import java.io.IOException;
import java.util.List;

import tech.tablesaw.aggregate.AggregateFunction;
import tech.tablesaw.aggregate.AggregateFunctions;
import tech.tablesaw.aggregate.Summarizer;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.DateColumn;
import tech.tablesaw.api.NumberColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.columns.numbers.NumberColumnFormatter;

public class TableSaw {
	public static void main(String[] args) {
		try {
			Table table = Table.read().csv("C:\\Users\\sshende\\Desktop\\_user__201905221627.csv");
			
			
			
			/*System.err.println("column Count = "+table.columnCount());
			System.err.println(""+table.columnNames());*/
			
			/*// Add a column for the months from the date col so we can count by month
			StringColumn month = table.dateColumn("date").month();
			month.setName("month");
			table.addColumns(month);
			// perform the crossTab operation
			Table counts = table.xTabCounts("month", "who");
			// formatting 
			// make table print as integers with no decimals instead of the raw doubles it holds
			counts.columnsOfType(ColumnType.NUMBER)
			                .forEach(x -> ((NumberColumn)x).setPrintFormatter(NumberColumnFormatter.ints()));
			// print
			System.out.println(counts);*/
			
			//System.err.println(""+table.xTabCounts("first_name"));
			//System.err.println("xTabPercents ============== "+table.xTabPercents("first_name"));
			
		/*	Summarizer summerizer = table.summarize("salary", AggregateFunctions.mean);
			
			ColumnType[] columnTypes = table.columnTypes();*/
			
			
			/*List<String> columnNames = table.columnNames();
			for(int i=0;i<columnNames.size();i++) {
				System.err.println("columnNames ===> "+columnNames.get(i));
			}*/
			
			/*for(int i=0;i<columnTypes.length;i++) {
				System.err.println("column "+i+" "+columnTypes[i]);
			}
			System.err.println("column types ===> "+table.columnTypes());*/
			
			//System.err.println("**********  "+summerizer.apply());
			
			/*List<Column> columnOfType = table.columnsOfType(ColumnType.STRING);
						
			for(int i=0;i<columnOfType.size();i++) {
				System.err.println("column Of Type ===> "+columnOfType.get(i));
			}*/
			
			/*DateColumn[] dateColumns = table.dateColumns();
			
			for(int j=0;j<dateColumns.length;j++) {
				System.err.println("table datecolumns ==> "+dateColumns[j]);
			}*/
			

			Table whoCounts = table.xTabCounts("salary");
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
