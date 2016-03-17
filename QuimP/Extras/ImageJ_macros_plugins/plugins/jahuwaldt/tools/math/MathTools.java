/*
*   MathTools  -- A collection of useful math utility routines.
*
*   Copyright (C) 1999-2002 by Joseph A. Huwaldt <jhuwaldt@knology.net>.
*   All rights reserved.
*   
*   This library is free software; you can redistribute it and/or
*   modify it under the terms of the GNU Library General Public
*   License as published by the Free Software Foundation; either
*   version 2 of the License, or (at your option) any later version.
*   
*   This library is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
*   Library General Public License for more details.
**/
package jahuwaldt.tools.math;

import java.util.BitSet;


/**
*  <p>  A collection of useful static routines of a general
*       mathematical nature.  This file includes functions that
*       accomplish all types of wonderous mathematical stuff.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author   Joseph A. Huwaldt   Date:  September 29, 1997
*  @version  May 6, 2002
**/
public class MathTools extends Object {

	/**
	*  The natural logarithm of 10.
	**/
	public static final double LOG10 = Math.log(10);
	
	/**
	*  The natural logarithm of 2.
	**/
	public static final double LOG2 = Math.log(2);
	
	/**
    *  The natural logarithm of the maximum double value:  log(MAX_VALUE).
    **/
    public static final double MAX_LOG = Math.log(Double.MAX_VALUE);
    
    /**
    *  The natural logarithm of the minimum double value:  log(MIN_VALUE).
    **/
    public static final double MIN_LOG = Math.log(Double.MIN_VALUE);
    
	/**
	*  Prevent anyone from instantiating this utiltity class.
	**/
	private MathTools() {}
	
	//-----------------------------------------------------------------------------------
	/**
	*  Test to see if a given long integer is even.
	*
	*  @param   n  Integer number to be tested.
	*  @return  True if the number is even, false if it is odd.
	**/
	public static final boolean even( long n ) {
		return (n & 1) == 0;
	}

	/**
	*  Test to see if a given long integer is odd.
	*
	*  @param   n  Integer number to be tested.
	*  @return  True if the number is odd, false if it is even.
	**/
	public static final boolean odd( long n ) {
		return (n & 1) != 0;
	}

	/**
	*  Calculates the square (x^2) of the argument.
	*
	*  @param   x  Argument to be squared.
	*  @return  Returns the square (x^2) of the argument.
	**/
	public static final double sqr( double x ) {
		if ( x == 0. )
			return 0.;
		else
			return x * x;
	}

	/**
	*  Computes the cube root of the specified real number.
	*  If the argument is negative, then the cube root is negative.
	*
	*  @param   x  Argument for which the cube root is to be found.
	*  @return  The cube root of the argument is returned.
	**/
	public static final double cubeRoot( double x ) {
		double value = 0;
		
		if ( x < 0. )
			value = -Math.exp( Math.log(-x) / 3. );
		else
			value = Math.exp( Math.log( x ) / 3. );
			
		return value;
	}

	/**
	*  Returns a number "a" raised to the power "b".  A "long" version
	*  of Math.pow().  This is much faster than using Math.pow() if
	*  the operands are integers.
	*
	*  @param   a  Number to be raised to the power "b".
	*  @param   b  Power to raise number "a" to.
	*  @return  A long integer "a" raised to the integer power "b".
	*  @throws  ArithmeticException if "b" is negative.
	**/
	public static final long pow( long a, long b ) throws ArithmeticException {
		if ( b < 0 )
			throw new ArithmeticException( "Exponent must be positive." );
		
		long r = 1;
		while ( b != 0 ) {
			if ( odd( b ) )
				r *= a;
			
			b >>>= 1;
			a *= a;
		}
		return r;
	}

	/**
	*  Raises 2 to the small integer power indicated (eg:  2^3 = 8).
	*  This is MUCH faster than calling Math.pow(2, x).
	*
	*  @param   x  Amount to raise 2 to the power of.
	*  @return  Returns 2 raised to the power indicated.
	**/
	public static final long pow2( long x ) {
		long value = 1;
		for ( long i = 0; i < x; ++i ) {
			value *= 2;
		}
		return value;
	}

	/**
	*  Raises 10 to the small integer power indicated (eg: 10^5 = 100000).
	*  This is faster than calling Math.pow(10, x).
	*
	*  @param   x  Amount to raise 10 to the power of.
	*  @return  Returns 10 raised to the power indicated.
	**/
	public static final double pow10(int x) {
		double pow10 = 10.;

		if (x != 0) {
			boolean neg = false;
			if (x < 0) {
				x *= -1;
				neg = true;
			}
		
			for (int i=1; i < x; ++i)
				pow10 *= 10.;
		
			if (neg)
				pow10 = 1./pow10;
		
		} else
			pow10 = 1.;

		return(pow10);
	}
	
	/**
	*  Find the base 10 logarithm of the given double.
	*
	*  @param   x  Value to find the base 10 logarithm of.
	*  @return  The base 10 logarithm of x.
	**/
	public static final double log10( double x ) {
		return Math.log(x)/LOG10;
	}

	/**
	*  Find the base 2 logarithm of the given double.
	*
	*  @param   x  Value to find the base 2 logarithm of.
	*  @return  The base 2 logarithm of x.
	**/
	public static final double log2( double x ) {
		return Math.log(x)/LOG2;
	}

	/**
	*  Rounds a floating point number to the desired decimal place.
	*  Example:  1346.4667 rounded to the 2nd place = 1300.
	*
	*  @param  value  The value to be rounded.
	*  @param  place  Number of decimal places to round value to.
	*                 A place of 1 rounds to 10's place, 2 to 100's
	*                 place, -2 to 1/100th place, et cetera.
	**/
	public static final double roundToPlace(double value, int place)  {

		//	If the value is zero, just pass the number back out.
		if (value != 0.) {

            //  If the place is zero, round to the one's place.
		    if (place == 0)
		        value = Math.floor(value+0.5);

		    else {
			    double pow10 = MathTools.pow10(place);	//	= 10 ^ place
			    double holdvalue = value/pow10;
		
			    value = Math.floor(holdvalue+0.5);		// Round number to nearest integer
			    value *= pow10;
		    }
		}

		return value;
	}

	/**
	*  Rounds a floating point number up to the desired decimal place.
	*  Example:  1346.4667 rounded up to the 2nd place = 1400.
	*
	*  @param  value  The value to be rounded up.
	*  @param  place  Number of decimal places to round value to.
	*                 A place of 1 rounds to 10's place, 2 to 100's
	*                 place, -2 to 1/100th place, et cetera.
	**/
	public static final double roundUpToPlace(double value, int place)  {

		//	If the value is zero, just pass the number back out.
		if (value != 0.) {

            //  If the place is zero, round to the one's place.
		    if (place == 0)
		        value = Math.ceil(value);

		    else {
			    double pow10 = MathTools.pow10(place);	//	= 10 ^ place
			    double holdvalue = value/pow10;
		
			    value = Math.ceil(holdvalue);			// Round number up to nearest integer
			    value *= pow10;
		    }
		}

		return value;
	}

	/**
	*  Rounds a floating point number down to the desired decimal place.
	*  Example:  1346.4667 rounded down to the 1st place = 1340.
	*
	*  @param  value  The value to be rounded down.
	*  @param  place  Number of decimal places to round value to.
	*                 A place of 1 rounds to 10's place, 2 to 100's
	*                 place, -2 to 1/100th place, et cetera.
	**/
	public static final double roundDownToPlace(double value, int place)  {

		//	If the value is zero, just pass the number back out.
		if (value != 0.) {

            //  If the place is zero, round to the one's place.
		    if (place == 0)
		        value = Math.floor(value);

		    else {
			    double pow10 = MathTools.pow10(place);	//	= 10 ^ place
			    double holdvalue = value/pow10;
		
			    value = Math.floor(holdvalue);			// Round number down to nearest integer
			    value *= pow10;
		    }
		}

		return value;
	}


	/**
	*  Calculates the greatest common divisor between two input
	*  integers.  The GCD is the largest number that can be
	*  divided into both input numbers.  Uses Euler's method.
	*
	*  @param   xval  First integer
	*  @param   yval  Second integer
	*  @return  The largest number that can be divided into both input
	*           values.
	**/
	public static final long greatestCommonDivisor( long xval, long yval ) {
		long value = 0;
		while ( value != xval ) {
			if ( xval < yval )
				yval = yval - xval;
			
			else {
				if ( xval > yval )
					xval = xval - yval;
				else
					value = xval;
			}
		}
		return (value);
	}

	/**
	*  Returns the fractional part of a floating point number
	*  (removes the integer part).
	*
	*  @param   x  Argument for which the fractional part is to be returned.
	*  @return  The fractional part of the argument is returned.
	**/
	public static final double frac( double x ) {
		x = x - (long) x;
		if ( x < 0. )
			++x;
		
		return x;
	}

	/**
	*  Straight linear 1D interpolation between two points.
	*
	*  @param   x1,y1  Coordinates of the 1st point (the high point).
	*  @param   x2,y2  Coordinates of the 2nd point (the low point).
	*  @param   x      The X coordinate of the point for which we want to
	*                  interpolate to determine a Y coordinate.  Will
	*                  extrapolate if X is outside of the bounds of the 
	*                  point arguments.
	*  @return  The interpolated Y value corresponding to the input X
	*           value is returned.
	**/
	public static final double lineInterp( double x1, double y1, double x2,
						  double y2, double x ) {
		return ((y2 - y1) / (x2 - x1) * (x - x1) + y1);
	}

	/**
	*  Converts a positive decimal number to it's binary
	*  equivelant.
	*
	*  @param  decimal  The positive decimal number to be encoded in
	*                   binary.
	*  @param  bits     The bitset to encode the number in.
	**/
	public static final void dec2bin( int decimal, BitSet bits ) {
		if ( decimal < 0 )
			throw new IllegalArgumentException( "Cannot convert a negative number to binary." );
		
		int i = 0;
		int value = decimal;
		while ( value > 0 ) {
			if ( value % 2 > 0 )
				bits.set( i );
			
			else
				bits.clear( i );
			
			value /= 2;
			++i;
		}

		for ( i = i; i < bits.size(); ++i ) {
			bits.clear( i );
		}
	}

	/**
	*  Converts binary number to it's base 10 decimal equivelant.
	*
	*  @param   bits  The bitset that encodes the number to be converted.
	*  @return  Returns the decimal equivelent of the given binary number.
	**/
	public static final long bin2dec( BitSet bits ) {
		long value = 0;
		int length = bits.size();

		for ( int i = 0; i < length; ++i ) {
			if ( bits.get( i ) )
				value += pow2( i );
		}
		return value;
	}

    /**
    *  Return the hyperbolic cosine of the specified argument
    *  in the range MIN_LOG to MAX_LOG.
    *  The hyperbolic cosine is defined as:
    *      cosh(x) = (exp(x) + exp(-x))/2
    *
    *  @param  x  Value to determine hyperbolic cosine of.
    **/
    public static final double cosh(double x) {
        if (Double.isNaN(x))	return Double.NaN;
        if (x < 0)	x = -x;
        if (x > (MAX_LOG + LOG2))	return Double.POSITIVE_INFINITY;
        
        double y=0;
        if (x >= (MAX_LOG - LOG2)) {
            y = Math.exp(0.5*x);
            y = (0.5*y)*y;
            
        } else {
            y = Math.exp(x);
            y = 0.5*y + 0.5/y;
        }
        
        return y;
    }
    
    /**
    *  Return the hyperbolic sine of the specified argument
    *  in the range MIN_LOG to MAX_LOG.
    *  The hyperbolic sine is defined as:
    *      sinh(x) = (exp(x) - exp(-x))/2
    *
    *  @param  x  Value to determine hyperbolic sine of.
    **/
    public static final double sinh(double x) {
        if (Double.isNaN(x))	return Double.NaN;
        if (x == 0)	return 0;
        if ( (x > (MAX_LOG + LOG2)) || (x > -(MIN_LOG - LOG2)) ) {
            if (x > 0)
                return Double.POSITIVE_INFINITY;
            else
                return Double.NEGATIVE_INFINITY;
        }
        
        double a = Math.abs(x);
        if ( a >= (MAX_LOG - LOG2) ) {
            a = Math.exp(0.5*a);
            a = (0.5*a)*a;
            
        } else {
            a = Math.exp(a);
            a = 0.5*a + 0.5/a;
        }
        if (x < 0)
            a = -a;
       
        return a;
    }
    
    /**
    *  Returns the hyperbolic tangent of the specified argument
    *  in the range MIN_LOG to MAX_LOG.
    *  The hyperbolic tangent is defined as:
    *      tanh(x) = sinh(x)/cosh(x) = 1 - 2/(exp(2*x) + 1)
    *
    *  @param  x  Value to determine the hyperbolic tangent of.
    **/
    public static final double tanh(double x) {
        if (Double.isNaN(x))	return Double.NaN;
        if (x == 0)	return 0;
        double z = Math.abs(x);
        if (z > 0.5*MAX_LOG) {
            if (x > 0)
                return 1.0;
            else
                return -1.0;
        }
        
        double s = Math.exp(2*z);
        z = 1.0 - 2.0/(s + 1.0);
        if (x < 0)
            z = -z;
        
        return z;
	}
    
    /**
    *  Returns the inverse hyperbolic cosine of the specified argument.
    *  The inverse hyperbolic cosine is defined as:
    *      acosh(x) = log(x + sqrt( (x-1)(x+1) )
    *
    *  @param x  Value to return inverse hyperbolic cosine of.
    *  @throws IllegalArgumentException if x is less than 1.0. 
    **/
    public static final double acosh(double x) {
        if (Double.isNaN(x))		return Double.NaN;
        if (Double.isInfinite(x))	return x;
        if (x < 1.0)
            throw new IllegalArgumentException("x less than 1.0");
        
        double y = 0;
        if (x > 1.0E8) {
            y = Math.log(x) + LOG2;
            
        } else {
            double a = Math.sqrt( (x - 1.0)*(x + 1.0) );
            y = Math.log(x + a);
        }
        
        return y;
    }
    
    /**
    *  Returns the inverse hyperbolic sine of the specified argument.
    *  The inverse hyperbolic sine is defined as:
    *      asinh(x) = log( x + sqrt(1 + x*x) )
    *
    *  @param xx  Value to return inverse hyperbolic cosine of.
    **/
    public static final double asinh(double xx) {
        if (Double.isNaN(xx))	return Double.NaN;
        if (Double.isInfinite(xx))	return xx;
        if (xx == 0)	return 0;
        
        int sign = 1;
        double x = xx;
        if (xx < 0) {
            sign = -1;
            x = -xx;
        }
        
        double y = 0;
        if (x > 1.0E8) {
            y = sign*(Math.log(x) + LOG2);
            
        } else {
            double a = Math.sqrt(x*x + 1.0);
            y = sign*Math.log(x + a);
        }
    
        return y;
    }
    
    /**
    *  Returns the inverse hyperbolic tangent of the specified argument.
    *  The inverse hyperbolic tangent is defined as:
    *      atanh(x) = 0.5 * log( (1 + x)/(1 - x) )
    *
    *  @param x  Value to return inverse hyperbolic cosine of.
    *  @throws IllegalArgumentException if x is outside the range -1, to +1.
    **/
    public static final double atanh(double x) {
        if (Double.isNaN(x))	return Double.NaN;
        if (x == 0)	return 0;
        
        double z = Math.abs(x);
        if (z >= 1.0) {
            if (x == 1.0)
                return Double.POSITIVE_INFINITY;
            if (x == -1.0)
                return Double.NEGATIVE_INFINITY;
            
            throw new IllegalArgumentException("x outside of range -1 to +1");
        }
        
        if (z < 1.0E-7)	return x;
        
        double y = 0.5*Math.log((1.0 + x)/(1.0 - x));
        
        return y;
    }
    
    
	/**
	*  Returns the absolute value of "a" times the sign of "b".
	**/
	public static final double sign(double a, double b) {
		return Math.abs(a)*(b < 0 ? -1 : 1);
	}
	

	/**
	*  Returns the absolute value of "a" times the sign of "b".
	**/
	public static final float sign(float a, double b) {
		return Math.abs(a)*(b < 0 ? -1 : 1);
	}
	

	/**
	*  Returns the absolute value of "a" times the sign of "b".
	**/
	public static final long sign(long a, double b) {
		return Math.abs(a)*(b < 0 ? -1 : 1);
	}
	

	/**
	*  Returns the absolute value of "a" times the sign of "b".
	**/
	public static final int sign(int a, double b) {
		return Math.abs(a)*(b < 0 ? -1 : 1);
	}
	

	/**
	*  Used to test out the methods in this class.
	**/
	public static void main(String args[]) {
	
		System.out.println();
		System.out.println("Testing MathTools...");
		
		System.out.println("  2 is an " + (even(2) ? "even" : "odd") + " number.");
		System.out.println("  3 is an " + (odd(3) ? "odd" : "even") + " number.");
		System.out.println("  The square of 3.8 is " + sqr(3.8) + ".");
		System.out.println("  The cube root of 125 is " + cubeRoot(125) + ".");
		System.out.println("  The integer 3^7 is " + pow(3,7) + ".");
		System.out.println("  The integer 2^8 is " + pow2(8) + ".");
		System.out.println("  The double 10^-3 is " + pow10(-3) + ".");
		System.out.println("  The base 10 logarithm of 8 is " + log10(8) + ".");
		System.out.println("  The base 2 logarithm of 8 is " + log2(8) + ".");
		System.out.println("  1346.4667 rounded to the nearest 100 is " +
									roundToPlace(1346.4667, 2) + ".");
		System.out.println("  1346.4667 rounded up to the nearest 100 is " +
									roundUpToPlace(1346.4667, 2) + ".");
		System.out.println("  1346.4667 rounded down to the nearest 10 is " +
									roundDownToPlace(1346.4667, 1) + ".");
		System.out.println("  The GCD of 125 and 45 is " + greatestCommonDivisor(125,45) + ".");
		System.out.println("  The fractional part of 3.141593 is " + frac(3.141593) + ".");
        double x = 5;
        System.out.println("  The hyperbolic sine of " + (float)x + " = " + (float)sinh(x) + ".");
        System.out.println("  The hyperbolic cosine of " + (float)x + " = " + (float)cosh(x) + ".");
        x = -.25;
        System.out.println("  The hyperbolic tangent of " + (float)x + " = " + (float)tanh(x) + ".");
        x = cosh(5);
        System.out.println("  The inv. hyperbolic cosine of " + (float)x + " = " + (float)acosh(x) + ".");
        System.out.println("  The inv. hyperbolic sine of " + (float)x + " = " + (float)asinh(x) + ".");
        x = tanh(-0.25);
        System.out.println("  The inv. hyperbolic tangent of " + (float)x + " = " + (float)atanh(x) + ".");
		
		System.out.println("  4.56 with the sign of -6.33 is " + sign(4.56F, -6.33));

	}

}


