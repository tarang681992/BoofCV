/*
 * Copyright (c) 2011-2012, Peter Abeles. All Rights Reserved.
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

package boofcv.abst.geo.f;

import boofcv.abst.geo.EpipolarMatrixEstimatorN;
import boofcv.alg.geo.f.EpipolarTestSimulation;
import boofcv.struct.geo.AssociatedPair;
import georegression.geometry.GeometryMath_F64;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Applies various compliance tests for implementations of {@link boofcv.abst.geo.EpipolarMatrixEstimatorN}
 *
 * @author Peter Abeles
 */
public abstract class CheckEpipolarMatrixEstimatorN extends EpipolarTestSimulation {

	// the algorithm being tested
	EpipolarMatrixEstimatorN alg;

	// true if pixels or false if normalized
	boolean isPixels;

	protected CheckEpipolarMatrixEstimatorN(EpipolarMatrixEstimatorN alg,
											boolean pixels) {
		this.alg = alg;
		isPixels = pixels;
	}

	/**
	 * Makes sure the minimum number of points has been set
	 */
	@Test
	public void checkMinimumPoints() {
		assertTrue(alg.getMinimumPoints()>0);
	}

	/**
	 * Make sure the ordering of the epipolar constraint is computed correctly
	 */
	@Test
	public void checkConstraint() {
		init(50,isPixels);

		boolean workedOnce = false;

		for( int i = 0; i < 10; i++ ) {
			List<AssociatedPair> pairs = randomPairs(alg.getMinimumPoints());

			if( !alg.process(pairs)) {
				continue;
			}

			List<DenseMatrix64F> solutions = alg.getSolutions();

			if( solutions.size() <= 0 )
				continue;

			workedOnce = true;

			for( DenseMatrix64F F : solutions ) {
				// normalize to ensure proper scaling
				double n = CommonOps.elementMaxAbs(F);
				CommonOps.scale(1.0/n,F);

				for( AssociatedPair p : pairs ) {
					double correct = Math.abs(GeometryMath_F64.innerProd(p.currLoc, F, p.keyLoc));
					double wrong = Math.abs(GeometryMath_F64.innerProd(p.keyLoc, F, p.currLoc));

					assertTrue(correct < wrong*0.001);
				}

			}
		}
		assertTrue(workedOnce);
	}
}
