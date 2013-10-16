package drm.taskworker.workers.geo;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class RegionSortTest {

	@Test
	public void test() {
		Region one = new Region(0, 0,4,4);
		Region[] parts = one.split();
		
		Arrays.sort(parts, new RegionSort());
		assertArrayEquals(parts,one.split());
		
	}

	
	@Test
	public void test1() {
		Region one = new Region(0, 0,4,4);
		Region[] parts = one.split();
		List<Region> p = Arrays.asList(parts);
 		Collections.shuffle(p);
 		parts = (Region[]) p.toArray();
		Arrays.sort(parts, new RegionSort());
		assertArrayEquals(parts,one.split());
		
	}
	
	@Test
	public void test2() {
		Region one = new Region(0, 0,5,4);
		Region[] parts = one.split();
		List<Region> p = Arrays.asList(parts);
 		Collections.shuffle(p);
 		parts = (Region[]) p.toArray();
		Arrays.sort(parts, new RegionSort());
		assertArrayEquals(parts,one.split());
		
	}
	
	@Test
	public void test3() {
		Region one = new Region(0, 7,5,9);
		Region[] parts = one.split();
		List<Region> p = Arrays.asList(parts);
 		Collections.shuffle(p);
 		parts = (Region[]) p.toArray();
		Arrays.sort(parts, new RegionSort());
		assertArrayEquals(parts,one.split());
		
	}

}
