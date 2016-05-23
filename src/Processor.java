import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

import org.jcamp.parser.JCAMPReader;
import org.jcamp.spectrum.IDataArray1D;
import org.jcamp.spectrum.IOrderedDataArray1D;
import org.jcamp.spectrum.NMRSpectrum;
import org.jcamp.spectrum.Spectrum;

/**
 * Processes the Proton NMR data. Utilizes code from the jcamp library 
 * to open and parse jdx file data. Contains a few helper methods. 
 * @author Kwesi Daniel
 *
 */
public class Processor {

	/**
	 * The main method
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception { //TODO: Catch specific exceptions
				String fileName = "benzeneSpectrum.jdx";  //TODO: Add feature to browse for file and input from command line
				/******************Code copied from jcamp.parser***Reads data from jdx file*********/
				StringBuilder fileData = new StringBuilder(1000);
			    BufferedReader reader = new BufferedReader(new FileReader(fileName));
			    char[] buf = new char[1024];
			    int numRead=0;
			    while((numRead=reader.read(buf)) != -1){
			      String readData = String.valueOf(buf, 0, numRead);
			      fileData.append(readData);
			      buf = new char[1024];
			    }
			    reader.close();
			    Spectrum jcampSpectrum = JCAMPReader.getInstance().createSpectrum(fileData.toString());
			    if (!(jcampSpectrum instanceof NMRSpectrum)) {
			      throw new Exception("Spectrum in file is not an NMR spectrum!");
			    }
			    /********************************************************************************/
			    NMRSpectrum nmrspectrum = (NMRSpectrum) jcampSpectrum; //converts to nmr spectrum
			    IOrderedDataArray1D x = nmrspectrum.getXData();   //obtains the x values from the graph
			    IDataArray1D y = nmrspectrum.getYData();    	//obtains y values from graph
			    double highest = getHighest(new File(fileName));  //gets the highest Y value
			    double[] xValues = x.toArray();		//arrays to store x and y values
			    double[] yValues = y.toArray();			
			    double[] newX = new double[xValues.length];   //arrays to store relevant x&y data
			    double[] newY = new double[yValues.length];
			    int i = 0,j = 0;  //index values
			    ArrayList<Peak> peaks = new ArrayList<Peak>();  //an arraylist to store peak objects
			    System.out.println("getting peaks. newX = " + newX.length); 
			    System.out.println("getting peaks. newY = " + newY.length);
			    //This loop removes "noise" from the NMR such as negative values and very 
			    //small y values 
			    for(;i < xValues.length;i++){ //TODO: Check loop for efficiency 
			    	if(xValues[i] < 0 || yValues[i] < 1){continue;} //values to skip
			    	newX[j] = xValues[i];
			    	newY[j] = yValues[i];
			    	j++;
			    	//If the next data point is noise, then this should be the end of the peak
			    	if(((i+1) < xValues.length) && (xValues[i+1] < 0 || yValues[i+1] < 1)){
			    		peaks.add(new Peak(strip(newX,j),strip(newY,j)));
			    		j = 0;
			    		newX = new double[xValues.length - i];
			    		newY = new double[yValues.length - i];
			    	}
			    }
			    
			   // peaks = merge(peaks.toArray(new Peak[0])); //TODO: Fix the peak merge function
			    
			    System.out.println("Scaling...");
			   normalize(peaks.toArray(new Peak[0]),highest); //normalizes peak values
			    
			   //Displays Integral - Debugging purposes 
			    System.out.println("getting integrals. Peaks = " + peaks.size());
			    for(i = 0;i < peaks.size();i++){
			    	System.out.println("integral =" + peaks.get(i).getIntegral());
			    }
			    
		
			   
		}

	
	/**
	 * When removing noise, the new array has extra trailing 0's. This 
	 * function strips those trailing 0's. 
	 * @param array - The array to be processed
	 * @param limit - The index of the last relevant data point
	 * @return - A array without trailing 0's
	 */
	public static double[] strip(double[] array,int limit){
		double[] newArray = new double[limit];
		for(int i = 0;i < limit;i++){
			newArray[i] = array[i];
		}
		return newArray;
	}


	/**
	 * This function normalizes the integrals of all the peaks in the given
	 * array. The scaling value = largest integral/smallest integral. 
	 * Each integral is then multiplied by this scale. The actual number of hydrogens 
	 * contributing to the peak is the product of this new integral and another factor.
	 * This factor is equal to ("highest" variable)/(max*scale).
	 * @param peaks - An array containing the peaks
	 * @param highest - The highest y value in the spectrum
	 */
	public static void normalize(Peak[] peaks,double highest){
		double max = 0;
		double min = peaks[0].getIntegral();
		for(int i = 0;i < peaks.length;i++){ //Gets the greatest integral
			if(peaks[i].getIntegral() > max){max = peaks[i].getIntegral();}
			if(peaks[i].getIntegral() < min){min = peaks[i].getIntegral();}
		}
		System.out.println("Max = " + max + " Min = " + min);
		int scale = round(max/min); //gets initial scale value
		System.out.println("Scale = " + scale);
		for(int i = 0;i < peaks.length;i++){ //TODO: Make these two loops more efficient. Reduce repetition
			peaks[i].setIntegral(round(peaks[i].getIntegral() * scale));
		}
		scale = round(highest)/round(max*scale); //gets the second scale value
		for(int i = 0;i < peaks.length;i++){
			peaks[i].setIntegral(round(peaks[i].getIntegral() * scale));
		}
		
		
	}
	
	/**
	 * Rounds a number to the nearest integer.
	 * Rounds up if the decimal is .5 or greater.
	 * Rounds down otherwise
	 * @param num - the number to be rounded
	 * @return - the rounded value as an integer
	 */
	public static int round(double num){
		int up = (int) (Math.ceil(num) - num);
		int down = (int) (num - Math.floor(num));
		if(up >= down){return (int)Math.ceil(num);}
		else{return (int) Math.floor(num);}
		
	} 
	
	/**
	 * This function merges peaks that have been accidentally split by this program.
	 * @param peaks
	 * @return
	 */
	/*
	public static ArrayList<Peak> merge(Peak[] peaks){
		ArrayList<Peak> newPeaks = new ArrayList<Peak>();
		boolean merged = false;
		double dif = 0;
		Peak temp;
		for(int i = 0;i < peaks.length;i++){
			
			if(i+1 < peaks.length){
				dif = peaks[i].getLast() - peaks[i+1].getFirst();
				if(dif < .01){
					System.out.println("Merging");
					temp = new Peak(peaks[i],peaks[i+1]);
					newPeaks.add(temp);
					peaks[i+1] = temp;
					merged = true;}
			}
			else{newPeaks.add(peaks[i]); merged = false;}
		}
		if(!merged){newPeaks.add(peaks[peaks.length-1]);}
		return newPeaks;
	}*/
	
	/**
	 * Gets the largest Y value directly from the jdx file
	 * @param file - the jdx file
	 * @return - the highest y value
	 * @throws FileNotFoundException
	 */
	public static double getHighest(File file) throws FileNotFoundException{
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(file);
		String next = scanner.nextLine();
		while(!next.contains("##MAXY= ")){
			next = scanner.nextLine();
		}
		next = next.substring(8,next.length()-3);
		return Double.parseDouble(next);
	}
		
}




