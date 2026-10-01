package com.vin.easyknack.audio;

import java.lang.ref.WeakReference;
import java.util.*;

public final class AudioAnalysis {

	public static final int MAX_VALUES = 8193;

	public record Frame(double frequency, double confidence, double span, boolean symmetric, float[] values) {

		public Frame(double frequency, double confidence, double span, float[] values) {
			this(frequency, confidence, span, false, values);
		}
	}

	private static Map<String, java.util.List<String>> aliases = Map.of();

	public static void aliases(Map<String, java.util.List<String>> value) {
		aliases = Map.copyOf(value);
	}

	private static final Map<String, Frame> frames = new HashMap<>();

	private static final List<WeakReference<Runnable>> listeners = new ArrayList<>();

	private AudioAnalysis() {
	}

	public static Frame frame(String id) {

		var direct = frames.get(id);

		if (direct != null) {
			return direct;
		}

		var resolved=aliases.getOrDefault(id, List.of()).stream().sorted().map(frames::get).filter(Objects::nonNull).findFirst().orElse(null);
        if(resolved!=null)return resolved;
        // A source-plugin readout refers to a processor inside its host node.
        // Resolve the host's flattened identity before appending that local path.
        int split=id.indexOf('/');
        if(split>0)return aliases.getOrDefault(id.substring(0,split),List.of()).stream().sorted()
            .map(host->frames.get(host+id.substring(split))).filter(Objects::nonNull).findFirst().orElse(null);
        return null;
	}

	public static void listen(Runnable listener) {
		listeners.add(new WeakReference<>(listener));
	}

	public static Map<String, Frame> parse(String response) {

		if (response == null || response.isBlank()) {
			throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Invalid analysis snapshot"));
		}

		String[] tokens = response.trim().split("\\s+");

		if (tokens.length == 0 || !tokens[0].equals("OK")) {
			throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Invalid analysis snapshot"));
		}

		/*
		 * Protocol V2.
		 */
		if (tokens.length >= 2 && tokens[1].equals("2")) {
			return parseV2(tokens);
		}

		/*
		 * Temporary backwards compatibility with the original fixed 256-value protocol.
		 */
		return parseLegacy(tokens);
	}

	private static Map<String, Frame> parseV2(String[] tokens) {

		var result = new HashMap<String, Frame>();

		int i = 2;

		while (i < tokens.length) {

			/*
			 * Frame header:
			 *
			 * id frequency confidence span symmetric valueCount
			 */
			if (tokens.length - i < 6) {
				throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Invalid analysis snapshot"));
			}

			String id = tokens[i++];

			double frequency = parseDouble(tokens[i++]);

			double confidence = parseDouble(tokens[i++]);

			double span = parseDouble(tokens[i++]);

			int symmetricFlag = parseInt(tokens[i++]);

			if (symmetricFlag != 0 && symmetricFlag != 1) {
				throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Invalid analysis symmetry flag"));
			}

			boolean symmetric = symmetricFlag != 0;

			int count = parseInt(tokens[i++]);

			if (count < 0 || count > MAX_VALUES || i + count > tokens.length) {
				throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Invalid analysis value count"));
			}

			float[] values = new float[count];

			for (int n = 0; n < count; ++n) {

				values[n] = parseFloat(tokens[i++]);
			}

			result.put(id, new Frame(frequency, confidence, span, symmetric, values));
		}

		return result;
	}

	private static Map<String, Frame> parseLegacy(String[] tokens) {

		/*
		 * Original format:
		 *
		 * id + frequency + confidence + span + 256 values
		 *
		 * = 260 tokens per frame.
		 */
		if ((tokens.length - 1) % 260 != 0) {
			throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Invalid analysis snapshot"));
		}

		var result = new HashMap<String, Frame>();

		for (int i = 1; i < tokens.length; i += 260) {

			float[] values = new float[256];

			for (int n = 0; n < 256; ++n) {

				values[n] = parseFloat(tokens[i + 4 + n]);
			}

			result.put(tokens[i], new Frame(parseDouble(tokens[i + 1]), parseDouble(tokens[i + 2]),
					parseDouble(tokens[i + 3]), false, values));
		}

		return result;
	}

	private static double parseDouble(String value) {

		double result = Double.parseDouble(value);

		if (!Double.isFinite(result)) {
			throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Non-finite analysis value"));
		}

		return result;
	}

	private static float parseFloat(String value) {

		float result = Float.parseFloat(value);

		if (!Float.isFinite(result)) {
			throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Non-finite analysis value"));
		}

		return result;
	}

	private static int parseInt(String value) {

		return Integer.parseInt(value);
	}

	public static void publish(Map<String, Frame> next) {

		frames.putAll(next);

		if (frames.size() > 256) {

			frames.clear();
			frames.putAll(next);
		}

		listeners.removeIf(ref -> {

			var listener = ref.get();

			if (listener == null) {
				return true;
			}

			listener.run();
			return false;
		});
	}

	public static void clear() {

		frames.clear();

		publish(Map.of());
	}
}
