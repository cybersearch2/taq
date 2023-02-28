package com.j256.simplelogging;

/**
 * "Fluent" logger which uses method chaining to support the logging calls. This causes no additional object if the
 * logging level is not enabled -- even when using primitives. Stole the idea (not the implementation) from Google's
 * fluent logging (https://google.github.io/flogger/). You get a fluent-logger by calling
 * {@link LoggerFactory#getFluentLogger(String)}.
 * 
 * For example, even with port being an int primitive below, there are no objects generated by thus call unless TRACE
 * log level is enabled. If TRACE is enabled then there is an additional @{link {@link FluentContext} object which holds
 * the message, throwable, and arguments.
 * 
 * <pre>
 * fluentLogger.atLevel(Level.TRACE) <!-- -->
 *   .msg("connected to host '{}' port '{}'") <!-- -->
 *   .arg(host) <!-- -->
 *   .arg(port) <!-- -->
 *   .log();
 * </pre>
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * @author graywatson
 */
public class FluentLogger extends BaseLogger {

	public FluentLogger(LogBackend backend) {
		super(backend);
	}

	/**
	 * Start of the chaining that sets the log level of the message. If this level is not enabled then the method
	 * returns a mute context singleton and all calls to it are no-ops.
	 */
	public FluentContext atLevel(Level level) {
		if (isLevelEnabled(level)) {
			return new FluentContextImpl(this, level);
		} else {
			return MuteContext.SINGLETON;
		}
	}

	/**
	 * Context that doesn't do anything. This is returned when the log level is not enabled.
	 */
	private static class MuteContext implements FluentContext {

		/** singleton instance of the context that is used by all calls when level not enabled */
		public static final MuteContext SINGLETON = new MuteContext();

		@Override
		public FluentContext msg(String message) {
			return this;
		}

		@Override
		public FluentContext throwable(Throwable th) {
			return this;
		}

		@Override
		public FluentContext arg(Object arg) {
			return this;
		}

		@Override
		public FluentContext arg(boolean arg) {
			return this;
		}

		@Override
		public FluentContext arg(byte arg) {
			return this;
		}

		@Override
		public FluentContext arg(char arg) {
			return this;
		}

		@Override
		public FluentContext arg(short arg) {
			return this;
		}

		@Override
		public FluentContext arg(int arg) {
			return this;
		}

		@Override
		public FluentContext arg(long arg) {
			return this;
		}

		@Override
		public FluentContext arg(float arg) {
			return this;
		}

		@Override
		public FluentContext arg(double arg) {
			return this;
		}

		@Override
		public FluentContext args(Object[] args) {
			return this;
		}

		@Override
		public void log() {
			// no-op
		}
	}
}