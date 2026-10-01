package com.vin.easyknack.model;

public enum ComparatorOperation {

	LESS("less"), LESS_EQUAL("less_equal"), GREATER("greater"), GREATER_EQUAL("greater_equal"), EQUAL("equal"),
	NOT_EQUAL("not_equal");

	private final String id;

	ComparatorOperation(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}

	public static ComparatorOperation fromId(String id) {
		for (var operation : values()) {
			if (operation.id.equals(id))
				return operation;
		}

		return LESS;
	}

	@Override
	public String toString() {
		return switch (this) {
		case LESS -> "<";
		case LESS_EQUAL -> "<=";
		case GREATER -> ">";
		case GREATER_EQUAL -> ">=";
		case EQUAL -> "==";
		case NOT_EQUAL -> "!=";
		};
	}
}
