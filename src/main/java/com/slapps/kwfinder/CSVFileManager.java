/**
 * 
 */
package com.slapps.kwfinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import com.google.common.base.Throwables;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

/**
 * Class to hold all functionality related to managing CSV file.
 * 
 * The input is coming in CSV file and we are also updating each
 * CSV record with result column.
 * 
 * @author Muthukumaran
 * 
 * @see https://www.javainterviewpoint.com/csvtobean-and-beantocsv-example-using-opencsv/ 
 * 
 */
public final class CSVFileManager {

	private static String fqcn = CSVFileManager.class.getName();
    private  final String[] HEADER = new String[]{"url", "keyword", "textToExport"};
	
	private File csvFile;
	
	public CSVFileManager(File file) {
		this.csvFile = file;
	}
	
	public File getCSVFile() {
		return this.csvFile;
	}
	
	public List<KWData> parse() throws IllegalStateException, IOException {
		FileReader fileReader = new FileReader(csvFile);
		List<KWData> data = new CsvToBeanBuilder<KWData>(fileReader)
								.withType(KWData.class).withSkipLines(0).
								withSeparator(',').
								build().parse();
		fileReader.close();
		return data;
		
	}
	
	public void update(List<KWData> rows)
	        throws IOException {
		FileWriter writer = new FileWriter(csvFile);
		ColumnPositionMappingStrategy<KWData> mappingStrategy = new ColumnPositionMappingStrategy<KWData>();
		mappingStrategy.setType(KWData.class);
		mappingStrategy.setColumnMapping(HEADER);
		StatefulBeanToCsvBuilder<KWData> builder = new StatefulBeanToCsvBuilder<KWData>(writer);
		StatefulBeanToCsv<KWData> beanWriter = builder.withMappingStrategy(mappingStrategy).
														withSeparator(',').build();
														// .withSeparator('#')
														// .withQuotechar('\'').build();

		// Write list to StatefulBeanToCsv object
		try {
			beanWriter.write(rows);
		} catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
			Logit.log(fqcn, Throwables.getStackTraceAsString(e));
		}
		writer.close();
	}	
}
