/*
 * Copyright (c) 2008, Keith Woodward
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of Keith Woodward nor the names
 *    of its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package straightedge.geom.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A convenience class to do code profiling.
 *
 * @author Keith Woodward, Ryan M
 *
 */
public class CodeTimer {

	private static DecimalFormat fourDP = new DecimalFormat("0.####");
	private static DecimalFormat percent = new DecimalFormat("0.##%");
	private static long SELF_TIME;
	public static final long NANOS_IN_A_SECOND = 1000000000;
	public static final long NANOS_IN_A_MILLISECOND = 1000000;

	static {
		recalibrate(10000);
	}
	/**
	 * The name of this {@link CodeTimer}, to identify the output
	 */
	public final String name;
	/**
	 * Defaults to <code>true</code>
	 */
	protected boolean enabled = true;
	private long count = 0;
	private long totalNanos = 0;
	private ArrayList<Long> clicks = new ArrayList<Long>();
	private ArrayList<String> clickNames = new ArrayList<String>();
	private ArrayList<String> allClickNames = new ArrayList<String>();
	private ArrayList<Long> intervalsBetweenClicks = new ArrayList<Long>();
	private long lastPrintOutNanos = System.nanoTime();
	/**
	 * The number of seconds between prints, defaults to 5
	 */
	public int printFrequencySeconds = 5;
	/**
	 * The level of information printed for the total time spent in a
	 * profiling period
	 */
	public Output period;
	/**
	 * The level of information printed for time taken in intervals
	 */
	public Output interval;

	/**
	 * Time unit that is printed
	 *
	 * @author ryanm
	 */
	public enum Output {

		/**
		 * Second-level granularity
		 */
		Seconds {

			@Override
			public String format(long totalNanos, long count) {
				double avTotalSeconds = (double) totalNanos / (count * NANOS_IN_A_SECOND);
				return fourDP.format(avTotalSeconds) + "s (count = "+count+", time = "+fourDP.format((double)totalNanos/NANOS_IN_A_SECOND)+"s)";
			}
		},
		/**
		 * Millisecond-level granularity
		 */
		Millis {

			@Override
			public String format(long totalNanos, long count) {
				double avTotalMillis = (double) totalNanos / (count * NANOS_IN_A_MILLISECOND);
				return fourDP.format(avTotalMillis) + "ms (count = "+count+", time = "+fourDP.format((double)totalNanos/NANOS_IN_A_MILLISECOND)+"ms)";
			}
		},
		/**
		 * Nanosecond-level granularity
		 */
		Nanos {

			@Override
			public String format(long totalNanos, long count) {
				double avTotalNanos = (double) totalNanos / count;
				return fourDP.format(avTotalNanos) + "ns (count = "+count+", time = "+fourDP.format(avTotalNanos)+"ns)";
			}
		};

		/**
		 * @param totalNanos
		 * @param count
		 * @return A string describing the average time
		 */
		public abstract String format(long totalNanos, long count);
	};

	/**
	 * Default constructor that uses Nanosecond outputs and is named "Untitled".
	 */
	public CodeTimer() {
		this.name = "Untitled";
		this.period = Output.Nanos;
		this.interval = Output.Nanos;
	}

	public CodeTimer(String name) {
		this.name = name;
		this.period = Output.Nanos;
		this.interval = Output.Nanos;
	}

	/**
	 * @param name
	 *           A name for this {@link CodeTimer}, so as to identify
	 *           the output
	 * @param period
	 *           output for profiling period duration, or
	 *           <code>null</code> for none. Note that if you specify
	 *           null, you'll be getting output on stdOut that is not
	 *           labelled as coming from this class
	 * @param interval
	 *           output for interval durations, or <code>null</code>
	 *           for just the percentage of period time taken
	 */
	public CodeTimer(String name, Output period, Output interval) {
		this.name = name;
		this.period = period;
		this.interval = interval;
	}

	/**
	 * Call to start a profiling period, or to start an interval in an
	 * open period
	 *
	 * @param name
	 *           A helpful name for this interval. Makes it easy to
	 *           find what bit of code you're measuring
	 */
	public void click(String name) {
		//enabled = false;
		if (enabled) {
			long clickTime = System.nanoTime();
			clicks.add(new Long(clickTime));
			clickNames.add(name);
		}
	}

	/**
	 * Call to end a profiling period, and print the results if
	 * {@link #printFrequencySeconds} have passed since we last printed
	 */
	public void lastClick() {
		if (enabled) {
			click("end");
			checkForNewNames();
			storeIntervals();
			if (System.nanoTime() - lastPrintOutNanos > printFrequencySeconds * NANOS_IN_A_SECOND) {
				printAndResetResults();
			}
			clickNames.clear();
		}
	}

	/**
	 * Call to end a profiling period
	 *
	 * @param print
	 *           <code>true</code> to print results, <code>false</code>
	 *           no to
	 */
	public void lastClick(boolean print) {
		if (enabled) {
			click("end");
			checkForNewNames();
			storeIntervals();
			if (print) {
				printAndResetResults();
			}
			
			clickNames.clear();
		}
	}

	protected void checkForNewNames(){
		// no need to add the last name so i < clickNames.size() - 1
		for (int i = 0; i < clickNames.size() - 1; i++){
			String possibleNewName = clickNames.get(i);
			boolean newName = true;
			for (int j = 0; j < allClickNames.size(); j++){
				if (allClickNames.get(j).equals(possibleNewName)){
					newName = false;
					break;
				}
			}
			if (newName){
//				System.out.println(this.getClass().getSimpleName()+": checkForNewNames added: "+possibleNewName);
				allClickNames.add(possibleNewName);
			}
		}
	}

	protected void storeIntervals() {
		totalNanos += (clicks.get(clicks.size() - 1).longValue()
				- clicks.get(0).longValue())
				- (clicks.size() - 1) * SELF_TIME;
		for (int i = 0; i < clicks.size() - 1; i++) {
			long newInterval = clicks.get(i + 1).longValue() - clicks.get(i).longValue() - SELF_TIME;
			if (i == intervalsBetweenClicks.size()) {
				intervalsBetweenClicks.add(new Long(newInterval));
			} else {
				long sumOfIntervals = intervalsBetweenClicks.get(i).longValue();
				sumOfIntervals += newInterval;
				intervalsBetweenClicks.set(i, new Long(sumOfIntervals));
			}
		}
		clicks.clear();
		count++;
	}

	public void printAndResetResults() {
		if (period != null) {
			System.out.println(getClass().getSimpleName() + " " + name + " : mean period = "
					+ period.format(totalNanos, count));
		}

		if (intervalsBetweenClicks.size() > 1) {
			String[][] strings = new String[intervalsBetweenClicks.size()][3];
			int maxLength0 = 0;
			int maxLength1 = 0;
			int maxLength2 = 0;
			for (int i = 0; i < intervalsBetweenClicks.size(); i++) {
				long intervalNanos = intervalsBetweenClicks.get(i).longValue();
				double intervalAsProportionOfTotal = (double) intervalNanos / totalNanos;
				String nameStr = (i >= allClickNames.size() ? "" : allClickNames.get(i));	// if two of the same names were used in click(), then the second identical name wouldn't have been added so there won't be enough names...
				String percentStr = percent.format(intervalAsProportionOfTotal);
				String intervalStr = (interval != null ? interval.format(intervalNanos, count) : "");

				strings[i][0] = nameStr;
				strings[i][1] = percentStr;
				strings[i][2] = intervalStr;
				if (strings[i][0].length() > maxLength0) {
					maxLength0 = strings[i][0].length();
				}
				if (strings[i][1].length() > maxLength1) {
					maxLength1 = strings[i][1].length();
				}
				if (strings[i][2].length() > maxLength2) {
					maxLength2 = strings[i][2].length();
				}
			}
			int extraSpaces = 4;
			for (int i = 0; i < strings.length; i++) {
				System.out.println(stringSpaces(extraSpaces)
						+ strings[i][0]
						+ stringSpaces(maxLength0 - strings[i][0].length() + extraSpaces)
						+ strings[i][1]
						+ stringSpaces(maxLength1 - strings[i][1].length() + extraSpaces)
						+ strings[i][2]);
			}

		}

		totalNanos = 0;
		count = 0;
		lastPrintOutNanos = System.nanoTime();
		intervalsBetweenClicks.clear();
	}

	protected String stringSpaces(int numSpaces) {
		StringBuilder spaces = new StringBuilder();
		for (int i = 0; i < numSpaces; i++) {
			spaces.append(" ");
		}
		return spaces.toString();
	}

	/**
	 * Calibrates the timer for the machine
	 *
	 * @param numTests
	 *           10000 might be about right
	 */
	public static void recalibrate(int numTests) {
		boolean print = false;
		CodeTimer codeTimer = new CodeTimer();
		// warm the JIT
		for (int i = 0; i < 1024; i++) {
			codeTimer.click("foo");
			codeTimer.lastClick();
		}


		// find how out long it takes to call click(), so that time can be accounted for
		ArrayList<Long> selfTimeObservations = new ArrayList<Long>(numTests);
		for (int i = 0; i < numTests; i++) {
			long nanoSelfTime = -(System.nanoTime() - System.nanoTime());
			long t0 = System.nanoTime();
			codeTimer.click("foo");
			long t1 = System.nanoTime();
			// lastClick is not tested since it really just calls click(),
			// but it is called here so that the CodeTimer's time and name lists do not get too big.
			codeTimer.lastClick();
			long currentSelfTime = t1 - t0 - nanoSelfTime;
			if (print) {
				System.out.println(CodeTimer.class.getSimpleName() + ": (t1 - t0) == " + (t1 - t0) + ", nanoSelfTime == " + nanoSelfTime + ", currentSelfTime == " + currentSelfTime);
			}
			selfTimeObservations.add(currentSelfTime);
		}
		// sort the times
		Collections.sort(selfTimeObservations);
		if (print) {
			for (int i = 0; i < selfTimeObservations.size(); i++) {
				System.out.println(CodeTimer.class.getSimpleName() + ": selfTimeObservations.get(i) == " + selfTimeObservations.get(i));
			}
		}
		// cut out the slowest 5% which are assumed to be outliers
		for (int i = 0; i < (int) (numTests * 0.05); i++) {
			selfTimeObservations.remove(0);
		}
		// cut out the fastest 5% which are assumed to be outliers
		for (int i = 0; i < (int) (numTests * 0.05); i++) {
			selfTimeObservations.remove(selfTimeObservations.size() - 1);
		}
		if (print) {
			System.out.println(CodeTimer.class.getSimpleName() + ": Slimmed list: selfTimeObservations.size() == " + selfTimeObservations.size());
			for (int i = 0; i < selfTimeObservations.size(); i++) {
				System.out.println(CodeTimer.class.getSimpleName() + ": selfTimeObservations.get(i) == " + selfTimeObservations.get(i));
			}
		}
		// find the average
		long sumOfSelfTimes = 0;
		for (int i = 0; i < selfTimeObservations.size(); i++) {
			sumOfSelfTimes += selfTimeObservations.get(i);
		}
		SELF_TIME = sumOfSelfTimes / selfTimeObservations.size();
		if (print) {
			System.out.println(CodeTimer.class.getSimpleName() + ": SELF_TIME == " + SELF_TIME);
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public static void main(String[] args) {
		// Test to see if the manual profiling method using System.nanoTime()'s gives
		// the same results as the convenience class CodeTimer.
		// The first test does manual profiling and prints the results,
		// the second test uses the CodeTimer.
		long testRunningTimeNanos = 11 * NANOS_IN_A_SECOND;	// run each test for 11 seconds.

		java.util.Random random = new java.util.Random(0);
		// warm the JIT compiler
		for (int i = 0; i < 1024; i++) {
			float randomAngle = random.nextFloat();
			long t0 = System.nanoTime();
			// the calculation being timed:
			double sine = Math.sin(randomAngle);
			double cosine = Math.cos(randomAngle + 1);
			double tan = Math.tan(randomAngle + 2);
			double calc = Math.sqrt(Math.pow(Math.pow(sine, cosine), tan));
			long t1 = System.nanoTime();
		}

		System.out.println(CodeTimer.class.getSimpleName() + ": starting test 1.");
		long startNanos = System.nanoTime();
		random = new java.util.Random(0);
		long nanoSelfTime = calibrateNanoTime(10000);
		System.out.println(CodeTimer.class.getSimpleName() + ": System.nanoTime() selfTime == " + nanoSelfTime);
		long totalNanos = 0;
		double lastPrintOutNanos = System.nanoTime();
		float maxSecondsBeforePrintOut = 5;
		long count = 0;
		while (true) {
			float randomAngle = random.nextFloat();
			long t0 = System.nanoTime();
			// the calculation being timed:
			double sine = Math.sin(randomAngle);
			double cosine = Math.cos(randomAngle + 1);
			double tan = Math.tan(randomAngle + 2);
			double calc = Math.sqrt(Math.pow(Math.pow(sine, cosine), tan));
			long t1 = System.nanoTime();
			totalNanos += t1 - t0 - nanoSelfTime;
			count++;
			if (System.nanoTime() - lastPrintOutNanos > maxSecondsBeforePrintOut * NANOS_IN_A_SECOND) {
				double avTotalNanos = (double) totalNanos / (count);
				DecimalFormat fourDP = new DecimalFormat("0.####");
				System.out.println(CodeTimer.class.getSimpleName() + ": avTotalNanos == " + fourDP.format(avTotalNanos));
				totalNanos = 0;
				count = 0;
				lastPrintOutNanos = System.nanoTime();
			}
			if (System.nanoTime() - startNanos > testRunningTimeNanos) {
				break;
			}
		}

		System.out.println(CodeTimer.class.getSimpleName() + ": starting test 2.");
		startNanos = System.nanoTime();
		random = new java.util.Random(0);
		CodeTimer codeTimer = new CodeTimer("Test", Output.Nanos, Output.Nanos);
		System.out.println(CodeTimer.class.getSimpleName() + ": CodeTimer.SELF_TIME == " + CodeTimer.SELF_TIME);
		while (true) {
			float randomAngle = random.nextFloat();
			codeTimer.click("foo");
			// the calculation being timed:
			double sine = Math.sin(randomAngle);
			double cosine = Math.cos(randomAngle + 1);
			double tan = Math.tan(randomAngle + 2);
			double calc = Math.sqrt(Math.pow(Math.pow(sine, cosine), tan));
			codeTimer.lastClick();

			if (System.nanoTime() - startNanos > testRunningTimeNanos) {
				break;
			}
		}
		System.out.println(CodeTimer.class.getSimpleName() + ": finished.");

	}

	// This is only used in the testing of the manual method.
	protected static long calibrateNanoTime(int numTests) {
		boolean print = false;
		// warm the JIT
		for (int i = 0; i < 1024; i++) {
			System.nanoTime();
		}

		// find how out long it takes to call click(), so that time can be accounted for
		ArrayList<Long> selfTimeObservations = new ArrayList<Long>(numTests);
		for (int i = 0; i < numTests; i++) {
			long nanoSelfTime = -(System.nanoTime() - System.nanoTime());
			if (print) {
				System.out.println(CodeTimer.class.getSimpleName() + ": nanoSelfTime == " + nanoSelfTime);
			}
			selfTimeObservations.add(nanoSelfTime);
		}
		// sort the times
		Collections.sort(selfTimeObservations);
		if (print) {
			for (int i = 0; i < selfTimeObservations.size(); i++) {
				System.out.println(CodeTimer.class.getSimpleName() + ": selfTimeObservations.get(i) == " + selfTimeObservations.get(i));
			}
		}
		// cut out the slowest 5% which are assumed to be outliers
		for (int i = 0; i < (int) (numTests * 0.05); i++) {
			selfTimeObservations.remove(0);
		}
		// cut out the fastest 5% which are assumed to be outliers
		for (int i = 0; i < (int) (numTests * 0.05); i++) {
			selfTimeObservations.remove(selfTimeObservations.size() - 1);
		}
		if (print) {
			System.out.println(CodeTimer.class.getSimpleName() + ": Slimmed list: selfTimeObservations.size() == " + selfTimeObservations.size());
			for (int i = 0; i < selfTimeObservations.size(); i++) {
				System.out.println(CodeTimer.class.getSimpleName() + ": selfTimeObservations.get(i) == " + selfTimeObservations.get(i));
			}
		}
		// find the average
		long sumOfSelfTimes = 0;
		for (int i = 0; i < selfTimeObservations.size(); i++) {
			sumOfSelfTimes += selfTimeObservations.get(i);
		}
		long nanoSelfTime = sumOfSelfTimes / selfTimeObservations.size();
		return nanoSelfTime;
	}
}
