import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface GUIProperty {
	Type type();
	String name();

	double rangeH() default 1;
	double rangeL() default 0;
	double max() default Double.POSITIVE_INFINITY;
	double min() default Double.NEGATIVE_INFINITY;

	enum Type {
		COLOR,
		NUMERIC,
		STRING,
		CHECK,
		ANGLE,
	}
}
