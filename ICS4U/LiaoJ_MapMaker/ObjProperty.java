// Jacky Liao
// December 12, 2017
// Maze Game
// ICS4U Ms.Strelkovska

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// The annotation to be applied to classes, to denote that they are an class with properties
@Retention(RetentionPolicy.RUNTIME)
public @interface ObjProperty {
	// Display name of this object
	String name();
	// Which type?
	Type type();
	enum Type {
		NONE,
		POINT,
		LINE,
	}
}
