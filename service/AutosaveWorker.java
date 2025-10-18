package service;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AutosaveWorker implements AutoSavable {

	private final Runnable saveAction;
	private Timer timer;
	private boolean enabled;
	private int intervalMinutes;

	public AutosaveWorker(Runnable saveAction, int defaultIntervalMinutes) {
		this.saveAction = saveAction;
		this.intervalMinutes = Math.max(1, defaultIntervalMinutes);
		this.enabled = false;
	}

	@Override
	public void enableAutosave(int intervalMinutes) {
		this.intervalMinutes = Math.max(1, intervalMinutes);
		this.enabled = true;
		restartTimer();
	}

	@Override
	public void disableAutosave() {
		this.enabled = false;
		stopTimer();
	}

	@Override
	public boolean isAutosaveEnabled() {
		return enabled;
	}

	private void restartTimer() {
		stopTimer();
		if (!enabled) return;
		int delayMs = Math.max(1, intervalMinutes) * 60 * 1000;
		timer = new Timer(delayMs, (ActionEvent e) -> saveAction.run());
		timer.setRepeats(true);
		timer.start();
	}

	private void stopTimer() {
		if (timer != null) {
			timer.stop();
			timer = null;
		}
	}
}



