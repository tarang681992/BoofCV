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

package gecv.alg.misc;

import gecv.alg.InputSanityCheck;
import gecv.struct.image.*;


/**
 * Standard mathematical operations performed on a per-pixel basis or computed across the whole image.
 *
 * DO NOT MODIFY: Generated by {@link gecv.alg.misc.impl.GeneratorPixelMath}.
 *
 * @author Peter Abeles
 */
public class PixelMath {

	/**
	 * Sets each pixel in the output image to be the absolute value of the input image.
	 * Both the input and output image can be the same instance.
	 * 
	 * @param input The input image. Not modified.
	 * @param output Where the absolute value image is written to. Modified.
	 */
	public static void abs( ImageSInt8 input , ImageSInt8 output ) {

		InputSanityCheck.checkSameShape(input,output);
		
		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++ , indexDst++) {
				output.data[indexDst] = (byte)Math.abs(input.data[indexSrc]);
			}
		}
	}

	/**
	 * Sets each pixel in the output image to be the absolute value of the input image.
	 * Both the input and output image can be the same instance.
	 * 
	 * @param input The input image. Not modified.
	 * @param output Where the absolute value image is written to. Modified.
	 */
	public static void abs( ImageSInt16 input , ImageSInt16 output ) {

		InputSanityCheck.checkSameShape(input,output);
		
		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++ , indexDst++) {
				output.data[indexDst] = (short)Math.abs(input.data[indexSrc]);
			}
		}
	}

	/**
	 * Sets each pixel in the output image to be the absolute value of the input image.
	 * Both the input and output image can be the same instance.
	 * 
	 * @param input The input image. Not modified.
	 * @param output Where the absolute value image is written to. Modified.
	 */
	public static void abs( ImageSInt32 input , ImageSInt32 output ) {

		InputSanityCheck.checkSameShape(input,output);
		
		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++ , indexDst++) {
				output.data[indexDst] = Math.abs(input.data[indexSrc]);
			}
		}
	}

	/**
	 * Sets each pixel in the output image to be the absolute value of the input image.
	 * Both the input and output image can be the same instance.
	 * 
	 * @param input The input image. Not modified.
	 * @param output Where the absolute value image is written to. Modified.
	 */
	public static void abs( ImageFloat32 input , ImageFloat32 output ) {

		InputSanityCheck.checkSameShape(input,output);
		
		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++ , indexDst++) {
				output.data[indexDst] = Math.abs(input.data[indexSrc]);
			}
		}
	}

	/**
	 * Returns the absolute value of the element with the largest absolute value.
	 * 
	 * @param input Input image. Not modified.
	 * @return Largest pixel absolute value.
	 */
	public static int maxAbs( ImageUInt8 input ) {

		int max = 0;

		for( int y = 0; y < input.height; y++ ) {
			int index = input.startIndex + y*input.stride;
			int end = index + input.width;

			for( ; index < end; index++ ) {
				int v = input.data[index] & 0xFF;
				if( v > max )
					max = v;
			}
		}
		return max;
	}

	/**
	 * Divides each element by the denominator. Both input and output images can
	 * be the same.
	 *
	 * @param input The input image. Not modified.
	 * @param output The output image. Modified.
	 * @param denominator What each element is divided by.
	 */
	public static void divide( ImageUInt8 input , ImageUInt8 output, int denominator ) {

		InputSanityCheck.checkSameShape(input,output);

		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++, indexDst++ ) {
				output.data[indexDst] = (byte)(input.data[indexSrc] / denominator);
			}
		}
	}

	/**
	 * Multiplied each element by the scale factor. Both input and output images can
	 * be the same.
	 *
	 * @param input The input image. Not modified.
	 * @param output The output image. Modified.
	 * @param scale What each element is divided by.
	 */
	public static void multiply( ImageUInt8 input , ImageUInt8 output, int scale ) {

		InputSanityCheck.checkSameShape(input,output);

		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++, indexDst++ ) {
				int val = (input.data[indexSrc] & 0xFF)* scale;
				if( val < 0 ) val = 0;
				else if( val > 255 ) val = 255;
				output.data[indexDst] = (byte)val;
			}
		}
	}

	/**
	 * Each element has the specified number added to it. Both input and output images can
	 * be the same.
	 *
	 * @param input The input image. Not modified.
	 * @param output The output image. Modified.
	 * @param value What is added to each element.
	 */
	public static void plus( ImageUInt8 input , ImageUInt8 output, int value ) {

		InputSanityCheck.checkSameShape(input,output);

		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++, indexDst++ ) {
				int val = (input.data[indexSrc] & 0xFF) + value;
				if( val < 0 ) val = 0;
				else if( val > 255 ) val = 255;
				output.data[indexDst] = (byte)val;
			}
		}
	}

	/**
	 * Bounds image pixels to be between these two values
	 * 
	 * @param img Image
	 * @param min minimum value.
	 * @param max maximum value.
	 */
	public static void boundImage( ImageUInt8 img , int min , int max ) {
		final int h = img.getHeight();
		final int w = img.getWidth();

		int range = max-min;

		byte[] data = img.data;

		for (int y = 0; y < h; y++) {
			int index = img.getStartIndex() + y * img.getStride();
			for (int x = 0; x < w; x++,index++) {
				int value = data[index];
				if( value < min )
					data[index] = (byte)min;
				else if( value > max )
					data[index] = (byte)max;
			}
		}
	}

	/**
	 * Returns the absolute value of the element with the largest absolute value.
	 * 
	 * @param input Input image. Not modified.
	 * @return Largest pixel absolute value.
	 */
	public static int maxAbs( ImageSInt8 input ) {

		int max = 0;

		for( int y = 0; y < input.height; y++ ) {
			int index = input.startIndex + y*input.stride;
			int end = index + input.width;

			for( ; index < end; index++ ) {
				int v = Math.abs(input.data[index]);
				if( v > max )
					max = v;
			}
		}
		return max;
	}

	/**
	 * Divides each element by the denominator. Both input and output images can
	 * be the same.
	 *
	 * @param input The input image. Not modified.
	 * @param output The output image. Modified.
	 * @param denominator What each element is divided by.
	 */
	public static void divide( ImageSInt8 input , ImageSInt8 output, int denominator ) {

		InputSanityCheck.checkSameShape(input,output);

		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++, indexDst++ ) {
				output.data[indexDst] = (byte)((input.data[indexSrc] )/ denominator);
			}
		}
	}

	/**
	 * Multiplied each element by the scale factor. Both input and output images can
	 * be the same.
	 *
	 * @param input The input image. Not modified.
	 * @param output The output image. Modified.
	 * @param scale What each element is divided by.
	 */
	public static void multiply( ImageSInt8 input , ImageSInt8 output, int scale ) {

		InputSanityCheck.checkSameShape(input,output);

		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++, indexDst++ ) {
				int val = input.data[indexSrc] * scale;
				if( val < -128 ) val = -128;
				else if( val > 127 ) val = 127;
				output.data[indexDst] = (byte)val;
			}
		}
	}

	/**
	 * Each element has the specified number added to it. Both input and output images can
	 * be the same.
	 *
	 * @param input The input image. Not modified.
	 * @param output The output image. Modified.
	 * @param value What is added to each element.
	 */
	public static void plus( ImageSInt8 input , ImageSInt8 output, int value ) {

		InputSanityCheck.checkSameShape(input,output);

		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++, indexDst++ ) {
				int val = input.data[indexSrc] + value;
				if( val < -128 ) val = -128;
				else if( val > 127 ) val = 127;
				output.data[indexDst] = (byte)val;
			}
		}
	}

	/**
	 * Bounds image pixels to be between these two values
	 * 
	 * @param img Image
	 * @param min minimum value.
	 * @param max maximum value.
	 */
	public static void boundImage( ImageSInt8 img , int min , int max ) {
		final int h = img.getHeight();
		final int w = img.getWidth();

		int range = max-min;

		byte[] data = img.data;

		for (int y = 0; y < h; y++) {
			int index = img.getStartIndex() + y * img.getStride();
			for (int x = 0; x < w; x++,index++) {
				int value = data[index];
				if( value < min )
					data[index] = (byte)min;
				else if( value > max )
					data[index] = (byte)max;
			}
		}
	}

	/**
	 * Returns the absolute value of the element with the largest absolute value.
	 * 
	 * @param input Input image. Not modified.
	 * @return Largest pixel absolute value.
	 */
	public static int maxAbs( ImageUInt16 input ) {

		int max = 0;

		for( int y = 0; y < input.height; y++ ) {
			int index = input.startIndex + y*input.stride;
			int end = index + input.width;

			for( ; index < end; index++ ) {
				int v = input.data[index] & 0xFFFF;
				if( v > max )
					max = v;
			}
		}
		return max;
	}

	/**
	 * Divides each element by the denominator. Both input and output images can
	 * be the same.
	 *
	 * @param input The input image. Not modified.
	 * @param output The output image. Modified.
	 * @param denominator What each element is divided by.
	 */
	public static void divide( ImageUInt16 input , ImageUInt16 output, int denominator ) {

		InputSanityCheck.checkSameShape(input,output);

		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++, indexDst++ ) {
				output.data[indexDst] = (short)(input.data[indexSrc] / denominator);
			}
		}
	}

	/**
	 * Multiplied each element by the scale factor. Both input and output images can
	 * be the same.
	 *
	 * @param input The input image. Not modified.
	 * @param output The output image. Modified.
	 * @param scale What each element is divided by.
	 */
	public static void multiply( ImageUInt16 input , ImageUInt16 output, int scale ) {

		InputSanityCheck.checkSameShape(input,output);

		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++, indexDst++ ) {
				int val = (input.data[indexSrc] & 0xFFFF)* scale;
				if( val < 0 ) val = 0;
				else if( val > 65535 ) val = 65535;
				output.data[indexDst] = (short)val;
			}
		}
	}

	/**
	 * Each element has the specified number added to it. Both input and output images can
	 * be the same.
	 *
	 * @param input The input image. Not modified.
	 * @param output The output image. Modified.
	 * @param value What is added to each element.
	 */
	public static void plus( ImageUInt16 input , ImageUInt16 output, int value ) {

		InputSanityCheck.checkSameShape(input,output);

		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++, indexDst++ ) {
				int val = (input.data[indexSrc] & 0xFFFF) + value;
				if( val < 0 ) val = 0;
				else if( val > 65535 ) val = 65535;
				output.data[indexDst] = (short)val;
			}
		}
	}

	/**
	 * Bounds image pixels to be between these two values
	 * 
	 * @param img Image
	 * @param min minimum value.
	 * @param max maximum value.
	 */
	public static void boundImage( ImageUInt16 img , int min , int max ) {
		final int h = img.getHeight();
		final int w = img.getWidth();

		int range = max-min;

		short[] data = img.data;

		for (int y = 0; y < h; y++) {
			int index = img.getStartIndex() + y * img.getStride();
			for (int x = 0; x < w; x++,index++) {
				int value = data[index];
				if( value < min )
					data[index] = (short)min;
				else if( value > max )
					data[index] = (short)max;
			}
		}
	}

	/**
	 * Returns the absolute value of the element with the largest absolute value.
	 * 
	 * @param input Input image. Not modified.
	 * @return Largest pixel absolute value.
	 */
	public static int maxAbs( ImageSInt16 input ) {

		int max = 0;

		for( int y = 0; y < input.height; y++ ) {
			int index = input.startIndex + y*input.stride;
			int end = index + input.width;

			for( ; index < end; index++ ) {
				int v = Math.abs(input.data[index]);
				if( v > max )
					max = v;
			}
		}
		return max;
	}

	/**
	 * Divides each element by the denominator. Both input and output images can
	 * be the same.
	 *
	 * @param input The input image. Not modified.
	 * @param output The output image. Modified.
	 * @param denominator What each element is divided by.
	 */
	public static void divide( ImageSInt16 input , ImageSInt16 output, int denominator ) {

		InputSanityCheck.checkSameShape(input,output);

		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++, indexDst++ ) {
				output.data[indexDst] = (short)((input.data[indexSrc] )/ denominator);
			}
		}
	}

	/**
	 * Multiplied each element by the scale factor. Both input and output images can
	 * be the same.
	 *
	 * @param input The input image. Not modified.
	 * @param output The output image. Modified.
	 * @param scale What each element is divided by.
	 */
	public static void multiply( ImageSInt16 input , ImageSInt16 output, int scale ) {

		InputSanityCheck.checkSameShape(input,output);

		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++, indexDst++ ) {
				int val = input.data[indexSrc] * scale;
				if( val < -32768 ) val = -32768;
				else if( val > 32767 ) val = 32767;
				output.data[indexDst] = (short)val;
			}
		}
	}

	/**
	 * Each element has the specified number added to it. Both input and output images can
	 * be the same.
	 *
	 * @param input The input image. Not modified.
	 * @param output The output image. Modified.
	 * @param value What is added to each element.
	 */
	public static void plus( ImageSInt16 input , ImageSInt16 output, int value ) {

		InputSanityCheck.checkSameShape(input,output);

		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++, indexDst++ ) {
				int val = input.data[indexSrc] + value;
				if( val < -32768 ) val = -32768;
				else if( val > 32767 ) val = 32767;
				output.data[indexDst] = (short)val;
			}
		}
	}

	/**
	 * Bounds image pixels to be between these two values
	 * 
	 * @param img Image
	 * @param min minimum value.
	 * @param max maximum value.
	 */
	public static void boundImage( ImageSInt16 img , int min , int max ) {
		final int h = img.getHeight();
		final int w = img.getWidth();

		int range = max-min;

		short[] data = img.data;

		for (int y = 0; y < h; y++) {
			int index = img.getStartIndex() + y * img.getStride();
			for (int x = 0; x < w; x++,index++) {
				int value = data[index];
				if( value < min )
					data[index] = (short)min;
				else if( value > max )
					data[index] = (short)max;
			}
		}
	}

	/**
	 * Returns the absolute value of the element with the largest absolute value.
	 * 
	 * @param input Input image. Not modified.
	 * @return Largest pixel absolute value.
	 */
	public static int maxAbs( ImageSInt32 input ) {

		int max = 0;

		for( int y = 0; y < input.height; y++ ) {
			int index = input.startIndex + y*input.stride;
			int end = index + input.width;

			for( ; index < end; index++ ) {
				int v = Math.abs(input.data[index]);
				if( v > max )
					max = v;
			}
		}
		return max;
	}

	/**
	 * Divides each element by the denominator. Both input and output images can
	 * be the same.
	 *
	 * @param input The input image. Not modified.
	 * @param output The output image. Modified.
	 * @param denominator What each element is divided by.
	 */
	public static void divide( ImageSInt32 input , ImageSInt32 output, int denominator ) {

		InputSanityCheck.checkSameShape(input,output);

		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++, indexDst++ ) {
				output.data[indexDst] = ((input.data[indexSrc] )/ denominator);
			}
		}
	}

	/**
	 * Multiplied each element by the scale factor. Both input and output images can
	 * be the same.
	 *
	 * @param input The input image. Not modified.
	 * @param output The output image. Modified.
	 * @param scale What each element is divided by.
	 */
	public static void multiply( ImageSInt32 input , ImageSInt32 output, int scale ) {

		InputSanityCheck.checkSameShape(input,output);

		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++, indexDst++ ) {
				int val = input.data[indexSrc] * scale;
				output.data[indexDst] = val;
			}
		}
	}

	/**
	 * Each element has the specified number added to it. Both input and output images can
	 * be the same.
	 *
	 * @param input The input image. Not modified.
	 * @param output The output image. Modified.
	 * @param value What is added to each element.
	 */
	public static void plus( ImageSInt32 input , ImageSInt32 output, int value ) {

		InputSanityCheck.checkSameShape(input,output);

		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++, indexDst++ ) {
				int val = input.data[indexSrc] + value;
				output.data[indexDst] = val;
			}
		}
	}

	/**
	 * Bounds image pixels to be between these two values
	 * 
	 * @param img Image
	 * @param min minimum value.
	 * @param max maximum value.
	 */
	public static void boundImage( ImageSInt32 img , int min , int max ) {
		final int h = img.getHeight();
		final int w = img.getWidth();

		int range = max-min;

		int[] data = img.data;

		for (int y = 0; y < h; y++) {
			int index = img.getStartIndex() + y * img.getStride();
			for (int x = 0; x < w; x++,index++) {
				int value = data[index];
				if( value < min )
					data[index] = min;
				else if( value > max )
					data[index] = max;
			}
		}
	}

	/**
	 * Returns the absolute value of the element with the largest absolute value.
	 * 
	 * @param input Input image. Not modified.
	 * @return Largest pixel absolute value.
	 */
	public static float maxAbs( ImageFloat32 input ) {

		float max = 0;

		for( int y = 0; y < input.height; y++ ) {
			int index = input.startIndex + y*input.stride;
			int end = index + input.width;

			for( ; index < end; index++ ) {
				float v = Math.abs(input.data[index]);
				if( v > max )
					max = v;
			}
		}
		return max;
	}

	/**
	 * Divides each element by the denominator. Both input and output images can
	 * be the same.
	 *
	 * @param input The input image. Not modified.
	 * @param output The output image. Modified.
	 * @param denominator What each element is divided by.
	 */
	public static void divide( ImageFloat32 input , ImageFloat32 output, float denominator ) {

		InputSanityCheck.checkSameShape(input,output);

		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++, indexDst++ ) {
				output.data[indexDst] = input.data[indexSrc] / denominator;
			}
		}
	}

	/**
	 * Multiplied each element by the scale factor. Both input and output images can
	 * be the same.
	 *
	 * @param input The input image. Not modified.
	 * @param output The output image. Modified.
	 * @param scale What each element is divided by.
	 */
	public static void multiply( ImageFloat32 input , ImageFloat32 output, float scale ) {

		InputSanityCheck.checkSameShape(input,output);

		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++, indexDst++ ) {
				output.data[indexDst] = input.data[indexSrc] * scale;
			}
		}
	}

	/**
	 * Each element has the specified number added to it. Both input and output images can
	 * be the same.
	 *
	 * @param input The input image. Not modified.
	 * @param output The output image. Modified.
	 * @param value What is added to each element.
	 */
	public static void plus( ImageFloat32 input , ImageFloat32 output, float value ) {

		InputSanityCheck.checkSameShape(input,output);

		for( int y = 0; y < input.height; y++ ) {
			int indexSrc = input.startIndex + y* input.stride;
			int indexDst = output.startIndex + y* output.stride;
			int end = indexSrc + input.width;

			for( ; indexSrc < end; indexSrc++, indexDst++ ) {
				output.data[indexDst] = input.data[indexSrc] + value;
			}
		}
	}

	/**
	 * Bounds image pixels to be between these two values
	 * 
	 * @param img Image
	 * @param min minimum value.
	 * @param max maximum value.
	 */
	public static void boundImage( ImageFloat32 img , float min , float max ) {
		final int h = img.getHeight();
		final int w = img.getWidth();

		float range = max-min;

		float[] data = img.data;

		for (int y = 0; y < h; y++) {
			int index = img.getStartIndex() + y * img.getStride();
			for (int x = 0; x < w; x++,index++) {
				float value = data[index];
				if( value < min )
					data[index] = min;
				else if( value > max )
					data[index] = max;
			}
		}
	}

}
