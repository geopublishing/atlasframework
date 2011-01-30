package org.geopublishing.geopublisher;

import java.awt.Container;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.netbeans.spi.wizard.ResultProgressHandle;

/**
 * This implementation of a {@link ResultProgressHandle} will pipe all output to
 * the logger. If not {@link Logger} is given, it outputs to sysout
 */
@Ignore
public class TestResultProgressHandle implements ResultProgressHandle {

	public TestResultProgressHandle() {
	}

	public TestResultProgressHandle(Logger logger) {
		this.logger = logger;
	}

	private Logger logger = null;

	@Override
	public void setProgress(int currentStep, int totalSteps) {

	}

	@Override
	public void setProgress(String description, int currentStep, int totalSteps) {
	}

	@Override
	public void setBusy(String description) {
		final String msg = "progress busy: " + description;
		if (logger != null) {
			logger.debug(msg);
		} else
			System.out.println(msg);
	}

	@Override
	public void finished(Object result) {
		final String msg = "progress finished with: " + result;
		if (logger != null) {
			logger.debug(msg);
		} else
			System.out.println(msg);
	}

	@Override
	public void failed(String message, boolean canNavigateBack) {
		final String msg = "progress failed with: " + message;
		if (logger != null) {
			logger.error(msg);
		} else
			System.err.println(msg);

	}

	@Override
	public void addProgressComponents(Container panel) {
	}

	@Override
	public boolean isRunning() {
		throw new RuntimeException("not yet implemented");
	}

}
