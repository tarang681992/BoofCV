/*
 * Copyright (c) 2011-2014, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.alg.segmentation.slic;

import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageSInt32;
import org.ddogleg.struct.FastQueue;

import java.util.Arrays;

/**
 *
 * <p>
 * [1] Radhakrishna Achanta, Appu Shaji, Kevin Smith, Aurelien Lucchi, Pascal Fua, and Sabine Süsstrunk,
 * SLIC Superpixels, EPFL Technical Report no. 149300, June 2010.
 * </p>
 *
 * @author Peter Abeles
 */
// TODO They use Euclidean distance squared in actual code
// TODO it also appears that they are approximating k-means clustersing.  Just doing NN
public abstract class SegmentSlic<T extends ImageBase> {

	// number of bands in the input image
	private int numBands;

	// the number of regions/superpixels.  K in the paper
	private int numberOfRegions;

	// spacial weighting tuning parameter  Is also m in the paper.
	private float m;

	// Number of iterations
	private int totalIterations;

	// Space between superpixel centers.  S in the paper
	private int gridInterval;
	// Adjustment to spacial distance.  Computed from m and gridInterval
	private float adjustSpacial;

	// The image being processed
	protected T input;

	// storage for clusters and pixel information
	private FastQueue<Cluster> clusters = new FastQueue<Cluster>(Cluster.class,true);
	private FastQueue<Pixel> pixels = new FastQueue<Pixel>(Pixel.class,true);

	public SegmentSlic( int numberOfRegions , float m , int totalIterations , int numBands ) {
		this.numberOfRegions = numberOfRegions;
		this.m = m;
		this.totalIterations = totalIterations;
		this.numBands = numBands;
	}

	public void process( T input , ImageSInt32 output ) {
		this.input = input;

		int numOfPixels = input.width*input.height;

		gridInterval = (int)Math.sqrt( numOfPixels/(double)numberOfRegions);

		if( gridInterval <= 0 )
			throw new IllegalArgumentException("Too many regions for an image of this size");

		// See equation (1)
		adjustSpacial = m/gridInterval;

		initializeClusters();

		// Perform the modified k-means iterations
		for( int i = 0; i < totalIterations; i++ ) {
			computeClusterDistance();
			updateClusters();
		}

		// Assign labels to each pixel based on how close it is to a cluster
		computeClusterDistance();
		assignLabelsToPixels(output);

		// Assign disconnected pixels to the largest cluster they touch
		// TODO do this
	}

	/**
	 * initialize all the clusters at regularly spaced intervals.  Their locations are perturbed a bit to reduce
	 * the likelihood of a bad location.  Initial color is set to the image color at the location
	 */
	private void initializeClusters() {

		int offset = gridInterval/2;

		int clusterId = 0;
		clusters.reset();
		for( int y = offset; y < input.height; y += gridInterval ) {
			for( int x = offset; x < input.width; x += gridInterval ) {
				// this should rarely be a problem, but this way I know it won't blow up with a really small image
				if( isTooCloseToEdge(x,y) )
					continue;

				Cluster c = clusters.grow();
				c.id = clusterId++;
				if( c.color == null)
					c.color = new float[numBands];

				// sets the location and color at the local minimal gradient point
				perturbCenter( c , x , y );
			}
		}
	}

	/**
	 * Checks to see if the cluster center is too close to the image border.
	 */
	protected boolean isTooCloseToEdge( int x , int y ) {
		return( x < 1 || x >= input.width-1 || y < 1 || y >= input.height-1 );
	}

	/**
	 * Set the cluster's center to be the pixel in a 3x3 neighborhood with the smallest gradient
	 */
	protected void perturbCenter( Cluster c , int x , int y ) {
		float best = Float.MAX_VALUE;
		int bestX=0,bestY=0;

		for( int dy = -1; dy <= 1; dy++ ) {
			for( int dx = -1; dx <= 1; dx++ ) {
				float d = gradient(x + dx, y + dy);
				if( d < best ) {
					best = d;
					bestX = dx;
					bestY = dy;
				}
			}
		}

		c.x = x+bestX;
		c.y = y+bestY;
		setColor(c,x+bestX,y+bestY);
	}

	/**
	 * Computes the gradient at the specified pixel
	 */
	protected float gradient(int x, int y) {
		float dx = getIntensity(x+1,y) - getIntensity(x-1,y);
		float dy = getIntensity(x,y+1) - getIntensity(x,y-1);

		return dx*dy;
	}

	/**
	 * Sets the cluster's to the pixel color at that location
	 */
	public abstract void setColor( Cluster c , int x , int y );

	/**
	 * Performs a weighted add to the cluster's color at the specified pixel in the image
	 */
	public abstract void addColor( Cluster c , int index , float weight );

	/**
	 * Euclidean Squared distance away that the pixel is from the provided color
	 */
	public abstract float colorDistance( float[] color , int index );

	/**
	 * Intensity of the pixel at the specified location
	 */
	public abstract float getIntensity(int x, int y);

	/**
	 * Computes how far away each cluster is from each pixel.  Expectation step.
	 */
	protected void computeClusterDistance() {
		for( int i = 0; i < pixels.size; i++ ) {
			pixels.data[i].reset();
		}

		for( int i = 0; i < clusters.size; i++ ) {
			Cluster c = clusters.data[i];

			// compute search bounds
			int centerX = (int)(c.x + 0.5f);
			int centerY = (int)(c.y + 0.5f);

			int x0 = centerX - gridInterval; int x1 = centerX + gridInterval + 1;
			int y0 = centerY - gridInterval; int y1 = centerY + gridInterval + 1;

			if( x0 < 0 ) x0 = 0;
			if( y0 < 0 ) y0 = 0;
			if( x1 > input.width ) x1 = input.width;
			if( y1 > input.height ) y1 = input.height;

			for( int y = y0; y < y1; y++ ) {
				int indexPixel = y*input.width + x0;
				int indexInput = input.startIndex + y*input.stride + x0;

				int dy = y-centerY;

				for( int x = x0; x < x1; x++ , indexPixel++ ) {
					int dx = x-centerX;

					float distanceColor = colorDistance(c.color,indexInput++);
					float distanceSpacial = dx*dx + dy*dy;
					pixels.data[indexPixel++].add(c,distanceColor + adjustSpacial*distanceSpacial);
				}
			}

			// update each pixel inside the search rectangle
			clusters.data[i].reset();
		}
	}

	/**
	 * Update the value of each cluster using  Maximization step.
	 */
	protected void updateClusters() {
		for( int i = 0; i < clusters.size; i++ ) {
			clusters.data[i].reset();
		}

		int indexPixel = 0;
		for( int y = 0; y < input.height; y++ ) {
			int indexInput = input.startIndex + y*input.stride;
			for( int x =0; x < input.width; x++ , indexPixel++ , indexInput++) {
				Pixel p = pixels.get(indexPixel);

				// convert the distance each cluster is from the pixel into weights
				p.computeWeights();

				for( int i = 0; i < p.total; i++ ) {
					ClusterDistance d = p.clusters[i];
					d.cluster.x += x*d.distance;
					d.cluster.y += y*d.distance;
					d.cluster.totalWeight += d.distance;
					addColor(d.cluster,indexInput,d.distance);
				}
			}
		}

		// recompute the center of each cluster
		for( int i = 0; i < clusters.size; i++ ) {
			clusters.data[i].update();
		}
	}

	/**
	 * Selects which region each pixel belongs to based on which cluster it is the closest to
	 */
	public void assignLabelsToPixels( ImageSInt32 output ) {

		int indexPixel = 0;
		for( int y = 0; y < output.height; y++ ) {
			int indexOutput = output.startIndex + y*output.stride;
			for( int x =0; x < output.width; x++ , indexPixel++ , indexOutput++) {
				Pixel p = pixels.data[indexPixel];

				// in the rare situation when it isn't assigned to anything, just set the ID to 0.
				int best = 0;
				float bestDistance = Float.MAX_VALUE;
				// find the region/cluster which it is closest to
				for( int j = 0; j < p.total; j++ ) {
					ClusterDistance d = p.clusters[j];
					if( d.distance < bestDistance ) {
						bestDistance = d.distance;
						best = d.cluster.id;
					}
				}

				output.data[indexOutput] = best;
			}
		}

	}

	/**
	 * K-means clustering information for each pixel.  Stores distance from each cluster mean.
	 */
	public static class Pixel
	{
		// list of clusters it is interacting with
		public ClusterDistance clusters[];
		// how many clusters it is interacting with
		int total;

		public Pixel() {
			this.clusters = new ClusterDistance[10];
			for( int i = 0; i < clusters.length; i++ )
				clusters[i] = new ClusterDistance();
		}

		public void add( Cluster c , float distance ) {
			// make sure it isn't already full.  THis should almost never happen
			if( total >= clusters.length )
				return;

			clusters[total].cluster = c;
			clusters[total].distance = distance;
			total++;
		}

		public void computeWeights() {
			float sum = 0;
			for( int i = 0; i < total; i++ ) {
				sum += clusters[i].distance;
			}
			for( int i = 0; i < total; i++ ) {
				clusters[i].distance =  1.0f - clusters[i].distance/sum;
			}
		}

		public void reset() {
			total = 0;
		}
	}

	/**
	 * Stores how far a cluster is from the specified pixel
	 */
	public static class ClusterDistance
	{
		// the cluster
		public Cluster cluster;
		// how far the pixel is from the cluster
		public float distance;
	}

	/**
	 * The mean in k-means.  Point in image (x,y) and color space.
	 */
	public static class Cluster
	{
		// unique ID for the cluster
		public int id;

		// location of the cluster in the image and color space
		public float x;
		public float y;
		public float color[];

		// the total.  Used when being updated
		public float totalWeight;

		public void reset() {
			x = y = 0;
			Arrays.fill(color,0);
			totalWeight = 0;
		}

		public void update() {
			x /= totalWeight;
			y /= totalWeight;
			for( int i = 0; i < color.length; i++ ) {
				color[i] /= totalWeight;
			}
		}
	}
}
