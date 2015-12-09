package uk.warwick.tools.general;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for RectangleBox
 * @author p.baniukiewicz
 * @date 09 Dec 2015
 */
public class RectangleBoxTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * @test Test of RectangleBox for square image and angle 0 deg
	 */
	@Test
	public void testRectangleBox_0s() {
		int width = 512;
		int height = 512;
		double angle = 0;
		
		RectangleBox r = new RectangleBox(width,height);
		
		assertEquals(512, r.getWidth(),0);
		assertEquals(512, r.getHeight(),0);
	}
	
	/**
	 * @test Test of RectangleBox for square image and angle 90 deg
	 */
	@Test
	public void testRectangleBox_90s() {
		int width = 512;
		int height = 512;
		double angle = 90;
		
		RectangleBox r = new RectangleBox(width,height);
		r.rotateBoundingBox(angle);
		
		assertEquals(512, r.getWidth(),0);
		assertEquals(512, r.getHeight(),0);
	}

	/**
	 * @test Test of RectangleBox for non square image and angle 90 deg
	 */
	@Test
	public void testRectangleBox_90ns() {
		int width = 512;
		int height = 1024;
		double angle = 90;
		
		RectangleBox r = new RectangleBox(width,height);
		r.rotateBoundingBox(angle);
		
		assertEquals(1024, Math.round(r.getWidth()),0);
		assertEquals(512, Math.round(r.getHeight()),0);
	}
	
	/**
	 * @test Test of RectangleBox for square image and angle 45 deg
	 * @post
	 * Expected values were read from IJ after rotating test image. ImageJ add +1 for every length
	 * (tested by rotating by 45 deg) thus expected values are smaller by 1 comparing to IJ
	 */
	@Test
	public void testRectangleBox_45s() {
		int width = 512;
		int height = 512;
		double angle = 45;
		
		RectangleBox r = new RectangleBox(width,height);
		r.rotateBoundingBox(angle);
		
		assertEquals(724, Math.round(r.getWidth()),0);
		assertEquals(724, Math.round(r.getHeight()),0);
	}
	
	/**
	 * @test Test of RectangleBox for square image and angle 30 deg
	 * @post
	 * Expected values were read from IJ after rotating test image. ImageJ add +1 for every length
	 * (tested by rotating by 45 deg) thus expected values are smaller by 1 comparing to IJ
	 */
	@Test
	public void testRectangleBox_30s() {
		int width = 512;
		int height = 512;
		double angle = 30;
		
		RectangleBox r = new RectangleBox(width,height);
		r.rotateBoundingBox(angle);
		
		assertEquals(699, Math.round(r.getWidth()),0);
		assertEquals(699, Math.round(r.getHeight()),0);
	}

}
