package com.dbdeploy;

import java.text.MessageFormat;

public class ConstraintUtils {

	public static <E> E ensureNotNull(final String argumentName, E argumentValue) {
		if (argumentValue == null) {
			final String message = MessageFormat.format("Argument {0} should not be null!", argumentName);
			throw new IllegalArgumentException(message);
		}
		return argumentValue;
	}

	public static long ensureGreaterThanZero(final String argumentName, final long argumentValue) {
		if (argumentValue < 0) {
			final String message = MessageFormat.format("Argument {0} has to be greater than zero!", argumentName);
			throw new IllegalArgumentException(message);
		}
		return argumentValue;
	}

}
