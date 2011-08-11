/*
 * Copyright 2011 Peter Abeles
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gecv.alg.detect.interest;

import gecv.abst.detect.extract.FeatureExtractor;
import gecv.abst.detect.interest.GeneralizedIntegralImageFeatureIntensity;
import gecv.alg.detect.extract.SelectNBestFeatures;
import gecv.core.image.border.FactoryImageBorder;
import gecv.core.image.border.ImageBorder_F32;
import gecv.struct.QueueCorner;
import gecv.struct.image.ImageBase;
import gecv.struct.image.ImageFloat32;
import jgrl.struct.point.Point2D_I16;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * The Fast Hessian (FH) [1] interest point detector is designed to be a fast multi-scale "blob" detector.  FH
 * is intended for use as a feature detector for SURF [1].  It works  by computing an approximation of the
 * image Hessian's determinant using "box-lets" type features.   Unlike traditional scale-space algorithms
 * the feature itself is rescaled and is efficiently computed using an {@link gecv.alg.transform.ii.IntegralImageOps integral image}.
 * </p>
 *
 * <p>
 * This class is intended to be a faithful implementation of the algorithm described in [1].  Deviations
 * from that paper are noted in the code an in the comments below.   This detector can be used to implement
 * the FH-9 and FH-15 detectors.  For the FH-15 detector the input image needs to be doubled in size prior
 * to processing and the feature location rescaled.
 * </p>
 *
 * <p>
 * Description of scale space approach, see [1] for a more detailed and complete description.  Features are detected in
 * a series of octaves.  Each octave is defined as a set of scales where higher octaves contain larger scales.
 * A scale is defined by a feature's size in pixels, the size is the feature's width/height.  Improved accuracy in done
 * by interpolating feature location in pixel coordinates and scale.
 * </p>
 *
 * <p>
 * For example, the FH-9 detector has 4 octaves with the following detector sizes:<br>
 * Octave 1: Sizes = 9,15,21,27<br>
 * Octave 2: Sizes = 15,27,39,51<br>
 * Octave 3: Sizes = 27,51,75,99<br>
 * Octave 4: Sizes = 51,99,147,195
 * </p>
 *
 * <p>
 * Features are only detected for sizes which have a size smaller and larger.  For the first octave in the example
 * above that would be for sizes 15 and 21.  Sizes 9 and 27 are only used to identify local maximums in scale space.
 * </p>
 *
 * <p>
 * Note: Interpolation is performed by fitting a second order polynomial instead of a quadratic, as
 * suggested in the paper. See comments in {@link #polyPeak(float, float, float)}.
 * </p>
 *
 * <p>
 * [1] Herbert Bay, Andreas Ess, Tinne Tuytelaars, and Luc Van Gool, "Speeded-Up Robust Features (SURF)", CVIU June, 2008, Volume
 * 110, Issue 3, pages 346-359
 * </p>
 *
 * @author Peter Abeles
 */
// todo Do not compute intensity along the border, instead set it to zero?
// TODO or handle the border in a more intelligent way
public class FastHessianFeatureDetector<T extends ImageBase> {

	// finds features from 2D intensity image
	private FeatureExtractor extractor;
		// selects the features with the largest intensity
	private SelectNBestFeatures selectBest;

	// local sub-space
	private ImageFloat32 intensity[];
	private int spaceIndex = 0;
	private QueueCorner foundFeatures = new QueueCorner(100);

	// List of found feature points
	private List<ScalePoint> foundPoints = new ArrayList<ScalePoint>();

	// size of detected feature at the smallest scale
	private int initialSize;
	// the number of octaves it examines
	private int numberOfOctaves;

	// local variables that are predeclared
	int sizes[];

	/**
	 * <p>
	 * Defines the feature detector by specifying the size of features.
	 * </p>
	 * <p>
	 * Configuration for FH-9: initialSize=9, numberScalesPerOctave=4, numberOfOctaves=4<br>
	 * Configuration for FH-15: initialSize=15, numberScalesPerOctave=5, numberOfOctaves=4<br>
	 * * Note that FH-15 requires the image to be up sampled first. See [1] for details.
	 * </p>
	 *
	 * @param extractor Feature extractor used to find local maximums in 2D image.
	 * @param maxFeaturesPerScale Maximum number of features it can find per image scale.  If set <= 0 then the all potential
	 * features will be returned, which is how it is in the original paper.
	 * @param initialSize Size of the smallest feature in the lowest octave.
	 * @param numberScalesPerOctave How many different feature sizes are considered in a single octave
	 * @param numberOfOctaves How many different octaves are considered.
	 */
	public FastHessianFeatureDetector(FeatureExtractor extractor , int maxFeaturesPerScale  ,
									  int initialSize ,
									  int numberScalesPerOctave ,
									  int numberOfOctaves ) {
		this.extractor = extractor;
		if( maxFeaturesPerScale > 0 ) {
			selectBest = new SelectNBestFeatures(200);
			selectBest.setN(maxFeaturesPerScale);
		}
		this.initialSize = initialSize;
		this.numberOfOctaves = numberOfOctaves;

		sizes = new int[ numberScalesPerOctave ];
	}

	/**
	 * Detect interest points inside of the image.
	 *
	 * @param integral Image transformed into an integral image.
	 */
	public void detect( T integral ) {
		if( intensity == null ) {
			intensity = new ImageFloat32[3];
			for( int i = 0; i < intensity.length; i++ ) {
				intensity[i] = new ImageFloat32(integral.width,integral.height);
			}
		}
		foundPoints.clear();

		int skip = 1;
		int sizeStep = 6;
		int octaveSize = initialSize;
		for( int octave = 0; octave < numberOfOctaves; octave++ ) {
			for( int i = 0; i < sizes.length; i++ ) {
				sizes[i] = octaveSize + i*sizeStep;
			}
			// if the maximum kernel size is larger than the image don't process
			// the image any more
			int maxSize = sizes[sizes.length-1];
			if( maxSize > integral.width || maxSize > integral.height )
				break;
			// detect features inside of this octave
			detectOctave(integral,skip,sizes);
			skip += skip;
			octaveSize += sizeStep;
			sizeStep += sizeStep;
		}

		// todo save previously computed sizes for reuse in higher octaves
	}

	/**
	 * Computes feature intensities for all the specified feature sizes and finds features
	 * inside of the middle feature sizes.
	 *
	 * @param integral Integral image.
	 * @param skip Pixel skip factor
	 * @param featureSize which feature sizes should be detected.
	 */
	protected void detectOctave( T integral , int skip , int ...featureSize ) {

		int w = integral.width/skip;
		int h = integral.height/skip;

		for( int i = 0; i < intensity.length; i++ ) {
			intensity[i].reshape(w,h);
		}

		// compute feature intensity in each level
		for( int i = 0; i < featureSize.length; i++ ) {
			GeneralizedIntegralImageFeatureIntensity.hessian(integral,skip,featureSize[i],intensity[spaceIndex]);

			spaceIndex++;
			if( spaceIndex >= 3 )
				spaceIndex = 0;

			// find maximum in scale space
			if( i >= 2 ) {
				findLocalScaleSpaceMax(featureSize,i-1,skip);
			}
		}
	}

	/**
	 * Looks for features which are local maximums in the image and scale-space.
	 *
	 * @param size Size of features in different scale-spaces.
	 * @param level Which level in the scale-space
	 * @param skip How many pixels are skipped over.
	 */
	private void findLocalScaleSpaceMax(int []size, int level, int skip) {
		int index0 = spaceIndex;
		int index1 = (spaceIndex + 1) % 3;
		int index2 = (spaceIndex + 2) % 3;

		ImageBorder_F32 inten0 = FactoryImageBorder.value(intensity[index0],0);
		ImageFloat32 inten1 = intensity[index1];
		ImageBorder_F32 inten2 = FactoryImageBorder.value(intensity[index2],0);

		// find local maximums in image 2D space.  Borders need to be ignored since
		// false positives are found around them as an artifact of pixels outside being
		// treated as being zero.
		foundFeatures.reset();
		extractor.setIgnoreBorder(size[level]/(2*skip));
		extractor.process(intensity[index1],null,-1,null,foundFeatures);

		// if configured to do so, only select the features with the highest intensity
		QueueCorner features;
		if( selectBest != null ) {
			selectBest.process(intensity[index1],foundFeatures);
			features = selectBest.getBestCorners();
		} else {
			features = foundFeatures;
		}

		int levelSize = size[level];
		int sizeStep = levelSize-size[level-1];

		// see if these local maximums are also a maximum in scale-space
		for( int i = 0; i < features.num; i++ ) {
			Point2D_I16 f = features.get(i);

			float val = inten1.get(f.x,f.y);
			
			// see if it is a max in scale-space too
			if( checkMax(inten0,val,f.x,f.y) && checkMax(inten2,val,f.x,f.y) ) {

				// find the feature's location to sub-pixel accuracy using a second order polynomial
				// NOTE: In the original paper this was done using a quadratic.  See comments above.
				// NOTE: Using a 2D polynomial for x and y might produce better results.
				float peakX = polyPeak(inten1.get(f.x-1,f.y),inten1.get(f.x,f.y),inten1.get(f.x+1,f.y));
				float peakY = polyPeak(inten1.get(f.x,f.y-1),inten1.get(f.x,f.y),inten1.get(f.x,f.y+1));
				float peakS = polyPeak(inten0.get(f.x,f.y),inten1.get(f.x,f.y),inten2.get(f.x,f.y));

				float interpX = (f.x+peakX)*skip;
				float interpY = (f.y+peakY)*skip;
				float interpS = levelSize+peakS*sizeStep;

				double scale =  1.2*interpS/9.0;
				foundPoints.add( new ScalePoint((int)interpX,(int)interpY,scale));
			}
		}
	}

	/**
	 * Sees if the best score in the current layer is greater than all the scores in a 3x3 neighborhood
	 * in another layer.
	 */
	protected static boolean checkMax(ImageBorder_F32 inten, float bestScore, int c_x, int c_y) {
		for( int y = c_y -1; y <= c_y+1; y++ ) {
			for( int x = c_x-1; x <= c_x+1; x++ ) {
				if( inten.get(x,y) >= bestScore ) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * <p>
	 * Fits a second order polynomial to the data and determines the location of the peak.
	 * <br>
	 * y = a*x<sup>2</sup>+b*x + c<br>
	 * x = {-1,0,1}<br>
	 * y = Feature value
	 * </p>
	 *
	 * <p>
	 * Note: The original paper fit a 3D Quadratic to the data instead.  This required the first
	 * and second derivative of the Laplacian to be estimated.  Such estimates are error prone
	 * and using the technique found in OpenSURF produced erratic results and required some hackery
	 * to get to work.  This should always produce stable results and is much faster.
	 * </p>
	 *
	 * @param lower Value at x=-1
	 * @param middle Value at x=0
	 * @param upper Value at x=1
	 * @return x-coordinate of the peak
	 */
	private float polyPeak( float lower , float middle , float upper )
	{
		// only need two coefficients to compute the peak's location
		float a = 0.5f*lower - middle + 0.5f*upper;
		float b = 0.5f*upper - 0.5f*lower;

		return -b/(2.0f*a);
	}

	/**
	 * Returns all the found interest points.
	 *
	 * @return Found interest points.
	 */
	public List<ScalePoint> getFoundPoints() {
		return foundPoints;
	}
}