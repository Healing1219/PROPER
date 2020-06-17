package inference;

import java.io.Serializable;

import org.apache.commons.math3.analysis.function.Gaussian;
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

public class RVar implements Serializable {

	public enum distrib_type_t {
		BINOMIAL, UNIFORM, GAUSSIAN, UINT, EXPONENTIAL, GAMMA, BETA, LAPLACE, GEOMETRIC, POISSON, T
	}

	private int id;
	private String name;
	private distrib_type_t type;
	private double lb;// If variable is gaussian this represents the mean
	private double ub;// If variable is gaussian this represents the stdev.

	public RVar() {

	}

	public RVar(int i, String n, distrib_type_t dr, double l, double u) {
		this.id = i;
		this.name = n;
		this.type = dr;
		this.lb = l;
		this.ub = u;

		if (type == distrib_type_t.UNIFORM || type == distrib_type_t.UINT) {
			assert (lb <= ub);
		}
		if (type == distrib_type_t.UINT) {
			lb = Math.ceil(lb);
			ub = Math.floor(ub);
		}
		if (type == distrib_type_t.GAUSSIAN || type == distrib_type_t.LAPLACE) {
			assert (ub > 0.0);
		}
		if (type == distrib_type_t.BINOMIAL) {// B(n,p)
			assert (lb >= 1);
			assert (ub > 0 && ub < 1);
			lb = Math.ceil(lb);
		}
		if (type == distrib_type_t.GAMMA || type == distrib_type_t.BETA) {
			assert (lb > 0 && ub > 0);
		}
		if (type == distrib_type_t.GEOMETRIC) {// G(p)
			assert (lb > 0 && lb < 1);
		}
		if (type == distrib_type_t.POISSON || type == distrib_type_t.EXPONENTIAL) {
			assert (lb > 0.0);
		}
	}

	public String getName() {
		return name;
	}

	public boolean isIntegerVariable() {
		return type == distrib_type_t.UINT;
	}

	public boolean hasLowerBound() {
		if (type == distrib_type_t.UNIFORM || type == distrib_type_t.UINT) {
			return true;
		}
		return false;
	}

	public boolean hasUpperBound() {
		if (type == distrib_type_t.UNIFORM || type == distrib_type_t.UINT) {
			return true;
		}
		return false;
	}

	public double getLowerBound() {
		if (type == distrib_type_t.UNIFORM || type == distrib_type_t.UINT) {
			return lb;
		}
		return 0;
	}

	public double getUpperBound() {
		if (type == distrib_type_t.UNIFORM || type == distrib_type_t.UINT) {
			return ub;
		}
		return -1;
	}

	public RVar uniformVariable(int i, String n, double lb, double ub) {
		assert (lb <= ub);
		RVar r = new RVar(i, name, distrib_type_t.UNIFORM, lb, ub);
		return r;
	}

	public RVar uniformIntVariable(int i, String n, double lb, double ub) {
		assert (lb <= ub);
		RVar r = new RVar(i, name, distrib_type_t.UINT, lb, ub);
		return r;
	}

	public RVar gaussianVariable(int i, String n, double mean, double sd) {
		assert (sd > 0.0);
		RVar r = new RVar(i, name, distrib_type_t.GAUSSIAN, mean, sd);
		return r;
	}

	public RVar BinomialVariable(int i, String n, double trials, double p) {
		assert (trials >= 1);
		assert (p >= 0 && p <= 1);
		RVar r = new RVar(i, name, distrib_type_t.BINOMIAL, trials, p);
		return r;
	}

	public RVar ExponentialVariable(int i, String n, double mean) {
		assert (mean > 0);
		RVar r = new RVar(i, name, distrib_type_t.EXPONENTIAL, mean, 0);
		return r;
	}

	public RVar GammaVariable(int i, String n, double shape, double scale) {
		assert (shape > 0 && scale > 0);
		RVar r = new RVar(i, name, distrib_type_t.GAMMA, shape, scale);
		return r;
	}

	public RVar BetaVariable(int i, String n, double alpha, double beta) {
		assert (alpha > 0 && beta > 0);
		RVar r = new RVar(i, name, distrib_type_t.BETA, alpha, beta);
		return r;
	}

	public RVar LaplaceVariable(int i, String n, double mu, double beta) {
		assert (beta > 0);
		RVar r = new RVar(i, name, distrib_type_t.LAPLACE, mu, beta);
		return r;
	}

	public RVar GeometricVariable(int i, String n, double p) {
		assert (p > 0);
		RVar r = new RVar(i, name, distrib_type_t.GEOMETRIC, p, 0);
		return r;
	}

	public RVar PoissonVariable(int i, String n, double p) {
		assert (p > 0 && p < 1);
		RVar r = new RVar(i, name, distrib_type_t.POISSON, p, 0);
		return r;
	}
	
	public RVar TVariable(int i, String n, double p) {
		assert (p > 0 && p < 1);
		RVar r = new RVar(i, name, distrib_type_t.T, p, 0);
		return r;
	}

	public double computeIntervalProbability(boolean hasLB, double l, boolean hasUB, double u) {

		switch (type) {
		case UNIFORM:
			if (!hasLB || l < lb) {
				l = lb;
			}
			if (!hasUB || u > ub) {
				u = ub;
			}
			if (l > u) {
				return 0.0;
			}
			if (l == u && l == lb && u == ub) {
				return 1.0;
			}
			return (u - l) / (ub - lb);
		case UINT:
			l = Math.ceil(l);
			u = Math.floor(u);
			if (!hasLB || l < lb) {
				l = lb;
			}
			if (!hasUB || u > ub) {
				u = ub;
			}
			if (l > u) {
				return 0.0;
			}
			return (u - l + 1) / (ub - lb + 1);
		case GAUSSIAN:
			NormalDistribution g1 = new NormalDistribution(lb, ub);
			if (hasLB && hasUB) {
				return g1.probability(l, u);
			} else if (hasUB) {
				return g1.cumulativeProbability(u);
			} else if (hasLB) {
				return 1 - g1.cumulativeProbability(l);
			} else {
				return 1.0;
			}
		case BINOMIAL:
			BinomialDistribution b = new BinomialDistribution((int) Math.floor(lb), ub);
			if (hasLB && hasUB) {
				return b.cumulativeProbability((int) Math.floor(u)) - b.cumulativeProbability((int) Math.ceil(l));
			} else if (hasUB) {
				return b.cumulativeProbability((int) Math.floor(u));
			} else if (hasLB) {
				return 1 - b.cumulativeProbability((int) Math.ceil(l));
			} else {
				return 1.0;
			}
		case EXPONENTIAL:
			ExponentialDistribution e = new ExponentialDistribution(lb, ub);
			if (hasLB && hasUB) {
				return e.cumulativeProbability(u) - e.cumulativeProbability(l);
			} else if (hasUB) {
				return e.cumulativeProbability(u);
			} else if (hasLB) {
				return 1 - e.cumulativeProbability(l);
			} else {
				return 1.0;
			}
		case GAMMA:
			GammaDistribution g = new GammaDistribution(lb, ub);
			if (hasLB && hasUB) {
				return g.cumulativeProbability(u) - g.cumulativeProbability(l);
			} else if (hasUB) {
				return g.cumulativeProbability(u);
			} else if (hasLB) {
				return 1 - g.cumulativeProbability(l);
			} else {
				return 1.0;
			}
		case BETA:
			BetaDistribution b1 = new BetaDistribution(lb, ub);
			if (hasLB && hasUB) {
				return b1.cumulativeProbability(u) - b1.cumulativeProbability(l);
			} else if (hasUB) {
				return b1.cumulativeProbability(u);
			} else if (hasLB) {
				return 1 - b1.cumulativeProbability(l);
			} else {
				return 1.0;
			}
		case LAPLACE:
			LaplaceDistribution la = new LaplaceDistribution(lb, ub);
			if (hasLB && hasUB) {
				return la.cumulativeProbability(u) - la.cumulativeProbability(l);
			} else if (hasUB) {
				return la.cumulativeProbability(u);
			} else if (hasLB) {
				return 1 - la.cumulativeProbability(l);
			} else {
				return 1.0;
			}
		case GEOMETRIC:
			GeometricDistribution ge = new GeometricDistribution(lb);
			if (hasLB && hasUB) {
				return ge.cumulativeProbability((int) Math.floor(u)) - ge.cumulativeProbability((int) Math.floor(l));
			} else if (hasUB) {
				return ge.cumulativeProbability((int) Math.floor(u));
			} else if (hasLB) {
				return 1 - ge.cumulativeProbability((int) Math.floor(l));
			} else {
				return 1.0;
			}
		case POISSON:
			PoissonDistribution po = new PoissonDistribution(lb);
			if (hasLB && hasUB) {
				return po.cumulativeProbability((int) Math.floor(u)) - po.cumulativeProbability((int) Math.floor(l));
			} else if (hasUB) {
				return po.cumulativeProbability((int) Math.floor(u));
			} else if (hasLB) {
				return 1 - po.cumulativeProbability((int) Math.floor(l));
			} else {
				return 1.0;
			}
		case T:
			TDistribution t = new TDistribution(lb);
			if (hasLB && hasUB) {
				return t.cumulativeProbability(u) - t.cumulativeProbability(l);
			} else if (hasUB) {
				return t.cumulativeProbability(u);
			} else if (hasLB) {
				return 1 - t.cumulativeProbability(l);
			} else {
				return 1.0;
			}
		}
		assert (false);
		return 0.0;
	}

	public double generateSample() {
		switch (type) {
		case UNIFORM:
			if (lb == ub) {
				return lb;
			}
			UniformRealDistribution u1 = new UniformRealDistribution(lb, ub);
			return u1.sample();
		case UINT:
			if (lb == ub) {
				return lb;
			}
			u1 = new UniformRealDistribution(lb, ub + 0.99);
			return Math.floor(u1.sample());
		case GAUSSIAN:
			NormalDistribution n = new NormalDistribution(lb, ub);
			return n.sample();
		case BINOMIAL:
			BinomialDistribution binomial = new BinomialDistribution((int) lb, ub);
			return binomial.sample();
		case EXPONENTIAL:
			ExponentialDistribution ex = new ExponentialDistribution(lb);
			return ex.sample();
		case GAMMA:
			GammaDistribution ga = new GammaDistribution(lb, ub);
			return ga.sample();
		case BETA:
			BetaDistribution be = new BetaDistribution(lb, ub);
			return be.sample();
		case LAPLACE:
			LaplaceDistribution la = new LaplaceDistribution(lb, ub);
			return la.sample();
		case GEOMETRIC:
			GeometricDistribution ge = new GeometricDistribution(lb);
			return ge.sample();
		case POISSON:
			PoissonDistribution po = new PoissonDistribution(lb);
			return po.sample();
		case T:
			TDistribution t = new TDistribution(lb);
			return t.sample();
		}
		assert (false);
		return 0.0;
	}

	public double generateSampleInRange(boolean hasLB, double l, boolean hasUB, double u) {
		double d=0, sgn = 1.0;
		switch (type) {
		case UNIFORM:
			if (!hasLB || lb > l) {
				l = lb;
			}
			if (!hasUB || ub < u) {
				u = ub;
			}
			assert (l <= u);
			if (l == u) {
				return l;
			}
			UniformRealDistribution u1 = new UniformRealDistribution(l, u);
			return u1.sample();
		case UINT:
			l = Math.ceil(l);
			u = Math.floor(u);
			if (!hasLB || lb > l) {
				l = lb;
			}
			if (!hasUB || ub < u) {
				u = ub;
			}
			assert (l <= u);

			if (lb == ub) {
				return lb;
			}
			u1 = new UniformRealDistribution(l, u + 0.99);
			return Math.floor(u1.sample());
		case GAUSSIAN:
			if (hasLB && hasUB) {
				assert (l <= u);
				if (l == u) {
					return l;
				}
				do {
					NormalDistribution n = new NormalDistribution(lb, ub);
					d = n.sample();
				} while (d < l || d > u);
			} else if (hasLB) {
				l -= lb;
				do {
					NormalDistribution n = new NormalDistribution(lb, ub);
					d = n.sample();
				} while (d < l);
			} else if (hasUB) {
				u -= lb;
				do {
					NormalDistribution n = new NormalDistribution(lb, ub);
					d = n.sample();
				} while (d > u);
			}else {
				NormalDistribution n = new NormalDistribution(0, ub);
				d = n.sample();
			}
			return d;
		case BINOMIAL:
			if (hasLB && hasUB) {
				do {
					BinomialDistribution n = new BinomialDistribution((int) lb, ub);
					d = n.sample();
				} while (d > u || d < l);
			} else if (hasLB) {
				do {
					BinomialDistribution n = new BinomialDistribution((int) lb, ub);
					d = n.sample();
				} while (d < l);
			} else if (hasUB) {
				do {
					BinomialDistribution n = new BinomialDistribution((int) lb, ub);
					d = n.sample();
				} while (d > u);
			}else {
				BinomialDistribution n = new BinomialDistribution((int) lb, ub);
				d = n.sample();
			}
			return d;
		case EXPONENTIAL:
			if (hasLB && hasUB) {
				do {
					ExponentialDistribution n = new ExponentialDistribution(lb);
					d = n.sample();
				} while (d > u || d < l);
			} else if (hasLB) {
				do {
					ExponentialDistribution n = new ExponentialDistribution(lb);
					d = n.sample();
				} while (d < l);
			} else if (hasUB) {
				do {
					ExponentialDistribution n = new ExponentialDistribution(lb);
					d = n.sample();
				} while (d > u);
			}else {
				ExponentialDistribution n = new ExponentialDistribution(lb);
				d = n.sample();
			}
			return d;
		case GAMMA:
			if (hasLB && hasUB) {
				do {
					GammaDistribution n = new GammaDistribution(lb,ub);
					d = n.sample();
				} while (d > u || d < l);
			} else if (hasLB) {
				do {
					GammaDistribution n = new GammaDistribution(lb,ub);
					d = n.sample();
				} while (d < l);
			} else if (hasUB) {
				do {
					GammaDistribution n = new GammaDistribution(lb,ub);
					d = n.sample();
				} while (d > u);
			}else {
				GammaDistribution n = new GammaDistribution(lb,ub);
				d = n.sample();
			}
			return d;
		case BETA:
			if (hasLB && hasUB) {
				do {
					BetaDistribution n = new BetaDistribution(lb,ub);
					d = n.sample();
				} while (d > u || d < l);
			} else if (hasLB) {
				do {
					BetaDistribution n = new BetaDistribution(lb,ub);
					d = n.sample();
				} while (d < l);
			} else if (hasUB) {
				do {
					BetaDistribution n = new BetaDistribution(lb,ub);
					d = n.sample();
				} while (d > u);
			}else {
				BetaDistribution n = new BetaDistribution(lb,ub);
				d = n.sample();
			}
			return d;
		case LAPLACE:
			if (hasLB && hasUB) {
				do {
					LaplaceDistribution n = new LaplaceDistribution(lb,ub);
					d = n.sample();
				} while (d > u || d < l);
			} else if (hasLB) {
				do {
					LaplaceDistribution n = new LaplaceDistribution(lb,ub);
					d = n.sample();
				} while (d < l);
			} else if (hasUB) {
				do {
					LaplaceDistribution n = new LaplaceDistribution(lb,ub);
					d = n.sample();
				} while (d > u);
			}else {
				LaplaceDistribution n = new LaplaceDistribution(lb,ub);
				d = n.sample();
			}
			return d;
		case GEOMETRIC:
			if (hasLB && hasUB) {
				do {
					GeometricDistribution n = new GeometricDistribution(lb);
					d = n.sample();
				} while (d > u || d < l);
			} else if (hasLB) {
				do {
					GeometricDistribution n = new GeometricDistribution(lb);
					d = n.sample();
				} while (d < l);
			} else if (hasUB) {
				do {
					GeometricDistribution n = new GeometricDistribution(lb);
					d = n.sample();
				} while (d > u);
			}else {
				GeometricDistribution n = new GeometricDistribution(lb);
				d = n.sample();
			}
			return d;
		case POISSON:
			if (hasLB && hasUB) {
				do {
					PoissonDistribution n = new PoissonDistribution(lb);
					d = n.sample();
				} while (d > u || d < l);
			} else if (hasLB) {
				do {
					PoissonDistribution n = new PoissonDistribution(lb);
					d = n.sample();
				} while (d < l);
			} else if (hasUB) {
				do {
					PoissonDistribution n = new PoissonDistribution(lb);
					d = n.sample();
				} while (d > u);
			}else {
				PoissonDistribution n = new PoissonDistribution(lb);
				d = n.sample();
			}
			return d;
		case T:
			if (hasLB && hasUB) {
				do {
					TDistribution n = new TDistribution(lb);
					d = n.sample();
				} while (d > u || d < l);
			} else if (hasLB) {
				do {
					TDistribution n = new TDistribution(lb);
					d = n.sample();
				} while (d < l);
			} else if (hasUB) {
				do {
					TDistribution n = new TDistribution(lb);
					d = n.sample();
				} while (d > u);
			}else {
				TDistribution n = new TDistribution(lb);
				d = n.sample();
			}
			return d;
		}
		assert (false);
		return 0.0;
	}

}
