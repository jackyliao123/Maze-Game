import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ObjProperty {
	String name();
	Type type();
	enum Type {
		NONE,
		POINT,
		LINE,
	}
}
