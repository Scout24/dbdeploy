package com.dbdeploy;

import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class ConstraintUtilsTest {

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionWhenGivenObjectIsNull() {
		ConstraintUtils.ensureNotNull("argumentName", null);
	}
	
	@Test
	public void shouldReturnObjectIfGivenObjectIsNotNull() {
		final String someObject = "Hello world";
		
		final String actualObject = ConstraintUtils.ensureNotNull("someObject", someObject);
		
		assertSame(someObject, actualObject);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionWhenLongIsLessThanZero() {
		ConstraintUtils.ensureGreaterThanZero("argumentName", -1);
	}
	
	@Test
	public void shouldReturnLongWhenItIsGreaterOrEqualToZero() {
		assertEquals(0, ConstraintUtils.ensureGreaterThanZero("zero", 0));
		assertEquals(1, ConstraintUtils.ensureGreaterThanZero("one", 1));
	}
}
