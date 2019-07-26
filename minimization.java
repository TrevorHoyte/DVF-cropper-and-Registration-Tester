import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.lang.Math;
import java.io.*;
import java.lang.*;
import java.util.*;
import java.lang.Math;
import java.text.*;

public class minimization {

	public static void main(String[] args) throws IOException {

//average stuff
		double sumforAV = 0;
		double numbpointAV =0 ;
		//reads file with bead location 
		String fileName = "POINTS.csv";
		File file = new File(fileName);

		try {
			Scanner inputStream = new Scanner(file);
			// make sure you skip the correct number of lines depending on where your data starts
			inputStream.next();
			while (inputStream.hasNext()) {
				String data =inputStream.next();
				String[] values =data.split(",");
				double xn = Double.parseDouble(values[8]);
				double yn = Double.parseDouble(values[9]);
				double zn = Double.parseDouble(values[10]);

				
				//for average
				numbpointAV ++;
				//this is the file that gets saved from the IJ plugin part
				String fileName2 = "RegisteredPoints.csv";
				File file2 = new File(fileName2);

				double minDist= Math.pow(10,10);
				String minDistwithID= "Something got messed up my G";

				try {
					Scanner inputStream2 = new Scanner(file2);


					//skips first lines of csv file
					for(int i=0; i<21;i++)
						inputStream2.next();

					while (inputStream2.hasNext()) {
						String data2 =inputStream2.next();
						String[] values2 =data2.split(",");
						double XMtest = Double.parseDouble(values2[16]);
						double YMtest = Double.parseDouble(values2[17]);
						double ZMtest = Double.parseDouble(values2[18]);

						//insert math for each loop
						//known points POINTS.csv file

						double Distance;

						double diffx = xn-XMtest;
						double diffy =yn-YMtest;
						double diffz = zn-ZMtest;

						double xsq = diffx*diffx;
						double ysq = diffy*diffy;
						double zsq = diffz*diffz;

						double sum1 =xsq+ysq+zsq;

						Distance = Math.sqrt(sum1);
						//rounding
						DecimalFormat df = new DecimalFormat("###.##");
					
						String DistwithID =  df.format(Distance) + " || R.I x:"+ df.format(XMtest) +",y:"+  df.format(YMtest) + ",z:" +df.format(ZMtest)
						 + " || OG x:" + df.format(xn) + ",y:"+ df.format(yn) + ",z:" +df.format(zn);

						//Write file with distance and coordinates of registered tumors
						// need to make sure the file is empty 
						/*************************** to check if program is running properly
				File file3 =new File("supertopsecretnumbers.txt");
				FileWriter fw3 = new FileWriter(file3,true);
				PrintWriter pw3 = new PrintWriter(fw3);
				pw3.println(""+DistwithID);
				pw3.close();
						 **********************************/

						if (Distance < minDist) {
							minDist = Distance;
							minDistwithID = DistwithID;
							
						}


					}
					inputStream2.close();	
					
					 System.out.println("distance: "+ minDistwithID);
					File file3 =new File("FINALRESULTSMF.txt");
					FileWriter fw3 = new FileWriter(file3,true);
					PrintWriter pw3 = new PrintWriter(fw3);
					pw3.println(""+minDistwithID);
					pw3.close();
					//for average
					sumforAV += minDist;
				}

				catch(FileNotFoundException e){}

			}
			
			double Average = sumforAV/numbpointAV;
			File file3 =new File("FINALRESULTSMF.txt");
			FileWriter fw3 = new FileWriter(file3,true);
			PrintWriter pw3 = new PrintWriter(fw3);
			pw3.println("Average Distance: "+Average);
			pw3.close();
			System.out.println("Average Distance :"+ Average);
			inputStream.close();
		}
		catch(FileNotFoundException e)
		{}

	}
}

