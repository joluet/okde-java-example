package de.tuhh.luethke.okde.example;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.ejml.simple.SimpleMatrix;
import org.math.plot.Plot3DPanel;

import de.tuhh.luethke.oKDE.Exceptions.EmptyDistributionException;
import de.tuhh.luethke.oKDE.model.BaseSampleDistribution;
import de.tuhh.luethke.oKDE.model.SampleModel;

/**
 * This is a simple example that illustrates the usage of the okde-java library.
 * It creates a set of two-dimensional sample points. The samples are generated
 * using a Gaussian mixture distribution with three components. Then the
 * okde-java library is used to estimate the sample distribution. Eventually,
 * the estimated distribution is plotted as a three-dimensional grid plot to
 * visualize the result.
 * 
 * @author jluethke
 * 
 */
public class oKdeExample extends JFrame {

	private static final long serialVersionUID = -2880832753267205498L;
	private static final int DIM = 2;

	public oKdeExample() {

		initUI();
	}

	private void initUI() {

		// some configuration for plotting
		setTitle("okde");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(900, 500);
		setLocationRelativeTo(null);

		// disable the forgetting factor
		double forgettingFactor = 1;
		// set the compression threshold
		double compressionThreshold = 0.02;
		// number of samples to genereate
		int noOfSamples = 1000;

		// sample model object used for sample distribution estimation
		SampleModel sampleDistribution = new SampleModel(forgettingFactor,
				compressionThreshold);

		// create a covariance matrix with all entries 0
		double[][] c = new double[DIM][DIM];
		for (int i = 0; i < c.length; i++) {
			for (int j = 0; j < c.length; j++) {
				c[i][j] = 0;
			}
		}

		// generate sample points using three different Gaussian distributions
		// first define the means and standard deviations
		int[] mean1 = { 3, 2 };
		float stddev1 = .2f;
		int[] mean2 = { 7, 4 };
		float stddev2 = .2f;
		int[] mean3 = { 3, 8 };
		float stddev3 = .5f;
		SimpleMatrix[] samples = new SimpleMatrix[noOfSamples];
		// now generate the random samples
		for (int i = 0; i < noOfSamples; i++) {
			// 30% are distributed with mean1 and stddev1
			// 20% are distributed with mean2 and stddev2
			// 50% are distributed with mean3 and stddev3
			if (i < noOfSamples * 0.3) {
				double[][] sampleArray = { { gaussian(mean1[0], stddev1) },
						{ gaussian(mean1[1], stddev1) } };
				SimpleMatrix sample = new SimpleMatrix(sampleArray);
				samples[i] = sample;
			} else if (i < noOfSamples * 0.5) {
				double[][] sampleArray = { { gaussian(mean2[0], stddev2) },
						{ gaussian(mean2[1], stddev2) } };
				SimpleMatrix sample = new SimpleMatrix(sampleArray);
				samples[i] = sample;
			} else if (i < noOfSamples) {
				double[][] sampleArray = { { gaussian(mean3[0], stddev3) },
						{ gaussian(mean3[1], stddev3) } };
				SimpleMatrix sample = new SimpleMatrix(sampleArray);
				samples[i] = sample;
			}
		}

		/*
		 * Now the sample model is updated using the generated sample data.
		 */
		try {
			// Add three samples at once to initialize the sample model
			ArrayList<SimpleMatrix> initSamples = new ArrayList<SimpleMatrix>();
			initSamples.add(samples[0]);
			initSamples.add(samples[1]);
			initSamples.add(samples[2]);
			double[] w = { 1, 1, 1 };
			SimpleMatrix[] cov = { new SimpleMatrix(c), new SimpleMatrix(c),
					new SimpleMatrix(c) };
			sampleDistribution.updateDistribution(
					initSamples.toArray(new SimpleMatrix[3]), cov, w);

			// Update the sample model with all generated samples one by one.
			for (int i = 3; i < noOfSamples; i++) {
				SimpleMatrix pos = samples[i];
				sampleDistribution.updateDistribution(pos, new SimpleMatrix(c),
						1d);
			}
		} catch (EmptyDistributionException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		/*
		 * Plot the distribution estimated by the sample model
		 */

		// first create a 100x100 grid
		double[] xArray = new double[100];
		double[] yArray = new double[100];
		for (int i = 0; i < 100; i++) {
			xArray[i] = i * 0.1;
			yArray[i] = i * 0.1;
		}
		// then evaluate the sample model at each point of the grid
		double[][] zArray = evaluateSampleDistribution(xArray, yArray,
				sampleDistribution);

		Plot3DPanel plot = new Plot3DPanel("SOUTH");
		// add grid plot to the PlotPanel
		plot.addGridPlot("estimated sample distribution", xArray, yArray,
				zArray);

		setContentPane(plot);
	}

	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				oKdeExample ps = new oKdeExample();
				ps.setVisible(true);
			}
		});
	}

	/**
	 * Evaluates a given sample distribution at point in a given grid.
	 * 
	 * @param x
	 *            x-axis of the grid
	 * @param y
	 *            y-axis of the grid
	 * @param dist
	 *            sample distribution to evaluate
	 * @return z-components of all evaluated grid points
	 */
	private double[][] evaluateSampleDistribution(double[] x, double[] y,
			BaseSampleDistribution dist) {
		double[][] z = new double[y.length][x.length];
		for (int i = 0; i < x.length; i++)
			for (int j = 0; j < y.length; j++) {
				double[][] point = { { x[i] }, { y[j] } };
				SimpleMatrix pointVector = new SimpleMatrix(point);
				z[j][i] = dist.evaluate(pointVector);
			}
		return z;
	}

	/**
	 * Return a real number from a Gaussian distribution with given mean and
	 * standard deviation
	 * 
	 * @param mean
	 *            mean
	 * @param stddev
	 *            standard deviation
	 * @return real number from Gaussian distribution with given mean and
	 *         standard deviation
	 */
	public static double gaussian(double mean, double stddev) {
		Random random = new Random();
		return mean + stddev * random.nextGaussian();
	}

}
