import java.util.ArrayList;


/**
 * Creates a Peak object to store peak data.
 * @author Kwesi Daniel
 *
 */
public class Peak {
	private double[] x; //An array to store all X values contributing to the peak
	private double[] y; //An array to store all Y values contributing to the peak
	private double integral; //The integral of this peak
	private double first; //The X value of the start of the peak
	private double last; //X value of the end of the peak
	
	
	/**
	 * A constructor for a peak.
	 * @param x - An array containing this peak's X values
	 * @param y - An array containing this peak's y values
	 */
	Peak(double[] x,double[] y){
		this.x = x;
		this.y = y;
		System.out.println("creating peak");
		integrate(this.x,this.y);
	    System.out.println("The range of this peak is " + this.x[0] + " to " + this.x[this.x.length-1]);	    
	    first = this.x[0];
	    last = this.x[this.x.length-1];
	}
	
	/**
	 * Creates a merged peak from two other peaks
	 * @param a - First peak
	 * @param b - Second peak
	 */
	/*
	Peak(Peak a,Peak b){
		double[] x = a.getX();
		double[] y = a.getY();
		double[] x2 = b.getX();
		double[] y2 = b.getY();
		this.x = mergeValues(x,x2);
		this.y = mergeValues(y,y2);
		System.out.println("creating peak");
		integrate(this.x,this.y);
	    System.out.println("The range of this peak is " + this.x[0] + " to " + this.x[this.x.length-1]);
	    first = this.x[0];
	    last = this.x[this.x.length-1];
	}*/
	
	/**
	 * Merges two arrays into one
	 * @param a - first array
	 * @param b - second array
	 * @return - the merged array
	 */
	/*
	private double[] mergeValues(double[] a, double[] b){
		double[] c = new double[a.length + b.length];
		int j = 0;
		for(int i = 0;i < a.length;i++,j++){
			c[j] = a[i];
		}
		for(int i = 0;i < b.length;i++,j++){
			c[j] = b[i];
		}
		return c;
	}*/
	
	/**
	 * Gets the left handed integral of this peak.
	 * 
	 * @param x - The x values of this peak
	 * @param y - The y values of this peak
	 */
	private void integrate(double[] x,double[] y){ //TODO: Add dependence on the class' X and Y arrays
		System.out.println("integrating");
		Point[] points = getIntermediates(x,y); //Gets the intermediate value between two points
		double width = 0; //width of the rectangle
		double height = 0; //height of the rectangle
		double areas = 0; //Accumulator for all the areas of the rectangles
		System.out.println("finding area");
		for(int i = 0;i+1 < points.length;i++){ //Gets the area of each rectangle and sums it.
			width = Math.abs(points[i+1].getX() - points[i].getX());
			height = points[i].getY();
			areas += getArea(width,height);
		}
		//setIntegral(areas);
		this.integral = areas;
		
	}
	
	/**
	 * Gets the area of a rectangle
	 * @param width - the width of this rectangle
	 * @param height - the height of this rectangle
	 * @return - the area of this rectangle
	 */
	private double getArea(double width,double height){
		return width * height;
	}
	
	/**
	 * Gets the average of two values
	 * @param a - the first value
	 * @param b - the second value
	 * @return - the average
	 */
	/*
	private double average(double a,double b){
		return (a+b)/2;
	}*/
	
	/**
	 * Gets the x and y of a point between two other points.
	 * @param p1 - the first point
	 * @param p2 - the second point
	 * @return - the intermediate point
	 */
	private Point averagePoints(Point p1,Point p2){
		double x = (p1.getX() + p2.getX())/2;
		double y = (p1.getY() + p2.getY())/2;
		return new Point(x,y);
	}
	
	/**
	 * Gets the intermediate values between each point. 
	 * This is to smooth out the curve to allow for a more accurate integral
	 * @param x - array containing x values
	 * @param y - array containing y values
	 * @return - an array of all the points in the peak's graph
	 */
	private Point[] getIntermediates(double[] x,double[]y){ //TODO: add dependency on the class' x and y arrays
		System.out.println("getting intermediates");
		ArrayList<Point> points = new ArrayList<Point>(); //Creates an arraylist to store points
		for(int i = 0;i < x.length;i++){
			points.add(new Point(x[i],y[i]));
		}
		x = null;
		y = null;
		return minimize(points.toArray(new Point[1]));
	}
	
	/**
	 * Obtains intermediate points and continues to obtain the intermediates 
	 * between these new points recursively. 
	 * @param points - The initial array of points on the graph
	 * @return - the new array with more data points.
	 */
	private Point[] minimize(Point[] points){
		System.out.println("minimizing");
		try{ 
			System.out.println("diff = " + (points[0].getX() - points[1].getX()));
			if((points[0].getX() - points[1].getX()) < .0000001){ //Base case. Stops creating intermediates 
				return points;									//when the difference between points is very small
				}
		}
		catch (IndexOutOfBoundsException oobe){
			System.out.println("Error! Array too Small!");
			oobe.printStackTrace();
		}
		System.out.println("points end = " + points[points.length-1].getX());
		ArrayList<Point> newPoints = new ArrayList<Point>();
		for(int i = 0;i+1 < points.length;i++){  
			newPoints.add(points[i]);   //adds the first point to the array
			newPoints.add(averagePoints(points[i],points[i+1])); //adds the intermediate point
		}
		newPoints.add(points[points.length-1]); //adds the last point to the array
		System.out.println("new points end = " + newPoints.get(newPoints.size()-1).getX());
		return minimize(newPoints.toArray(new Point[0]));  //Gets the intermediate points of this new array
		
	}

	public double getIntegral() {
		return integral;
	}

	public void setIntegral(double integral) {
		this.integral = integral;
	}
	
	public double getFirst() {
		return first;
	}

	public double getLast() {
		return last;
	}

	public double[] getX() {
		return x;
	}

	public double[] getY() {
		return y;
	}

}
