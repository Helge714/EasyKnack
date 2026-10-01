package com.vin.easyknack.model;

public enum EdgeMode {

	RISING("rising", "Rising"), FALLING("falling", "Falling"), BOTH("both", "Both");

	private final String id;
	private final String label;

	EdgeMode(String id, String label) {
		this.id = id;
		this.label = label;
	}

	public String id() {
		return id;
	}

	@Override
	public String toString() {
		return com.vin.easyknack.ui.AppText.literal(label);
	}

	public static EdgeMode fromId(String id) {
		return java.util.Arrays.stream(values()).filter(value -> value.id.equals(id)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Unknown edge mode: ") + id));
	}
}