package com.vin.easyknack.model;

/**
 * Stable IDs and presentation semantics shared with the C++ MathNode.
 *
 * Operation-specific defaults describe the desired automatic value. They must
 * NOT overwrite an existing user value merely because the operation changes.
 */
public enum MathOperation {

	// Group group, String id, String label, String tooltip, boolean unary, Operand a, Operand b)
	// Arithmetic
	ADD(Group.ARITHMETIC, "add", "Add", "Adds A and B.", false, operand("A", 0.0), operand("B", 0.0)),
	SUBTRACT(Group.ARITHMETIC, "subtract", "Subtract", "Subtracts B from A.", false, operand("A", 0.0),
			operand("B", 0.0)),
	MULTIPLY(Group.ARITHMETIC, "multiply", "Multiply", "Multiplies A by B.", false, operand("A", 0.0),
			operand("B", 1.0)),
	DIVIDE(Group.ARITHMETIC, "divide", "Divide",
			"Divides Dividend by Divisor. Returns 0 when Divisor is effectively zero.", false, operand("Dividend", 0.0),
			operand("Divisor", 1.0)),
	POWER(Group.ARITHMETIC, "power", "Power", "Raises Base to Exponent.", false, operand("Base", 1.0),
			operand("Exponent", 2.0, -16.0, 16.0)),
	// Remainder / wrapping
	REMAINDER(Group.REMAINDER, "remainder", "Remainder",
			"C-style floating remainder. The result follows the sign of Value.", false, operand("Value", 0.0),
			operand("Divisor", 1.0)),
	MODULO(Group.REMAINDER, "modulo", "Modulo",
			"Floor-based modulo. With a positive modulus, the result is always non-negative.", false,
			operand("Value", 0.0), operand("Modulus", 1.0)),
	// Range
	MINIMUM(Group.RANGE, "minimum", "Minimum", "Returns the smaller of A and B.", false, operand("A", 0.0),
			operand("B", 1.0)),
	MAXIMUM(Group.RANGE, "maximum", "Maximum", "Returns the larger of A and B.", false, operand("A", 0.0),
			operand("B", 1.0)),
	// Rounding
	FLOOR(Group.ROUNDING, "floor", "Floor", "Rounds Value down to the next integer.", true, operand("Value", 0.0),
			null),
	CEIL(Group.ROUNDING, "ceil", "Ceil", "Rounds Value up to the next integer.", true, operand("Value", 0.0), null),
	ROUND(Group.ROUNDING, "round", "Round", "Rounds Value to the nearest integer.", true, operand("Value", 0.0), null),
	FRACT(Group.ROUNDING, "fract", "Fraction", "Returns Value - floor(Value), producing a fraction in [0, 1).", true,
			operand("Value", 0.0), null),
	// Powers / logarithms
	SQRT(Group.EXPONENTIAL, "sqrt", "Square Root", "Returns the square root of Value. Negative real inputs return 0.",
			true, operand("Value", 0.0, 0.0, 1000000.0), null),
	ROOT(Group.EXPONENTIAL, "root", "Root", "Returns the Degree-th root of Value. Degree 0 is undefined.", false,
			operand("Value", 0.0), operand("Degree", 2.0, -16.0, 16.0)),
	EXP(Group.EXPONENTIAL, "exp", "Exp (e^x)", "Returns e raised to Exponent.", true,
			operand("Exponent", 0.0, -16.0, 16.0), null),
	LOG(Group.EXPONENTIAL, "log", "Natural Log", "Returns ln(Value). Value must be greater than 0.", true,
			operand("Value", 1.0, 0.000001, 1000000.0), null),
	LOG_BASE(Group.EXPONENTIAL, "log_base", "Log Base",
			"Returns the logarithm of Value using Base. Base must be positive and not equal to 1.", false,
			operand("Value", 1.0, 0.000001, 1000000.0), operand("Base", 10.0, 0.000001, 1000000.0)),
	// Unary
	ABSOLUTE(Group.UNARY, "absolute", "Absolute", "Returns the absolute value of Value.", true, operand("Value", 0.0),
			null),
	NEGATE(Group.UNARY, "negate", "Negate", "Returns Value with its sign inverted.", true, operand("Value", 0.0), null);

	public enum Group {
		ARITHMETIC, REMAINDER, RANGE, ROUNDING, EXPONENTIAL, UNARY
	}

	public record Operand(String label, double defaultValue, double min, double max) {
	}

	private static Operand operand(String label, double defaultValue) {
		return new Operand(label, defaultValue, -1000000.0, 1000000.0);
	}

	private static Operand operand(String label, double defaultValue, double min, double max) {
		return new Operand(label, defaultValue, min, max);
	}

	private final Group group;
	private final String id;
	private final String label;
	private final String tooltip;
	private final boolean unary;
	private final Operand a;
	private final Operand b;

	MathOperation(Group group, String id, String label, String tooltip, boolean unary, Operand a, Operand b) {
		this.group = group;
		this.id = id;
		this.label = label;
		this.tooltip = tooltip;
		this.unary = unary;
		this.a = a;
		this.b = b;
	}

	public Group group() {
		return group;
	}

	public String id() {
		return id;
	}

	public String tooltip() {
		return com.vin.easyknack.ui.AppText.literal(tooltip);
	}

	public boolean unary() {
		return unary;
	}

	public Operand a() {
		return a;
	}

	public Operand b() {
		return b;
	}

	@Override
	public String toString() {
		return com.vin.easyknack.ui.AppText.literal(label);
	}

	public static MathOperation fromId(String id) {
		return java.util.Arrays.stream(values()).filter(value -> value.id.equals(id)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Unknown math operation: ") + id));
	}
}