// Jacky Liao
// December 12, 2017
// Maze Game
// ICS4U Ms.Strelkovska

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// Annotation for easy GUI creation
@Retention(RetentionPolicy.RUNTIME)
public @interface GUIProperty {
	// Type of field
	Type type();
	// Name of field
	String name();

	double rangeH() default 1;
	double rangeL() default 0;
	double max() default Double.POSITIVE_INFINITY;
	double min() default Double.NEGATIVE_INFINITY;

	// Type of GUI control
	enum Type {
		COLOR,
		NUMERIC,
		STRING,
		CHECK,
		ANGLE,
	}
}
