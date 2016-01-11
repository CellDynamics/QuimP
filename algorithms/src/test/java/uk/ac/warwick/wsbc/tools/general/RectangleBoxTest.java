package uk.ac.warwick.wsbc.tools.general;

import static org.junit.Assert.*;

import java.util.Vector;

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
	public void TestRectangleBox_0s() {
		int width = 512;
		int height = 512;
		double angle = 0;
		
		RectangleBox r = new RectangleBox(width,height);
		r.rotateBoundingBox(angle);
		
		assertEquals(512, r.getWidth(),0);
		assertEquals(512, r.getHeight(),0);
	}
	
	/**
	 * @throws Exception 
	 * @test Test of RectangleBox for square image and angle 0 deg for input vectors
	 */
	@Test
	public void TestRectangleBoxVector_0s() throws Exception {
		
		Vector<Double> x = new Vector<Double>();
		Vector<Double> y = new Vector<Double>();
		
		x.add(-10.0);x.add(10.0);x.add(10.0);x.add(-10.0);
		y.add(10.0);y.add(10.0);y.add(-10.0);y.add(-10.0);
		
		double angle = 0;
		
		RectangleBox r = new RectangleBox(x,y);
		r.rotateBoundingBox(angle);
		
		assertEquals(20, r.getWidth(),0);
		assertEquals(20, r.getHeight(),0);
	}
	
	/**
	 * @throws Exception 
	 * @test Test of RectangleBox for square image and angle 45 deg for input vectors
	 */
	@Test
	public void TestRectangleBoxVector_45s() throws Exception {
		
		Vector<Double> x = new Vector<Double>();
		Vector<Double> y = new Vector<Double>();
		
		x.add(-10.0);x.add(10.0);x.add(10.0);x.add(-10.0);
		y.add(10.0);y.add(10.0);y.add(-10.0);y.add(-10.0);
		
		double angle = 45;
		
		RectangleBox r = new RectangleBox(x,y);
		r.rotateBoundingBox(angle);
		
		assertEquals(28, Math.round(r.getWidth()),0);
		assertEquals(28, Math.round(r.getHeight()),0);
	}
	
	/**
	 * @test Test of RectangleBox for square image and angle 90 deg
	 */
	@Test
	public void TestRectangleBox_90s() {
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
	public void TestRectangleBox_90ns() {
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
	 */
	@Test
	public void TestRectangleBox_45s() {
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
	 */
	@Test
	public void TestRectangleBox_30s() {
		int width = 512;
		int height = 512;
		double angle = 30;
		
		RectangleBox r = new RectangleBox(width,height);
		r.rotateBoundingBox(angle);
		
		assertEquals(699, Math.round(r.getWidth()),0);
		assertEquals(699, Math.round(r.getHeight()),0);
	}

}