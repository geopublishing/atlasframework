package org.geopublishing.atlasStyler;

public enum MARKTYPE {
	circle, square, triangle, star, cross, external_graphic,
	// internal_graphic,
	shape_vertline, shape_horline, shape_slash, shape_backslash, shape_dot, shape_plus, shape_times;

	public static MARKTYPE readWellKnownName(String s) {
		final String ss = s.toLowerCase();

		if (ss.equals("shape://backslash"))
			return shape_backslash;
		if (ss.equals("shape://slash"))
			return shape_slash;
		if (ss.equals("shape://dot"))
			return shape_dot;
		if (ss.equals("shape://times"))
			return shape_times;
		if (ss.equals("shape://plus"))
			return shape_plus;
		if (ss.equals("shape://vertline"))
			return shape_vertline;
		if (ss.equals("shape://horline"))
			return shape_horline;

		if (ss.equals("hatch"))
			return shape_slash;

		return MARKTYPE.valueOf(s);
	}

	public String toWellKnownName() {

		switch (this) {
		case shape_backslash:
			return "shape://backslash";
		case shape_slash:
			return "shape://slash";
		case shape_dot:
			return "shape://dot";
		case shape_times:
			return "shape://times";
		case shape_plus:
			return "shape://plus";
		case shape_vertline:
			return "shape://vertline";
		case shape_horline:
			return "shape://horline";
		default:
			return super.toString();
		}

	}
};
