package Neural;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

public class NeuralNetwork implements Serializable {
	/**
	 * 
	 */
	 private static final long serialVersionUID = -664522815884100314L;
	 double[][][] weights;
	 double[][][] dWeights;
	 int[] size;
	 double[][] activation;
	 double startingLearningRate = 0.2;
	 double minLearningRate=0.0001;
	 double degration=0.8;
	 double momentum =0.0;
	 double bias = -1;
	 double chance;
	 int epoch, trial;
	
	
	public NeuralNetwork(int[] size){
		this.size=size;
		activation=new double[size.length][];
		init();
	}
	public void setVariables(double d, int e, int t){
		chance=d;
		epoch=e;
		trial=t;
	}
	public double getChance(){
		return chance;
	}
	
	public int getEpoch(){
		return epoch;
	}
	
	public int getTrial(){
		return trial;
	}
	
	
	private void init(){
		weights = new double[size.length - 1][][];
		dWeights=new double [size.length -1][][];
		for (int i = 0; i < weights.length; i++) {
			weights[i] = new double[size[i + 1]][size[i]];
			dWeights[i] = new double[size[i + 1]][size[i]];
		}

		Random r = new Random(System.currentTimeMillis());
		for (int i = 0; i < weights.length; i++) {
			for (int j = 0; j < weights[i].length; j++) {
				for (int k = 0; k < weights[i][j].length; k++) {
					weights[i][j][k] = r.nextDouble() * 100 - 50;
				}
			}
		}
	}
	
	private static double sigmoid(double in) {
		return (double) (1 /( Math.exp(-in) + 1));
	}

	private static double dSigmoid(double in) {
		double t = sigmoid(in);
		return t * (1 - t);
	}
	
	public double[][] forwardProp(double[] input){
		activation = new double[size.length][];
		activation[0] = Arrays.copyOf(input, input.length);
		
		for (int k = 0; k < size.length - 1; k++) {
			activation[k+1]=new double[size[k+1]];
			for (int i = 0; i < weights[k].length; i++) {
				activation[k+1][i]=0;
				for (int j = 0; j < weights[k][i].length; j++) {
					activation[k+1][i] += activation[k][j] *weights[k][i][j];
				}
				if (k < size.length - 2) {
					activation[k+1][i]=sigmoid(activation[k+1][i]);
				}
			}
		}
		return activation;
	}
	
	private static double[] arraySub(double[] A, double[] B) {
		if (A.length != B.length)
			System.err.print("Arrays need to be off equals size to substract");
		double[] C = new double[A.length];
		for (int i = 0; i < A.length; i++)
			C[i] = A[i] - B[i];
		return C;
	}
	
	public double backProp(double[][] activation, double[] expectedOutput) {
		double[][] error = new double[activation.length][];

		for (int i = 0; i < activation.length; i++) {
			error[i] = new double[activation[i].length];
		}

		double[][] delta = new double[size.length][];
		for (int i = 0; i < delta.length; i++) {
			delta[i] = new double[size[i]];
		}

		error[error.length - 1] = arraySub(expectedOutput, activation[activation.length - 1]);

//		System.out.print(error[error.length - 1][0]+" " );
//		System.out.print(error[error.length - 1][1]+" " );
//		System.out.print(error[error.length - 1][2]+" " );
//		System.out.print(error[error.length - 1][3]+"\n" );
		for (int k = weights.length - 1; k > 0; k--) {
			if (k > 0) {
				error[k] = new double[error[k].length];
			}
			for (int i = 0; i < delta[k + 1].length; i++) {
				if (k != weights.length - 1) {
					delta[k + 1][i] = error[k + 1][i] * dSigmoid(activation[k + 1][i]);
				} else {
					delta[k + 1][i] = error[k + 1][i];
				}
				for (int j = 0; j < weights[k][i].length; j++) {
					error[k][j] += delta[k + 1][i] * weights[k][i][j];
				}
			}
		}

		for (int k = weights.length - 1; k > 0; k--) {
			for (int i = 0; i < weights[k].length; i++) {
				for (int j = 0; j < weights[k][i].length; j++) {
					dWeights[k][i][j] =startingLearningRate * delta[k + 1][i] * activation[k][j]+
							momentum*dWeights[k][i][j];
					weights[k][i][j] += dWeights[k][i][j];
				}
			}
		}
		return 0;

	}
	
	public void degradeRate(){
		startingLearningRate=Math.max(startingLearningRate *degration,minLearningRate);
	}
	
	public boolean sameSize(int[] os){
		
		if(size.length != os.length)return false;
		
		for(int i=0;i<os.length;i++){
			if(os[i]!=size[i])return false;
		}
		
		return true;
	}
	
	public void printWeights(){
		for (int k = 0; k < size.length - 1; k++) {
			for (int i = 0; i < weights[k].length; i++) {
				for (int j = 0; j < weights[k][i].length; j++) {
					System.out.print(weights[k][i][j]+" ");
				}
				System.out.println("");
			}
			System.out.println("");
		}
	}
	
	
}
