package org.jcamp.parser;

import org.jcamp.spectrum.ArrayData;
import org.jcamp.spectrum.Assignment;
import org.jcamp.spectrum.EquidistantData;
import org.jcamp.spectrum.IDataArray1D;
import org.jcamp.spectrum.IOrderedDataArray1D;
import org.jcamp.spectrum.ISpectrumIdentifier;
import org.jcamp.spectrum.OrderedArrayData;
import org.jcamp.spectrum.Pattern;
import org.jcamp.spectrum.Peak1D;
import org.jcamp.spectrum.RamanSpectrum;
import org.jcamp.spectrum.Spectrum;
import org.jcamp.units.CommonUnit;
import org.jcamp.units.Unit;

/**
 * adapter between Raman spectrum class and JCAMPReader.
 * 
 * @author Thomas Weber
 */
public class RamanJCAMPReader
  extends CommonSpectrumJCAMPReader
  implements ISpectrumJCAMPReader {

  /**
   * RamanJCAMPAdapter constructor comment.
   */
  protected RamanJCAMPReader() {
    super();
  }

  /**
   * read Raman full spectrum.
   * 
   * @return com.creon.chem.spectrum.RamanSpectrum
   * @param block com.creon.chem.jcamp.JCAMPBlock
   */
  private RamanSpectrum createFS(JCAMPBlock block) throws JCAMPException {
    RamanSpectrum spectrum;
    Unit xUnit = getXUnits(block);
    if (xUnit == null)
      xUnit = CommonUnit.perCM;
    Unit yUnit = getYUnits(block);
    if (yUnit == null)
      yUnit = CommonUnit.absorbance;
    double xFactor = getXFactor(block);
    double yFactor = getYFactor(block);
    int nPoints = getNPoints(block);
    if (block.getDataRecord("XYDATA") != null) {
      double firstX = getFirstX(block);
      double lastX = getLastX(block);
      double[] intensities = getXYData(block, firstX, lastX, nPoints, xFactor, yFactor);
      if (intensities.length != nPoints)
	throw new JCAMPException("incorrect ##NPOINTS= or bad ##XYDATA=");
      IOrderedDataArray1D x = new EquidistantData(firstX, lastX, nPoints, xUnit);
      IDataArray1D y = new ArrayData(intensities, yUnit);
      spectrum = new RamanSpectrum(x, y, true);
    } else if (block.getDataRecord("XYPOINTS") != null) {
      double xy[][] = getXYPoints(block, nPoints, xFactor, yFactor);
      IOrderedDataArray1D x = new OrderedArrayData(xy[0], xUnit);
      IDataArray1D y = new ArrayData(xy[1], yUnit);
      spectrum = new RamanSpectrum(x, y, false);
    } else
      throw new JCAMPException("missing data: ##XYDATA= or ##XYPOINTS= required.");
    return spectrum;
  }

  /**
   * create Raman peak table (peak spectrum) from JCAMPBlock.
   * 
   * @return RamanSpectrum
   * @param block JCAMPBlock
   * @exception JCAMPException exception thrown if parsing fails.
   */
  private RamanSpectrum createPeakTable(JCAMPBlock block) throws JCAMPException {
    RamanSpectrum spectrum = null;
    Unit xUnit = getXUnits(block);
    if (xUnit == null)
      xUnit = CommonUnit.hertz;
    Unit yUnit = getYUnits(block);
    if (yUnit == null)
      yUnit = CommonUnit.intensity;
    double xFactor = getXFactor(block);
    double yFactor = getYFactor(block);
    int nPoints = getNPoints(block);
    Object[] tables = getPeaktable(block, nPoints, xFactor, yFactor);
    Peak1D[] peaks = (Peak1D[]) tables[0];
    if (peaks.length != nPoints) {
      block.getErrorHandler().error(
	  "incorrect ##NPOINTS=: expected "
	      + Integer.toString(nPoints)
	      + " but got "
	      + Integer.toString(peaks.length));
      nPoints = peaks.length;
    }
    double[][] xy = peakTableToPeakSpectrum(peaks);
    IOrderedDataArray1D x = new OrderedArrayData(xy[0], xUnit);
    IDataArray1D y = new ArrayData(xy[1], yUnit);
    spectrum = new RamanSpectrum(x, y, false);
    spectrum.setPeakTable(peaks);
    if (tables.length > 1) {
      spectrum.setPatternTable((Pattern[]) tables[1]);
      if (tables.length > 2)
	spectrum.setAssignments((Assignment[]) tables[2]);
    }
    return spectrum;
  }

  /**
   * createSpectrum method comment.
   */
  @Override
  public Spectrum createSpectrum(JCAMPBlock block) throws JCAMPException {
    if (block.getSpectrumID() != ISpectrumIdentifier.RAMAN)
      block.getErrorHandler().fatal("JCAMP reader adapter missmatch");
    RamanSpectrum spectrum = null;
    JCAMPBlock.Type type = block.getType();
    if (type.equals(JCAMPBlock.Type.FULLSPECTRUM))
      spectrum = createFS(block);
    else if (type.equals(JCAMPBlock.Type.PEAKTABLE))
      spectrum = createPeakTable(block);
    else if (type.equals(JCAMPBlock.Type.ASSIGNMENT))
      spectrum = createPeakTable(block);
    else // never reached
      block.getErrorHandler().fatal("illegal block type");
    setNotes(block, spectrum);
    return spectrum;
  }
}