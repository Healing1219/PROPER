package backend;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.GeometricDistribution;
import org.apache.commons.math3.distribution.LaplaceDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import backend.Util.Var;
import frontend.Symbol;
import frontend.TypeSystem;

public class ClibCall {
	
	private Set<String> apiSet;
	static TypeSystem typeSystem = TypeSystem.getTypeSystem();
	//变量命名
	public static int count=0;
	public static Symbol sym;
	
	private ClibCall() {
		apiSet = new HashSet<String>();
		apiSet.add("R");
		apiSet.add("Binomial");
		apiSet.add("unifInt");
		apiSet.add("Normal");
		apiSet.add("unifReal");
		apiSet.add("Exponential");
		apiSet.add("Gamma");
		apiSet.add("Beta");
		apiSet.add("Laplace");
		apiSet.add("Geometric");
		apiSet.add("Poisson");
		apiSet.add("T");
		apiSet.add("not");
		apiSet.add("flip");
	}

	private static ClibCall instance = null;

	public static ClibCall getInstance() {

		if (instance == null) {
			instance = new ClibCall();
		}

		return instance;
	}

	public boolean isAPICall(String funcName) {
		return apiSet.contains(funcName);
	}

	public static Object invokeAPI(String funcName) {
		switch (funcName) {
		case "R":
			return handleR();
		case "Binomial":
			return handleBinomial();
		case "Normal":
			return handleNormal();
		case "unifInt":
			return handleUniformInt();
		case "unifReal":
			return handleUniformReal();
		case "Exponential":
			return handleExponential();
		case "Gamma":
			return handleGamma();
		case "Beta":
			return handleBeta();
		case "Laplace":
			return handleLaplace();
		case "Geometric":
			return handleGeometric();
		case "Poisson":
			return handlePoisson();
		case "T":
			return handleT();
		case "not":
			return not();
		case "flip":
			return flip();
		default:
			return null;
		}
	}
	
	private static Object handleR() {
		ArrayList<Object> argsList = FunctionArgumentList.getFunctionArgumentList().getFuncArgList(false);
		int n=argsList.size();
		if(n%2!=0)
			System.err.println("请检查输入参数");
		//double[] arr=new double[n/2];
		double p=0;
		for(int i=n/2;i<n;i++) {
			p+=Double.parseDouble(argsList.get(i)+"");
		}
		if(p!=1.0) {
			System.err.println("概率之和必须为1");
		}
		double m=Math.random();
		p=0;
		if(Util.locType!=0) {
			ArrayList<Integer> scatters=new ArrayList<Integer>();
			double min=Double.parseDouble(argsList.get(0)+"");
			double max=Double.parseDouble(argsList.get(0)+"");
			double mean=0;
			double powMean=0;
			for(int  i=0;i<n/2;i++) {
				if(min>Double.parseDouble(argsList.get(i)+""))
					min=Double.parseDouble(argsList.get(i)+"");
				if(max<Double.parseDouble(argsList.get(i)+""))
					max=Double.parseDouble(argsList.get(i)+"");
				mean+=Double.parseDouble(argsList.get(i)+"")*Double.parseDouble(argsList.get(n/2+i)+"");
				powMean+=Math.pow(Double.parseDouble(argsList.get(i)+""),2)*Double.parseDouble(argsList.get(n/2+i)+"");
				scatters.add((new Double(argsList.get(i)+"")).intValue());
			}
			sym=new Symbol();
			sym.r=new Util.R(true,min,max,mean,powMean);
			sym.setR(true);
			
			int min1=0;
			sym.setrId(++Util.r);
			for(int i= 1;i<scatters.size();i++) {
				if(scatters.get(i)!=scatters.get(i-1)+1) {
					Util.rScope(Util.r,scatters.get(min1), scatters.get(i-1), true);
					min1=i;
				}
				if(i==scatters.size()-1) {
					Util.rScope(Util.r,scatters.get(min1), scatters.get(i), true);
				}
			}
			//Util.rScope(++Util.r,min, max, true);
		}
		for(int i=n/2;i<n;i++) {
			p+=Double.parseDouble(argsList.get(i)+"");
			if(m<p)
				return Double.parseDouble(argsList.get(i-n/2)+"");
		}
		return null;
	}

	private static Object handleBinomial() {
		ArrayList<Object> argsList = FunctionArgumentList.getFunctionArgumentList().getFuncArgList(false);
		if(argsList.size()==2) {
			int trials = (int)Double.parseDouble(argsList.get(0)+"");
			double p = Double.parseDouble(argsList.get(1)+"");
			count++;
			Var var =new Var();
			var.name="r_"+count;
			var.type="B";
			var.param1=trials;
			var.param2=p;
			Util.setCoeff(count, 1.0);
			BinomialDistribution binomial=new BinomialDistribution(trials,p);
			//sym=new Symbol(var.name, 0);
			//sym.addSpecifier(typeSystem.newType("int"));
			if(Util.locType!=0) {
				sym=new Symbol();
				//ArrayList<Double> scatters=new ArrayList<Double>();
				double min=binomial.getSupportLowerBound();
				double max=binomial.getSupportUpperBound();
				double mean= binomial.getNumericalMean();
				double powMean=Math.pow(mean, 2)+binomial.getNumericalVariance();
				sym.r=new Util.R(true,min,max,mean,powMean);
				sym.setR(true);
				sym.setrId(++Util.r);
				/*for(int i= (new Double(min)).intValue();i<=max;i++) {
					scatters.add(i*1.0);
				}*/
				Util.rScope(Util.r,min, max, true);
			}
			if(Util.varList!=null) {
				Util.varList.add(var);
			}
	        return binomial.sample();
		}
		return null;
	}

	private static Object handleNormal() {
		ArrayList<Object> argsList = FunctionArgumentList.getFunctionArgumentList().getFuncArgList(false);
		if(argsList.size()==2) {
			double mean  = Double.parseDouble(argsList.get(0)+"");
			double std  = Double.parseDouble(argsList.get(1)+"");
			count++;
			Var var =new Var();
			var.name="r_"+count;
			var.type="G";
			var.param1=mean;
			var.param2=std;
			Util.varList.add(var);
//			sym=new Symbol(var.name, 0);
//			sym.addSpecifier(typeSystem.newType("real"));
			Util.setCoeff(count, 1.0);
			NormalDistribution nomial=new NormalDistribution(mean,std);
			if(Util.locType!=0) {
				sym=new Symbol();
				double min=nomial.getSupportLowerBound();
				double max=nomial.getSupportUpperBound();
				mean=nomial.getNumericalMean();
				double powMean=Math.pow(mean, 2)+nomial.getNumericalVariance();
				sym.r=new Util.R(true,min,max,mean,powMean);
				sym.setR(true);
				sym.setrId(++Util.r);
				Util.rScope(Util.r,min, max, false);
			}
			if(Util.varList!=null) {
				Util.varList.add(var);
			}
			return nomial.sample();
		}
		return null;
	}

	private static Object handleUniformInt() {
		ArrayList<Object> argsList = FunctionArgumentList.getFunctionArgumentList().getFuncArgList(false);
		if(argsList.size()==2) {
			int upper  = Integer.parseInt(argsList.get(1)+"");
			int lower  = Integer.parseInt(argsList.get(0)+"");
			count++;
			Var var =new Var();
			var.name="r_"+count;
			var.type="I";
			var.param1=lower;
			var.param2=upper;
			Util.setCoeff(count, 1.0);
			UniformIntegerDistribution uniformInt=new UniformIntegerDistribution(lower,upper);
			if(Util.locType!=0) {
				//ArrayList<Double> scatters=new ArrayList<Double>();
				double min=Double.parseDouble(lower+"");
				double max=Double.parseDouble(upper+"");
				double mean=uniformInt.getNumericalMean();
				double squareMean=Math.pow(uniformInt.getNumericalMean(),2)+uniformInt.getNumericalVariance();
				sym=new Symbol();
				sym.r=new Util.R(true,min,max,mean,squareMean);
				sym.setR(true);
				sym.setrId(++Util.r);
				/*for(int i= (new Double(min)).intValue();i<=max;i++) {
					scatters.add(i*1.0);
				}*/
				Util.rScope(Util.r,min, max, true);
			}
			if(Util.varList!=null) {
				Util.varList.add(var);
			}
	        return uniformInt.sample();
		}
		return null;
	}

	private static Object handleUniformReal() {
		ArrayList<Object> argsList = FunctionArgumentList.getFunctionArgumentList().getFuncArgList(false);
		if(argsList.size()==2) {
			double lower  = Double.parseDouble(argsList.get(0)+"");
			double upper  = Double.parseDouble(argsList.get(1)+"");
			count++;
			Var var =new Var();
			var.name="r_"+count;
			var.type="U";
			var.param1=lower;
			var.param2=upper;
			//sym=new Symbol(var.name, 0);
			//sym.addSpecifier(typeSystem.newType("real"));
			Util.setCoeff(count, 1.0);
			UniformRealDistribution uniformReal=new UniformRealDistribution(lower,upper);
			if(Util.locType!=0) {
				sym=new Symbol();
				double min=uniformReal.getSupportLowerBound();
				double max=uniformReal.getSupportUpperBound();
				double mean=uniformReal.getNumericalMean();
				double powMean=Math.pow(mean, 2)+uniformReal.getNumericalVariance();
				sym.r=new Util.R(true,min,max,mean,powMean);
				sym.setR(true);
				sym.setrId(++Util.r);
				Util.rScope(Util.r,min, max, false);
			}
			if(Util.varList!=null) {
				Util.varList.add(var);
			}
			return uniformReal.sample();
		}
		return null;
	}
	
	private static Object handleExponential() {
		ArrayList<Object> argsList = FunctionArgumentList.getFunctionArgumentList().getFuncArgList(false);
		if(argsList.size()==1) {
			double mean  = Double.parseDouble(argsList.get(0)+"");
			count++;
			Var var =new Var();
			var.name="r_"+count;
			var.type="E";
			var.param1=Double.parseDouble(argsList.get(0)+"");
			Util.setCoeff(count, 1.0);
			ExponentialDistribution ex=new ExponentialDistribution(mean);
			if(Util.locType!=0) {
				sym=new Symbol();
				double min=ex.getSupportLowerBound();
				double max=ex.getSupportUpperBound();
				mean=ex.getNumericalMean();
				double powMean=Math.pow(mean, 2)+ex.getNumericalVariance();
				sym.r=new Util.R(true,min,max,mean,powMean);
				sym.setR(true);
				sym.setrId(++Util.r);
				Util.rScope(Util.r,min, max, false);
			}
			if(Util.varList!=null) {
				Util.varList.add(var);
			}
			return ex.sample();
		}
		return null;
	}
	
	private static Object handleGamma() {
		ArrayList<Object> argsList = FunctionArgumentList.getFunctionArgumentList().getFuncArgList(false);
		if(argsList.size()==2) {
			double shape  = Double.parseDouble(argsList.get(0)+"");
			double scale  = Double.parseDouble(argsList.get(1)+"");
			count++;
			Var var =new Var();
			var.name="r_"+count;
			var.type="Ga";
			var.param1=shape;
			var.param2=scale;
			Util.setCoeff(count, 1.0);
			GammaDistribution ga=new GammaDistribution(shape,scale);
			if(Util.locType!=0) {
				sym=new Symbol();
				double min=ga.getSupportLowerBound();
				double max=ga.getSupportUpperBound();
				double mean=ga.getNumericalMean();
				double powMean=Math.pow(mean, 2)+ga.getNumericalVariance();
				sym.r=new Util.R(true,min,max,mean,powMean);
				sym.setR(true);
				sym.setrId(++Util.r);
				Util.rScope(Util.r,min, max, false);
			}
			if(Util.varList!=null) {
				Util.varList.add(var);
			}
	        return ga.sample();
		}
		return null;
	}
	
	private static Object handleBeta() {
		ArrayList<Object> argsList = FunctionArgumentList.getFunctionArgumentList().getFuncArgList(false);
		if(argsList.size()==2) {
			double alpha  = Double.parseDouble(argsList.get(0)+"");
			double beta  = Double.parseDouble(argsList.get(1)+"");
			count++;
			Var var =new Var();
			var.name="r_"+count;
			var.type="Be";
			var.param1=alpha;
			var.param2=beta;
			Util.setCoeff(count, 1.0);
			BetaDistribution be=new BetaDistribution(alpha,beta);
			if(Util.locType!=0) {
				sym=new Symbol();
				double min=be.getSupportLowerBound();
				double max=be.getSupportUpperBound();
				double mean=be.getNumericalMean();
				double powMean=Math.pow(mean, 2)+be.getNumericalVariance();
				sym.r=new Util.R(true,min,max,mean,powMean);
				sym.setR(true);
				sym.setrId(++Util.r);
				Util.rScope(Util.r,min, max, false);
			}
			if(Util.varList!=null) {
				Util.varList.add(var);
			}
	        return be.sample();
		}
		return null;
	}
	
	private static Object handleLaplace() {
		ArrayList<Object> argsList = FunctionArgumentList.getFunctionArgumentList().getFuncArgList(false);
		if(argsList.size()==2) {
			double mu= Double.parseDouble(argsList.get(0)+"");
			double beta  = Double.parseDouble(argsList.get(1)+"");
			count++;
			Var var =new Var();
			var.name="r_"+count;
			var.type="La";
			var.param1=mu;
			var.param2=beta;
			Util.setCoeff(count, 1.0);
			LaplaceDistribution la=new LaplaceDistribution(mu,beta);
			if(Util.locType!=0) {
				sym=new Symbol();
				double min=la.getSupportLowerBound();
				double max=la.getSupportUpperBound();
				double mean=la.getNumericalMean();
				double powMean=Math.pow(mean, 2)+la.getNumericalVariance();
				sym.r=new Util.R(true,min,max,mean,powMean);
				sym.setR(true);
				sym.setrId(++Util.r);
				Util.rScope(Util.r,min, max, false);
			}
			if(Util.varList!=null) {
				Util.varList.add(var);
			}
	        return la.sample();
		}
		return null;
	}
	
	private static Object handleGeometric() {
		ArrayList<Object> argsList = FunctionArgumentList.getFunctionArgumentList().getFuncArgList(false);
		if(argsList.size()==1) {
			double p  = Double.parseDouble(argsList.get(0)+"");
			count++;
			Var var =new Var();
			var.name="r_"+count;
			var.type="Ge";
			var.param1=p;
			Util.setCoeff(count, 1.0);
			GeometricDistribution ge=new GeometricDistribution(p);
			if(Util.locType!=0) {
				sym=new Symbol();
				double min=ge.getSupportLowerBound();
				double max=ge.getSupportUpperBound();
				double mean=ge.getNumericalMean();
				double powMean=Math.pow(mean, 2)+ge.getNumericalVariance();
				sym.r=new Util.R(true,min,max,mean,powMean);
				sym.setR(true);
				sym.setrId(++Util.r);
				Util.rScope(Util.r,min, max, false);
			}
			if(Util.varList!=null) {
				Util.varList.add(var);
			}
	        return ge.sample();
		}
		return null;
	}
	
	private static Object handlePoisson() {
		ArrayList<Object> argsList = FunctionArgumentList.getFunctionArgumentList().getFuncArgList(false);
		if(argsList.size()==1) {
			double p  = Double.parseDouble(argsList.get(0)+"");
			count++;
			Var var =new Var();
			var.name="r_"+count;
			var.type="P";
			var.param1=p;
			Util.setCoeff(count, 1.0);
			PoissonDistribution po=new PoissonDistribution(p);
			if(Util.locType!=0) {
				sym=new Symbol();
				double min=po.getSupportLowerBound();
				double max=po.getSupportUpperBound();
				double mean=po.getNumericalMean();
				double powMean=Math.pow(mean, 2)+po.getNumericalVariance();
				sym.r=new Util.R(true,min,max,mean,powMean);
				sym.setR(true);
				sym.setrId(++Util.r);
				Util.rScope(Util.r,min, max, false);
			}
			if(Util.varList!=null) {
				Util.varList.add(var);
			}
	        return po.sample();
		}
		return null;
	}
	
	private static Object handleT() {
		ArrayList<Object> argsList = FunctionArgumentList.getFunctionArgumentList().getFuncArgList(false);
		if(argsList.size()==1) {
			double p  = Double.parseDouble(argsList.get(0)+"");
			count++;
			Var var =new Var();
			var.name="r_"+count;
			var.type="T";
			var.param1=p;
			Util.varList.add(var);
			Util.setCoeff(count, 1.0);
			TDistribution t=new TDistribution(p);
			if(Util.locType!=0) {
				sym=new Symbol();
				double min=t.getSupportLowerBound();
				double max=t.getSupportUpperBound();
				double mean=t.getNumericalMean();
				double powMean=Math.pow(mean, 2)+t.getNumericalVariance();
				sym.r=new Util.R(true,min,max,mean,powMean);
				sym.setR(true);
				sym.setrId(++Util.r);
				Util.rScope(Util.r,min, max, false);
			}
			if(Util.varList!=null) {
				Util.varList.add(var);
			}
	        return t.sample();
		}
		return null;
	}
	
	private static Object not() {
		ArrayList<Object> argsList = FunctionArgumentList.getFunctionArgumentList().getFuncArgList(false);
		if(argsList.size()==1) {
			boolean b  = (boolean)argsList.get(0);
			if(b==true) {
				return false;
			}else {
				return true;
			}
		}
		return null;
	}
	
	private static Object flip() {
		ArrayList<Object> argsList = FunctionArgumentList.getFunctionArgumentList().getFuncArgList(false);
		if(argsList.size()==1) {
			double b  = Double.parseDouble(argsList.get(0)+"");
			if(Math.random()<=b) {
				return true;
			}else {
				return false;
			}
		}
		return null;
	}
}
