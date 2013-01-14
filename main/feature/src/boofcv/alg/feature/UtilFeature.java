/*
 * Copyright (c) 2011-2013, Peter Abeles. All Rights Reserved.
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

package boofcv.alg.feature;

import boofcv.abst.feature.describe.DescribeRegionPoint;
import boofcv.abst.feature.detdesc.DetectDescribeMulti;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.struct.FastQueue;
import boofcv.struct.feature.TupleDesc;

/**
 * Various utilities related to image features
 *
 * @author Peter Abeles
 */
public class UtilFeature {

	/**
	 * Creates a FastQueue and declares new instances of the descriptor using the provided
	 * {@link DetectDescribePoint}.  The queue will have declareInstance set to true, otherwise
	 * why would you be using this function?
	 */
	public static <TD extends TupleDesc>
	FastQueue<TD> createQueue( final DescribeRegionPoint<?, TD> detDesc , int initialMax ) {
		return new FastQueue<TD>(initialMax,detDesc.getDescriptorType(),true) {
			@Override
			protected TD createInstance() {
				return detDesc.createDescription();
			}
		};
	}

	/**
	 * Creates a FastQueue and declares new instances of the descriptor using the provided
	 * {@link DetectDescribePoint}.  The queue will have declareInstance set to true, otherwise
	 * why would you be using this function?
	 */
	public static <TD extends TupleDesc>
	FastQueue<TD> createQueue( final DetectDescribePoint<?, TD> detDesc , int initialMax ) {
		return new FastQueue<TD>(initialMax,detDesc.getDescriptionType(),true) {
			@Override
			protected TD createInstance() {
				return detDesc.createDescription();
			}
		};
	}

	/**
	 * Creates a FastQueue and declares new instances of the descriptor using the provided
	 * {@link DetectDescribePoint}.  The queue will have declareInstance set to true, otherwise
	 * why would you be using this function?
	 */
	public static <TD extends TupleDesc>
	FastQueue<TD> createQueue( final DetectDescribeMulti<?, TD> detDesc , int initialMax ) {
		return new FastQueue<TD>(initialMax,detDesc.getDescriptionType(),true) {
			@Override
			protected TD createInstance() {
				return detDesc.createDescription();
			}
		};
	}
}
