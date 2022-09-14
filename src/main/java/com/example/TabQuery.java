package com.example;

import static tech.tablesaw.aggregate.AggregateFunctions.mean;
import static tech.tablesaw.aggregate.AggregateFunctions.sum;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.style.Styler.ChartTheme;

import tech.tablesaw.api.Table;

public class TabQuery {

	public static void main(String[] args) {
		Table t = Table.read().file("/home/abey/Downloads/Test/JavaTable-Gavin/demo/src/main/java/com/example/sales.csv");

		// Row Count
		System.out.println("Row Count: " + t.rowCount());

		// Column Count
		System.out.println("Column Count:" + t.columnCount());

		// Column Names
		System.out.println(t.columnNames());

		// print the first 10 rows
		System.out.println(t.print(10));

		// print 10 rows with seleted columns
		System.out.println(t.selectColumns("Region", "Order Date",
				"Item Type", "Units Sold",
				"Total Profit").print(10));

		// Total number of items sold in each region
		var cols = t.selectColumns("Region", "Order Date", "Item Type", "Units Sold");
		var items = cols.summarize("Units Sold", sum).by("Region", "Item Type")
				.sortOn("Region", "Item Type");
		System.out.println(items.printAll());

		// The top 5 highest selling items around the world
		var top5 = cols.summarize("Units Sold", sum).by("Item Type")
				.sortDescendingOn("Sum [Units Sold]").first(5);
		System.out.println(top5.printAll());

		// The average number of items sold in each region
		var avg = cols.summarize("Units Sold", sum).by("Item Type", "Region")
				.summarize("Sum [Units Sold]", mean).by("Region");
		System.out.println(avg.printAll());

		// Print all the items ordered between the 10th of September 2012 and 10th of
		// October 2012
		var formatter = DateTimeFormatter.ofPattern("M/dd/yyyy");
		var from = LocalDate.parse("9/10/2012", formatter);
		formatter = DateTimeFormatter.ofPattern("M/dd/yyyy");
		var till = LocalDate.parse("10/10/2012", formatter);
		var drange = cols.dateColumn("Order Date").isBetweenIncluding(from, till);

		var res = cols.where(drange);

		System.out.println(res.print());

		// Plotting the top five items on a pie chart
		PieChart pie = new PieChartBuilder()
				.title("Top 5 Countries")
				.build();
		for (int i = 0; i < top5.rowCount(); i++) {
			pie.addSeries(top5.getString(i, 0), (double) top5.get(i, 1));
		}

		new SwingWrapper<>(pie).displayChart();

		// Plotting a bar chart with total items sold at multiple regions
		CategoryChart chart = new CategoryChartBuilder()
				.width(800).height(600)
				.title("Items sold in various regions")
				.xAxisTitle("Items")
				.yAxisTitle("No of items sold (x10000)")
				.theme(ChartTheme.GGPlot2).build();

		var reg = "";
		ArrayList<String> ite = new ArrayList<>();
		ArrayList<Number> num = new ArrayList<>();

		for (int i = 0; i < items.rowCount(); i++) {

			if (i == 0) {
				reg = items.getString(i, "Region");
			} else if (!(reg.equals(items.getString(i, "Region")))) {

				chart.addSeries(reg, ite, num);
				ite = new ArrayList<>();
				num = new ArrayList<>();
				reg = items.getString(i, "Region");
			}

			ite.add(items.getString(i, 1));
			num.add(Integer.parseInt(items.getString(i, 2)) / 10000);

			if (i == items.rowCount() - 1) {
				chart.addSeries(reg, ite, num);
			}

		}

		new SwingWrapper<CategoryChart>(chart).displayChart();
	}
}