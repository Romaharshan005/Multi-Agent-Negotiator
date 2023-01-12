import java.util.HashMap;
import java.util.List;
import java.util.Map;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;

/**
 * This is your negotiation party.
 */
public class Group1_Agent extends AbstractNegotiationParty {

	private double maximumUtility = 1.0;
	double time = 0.2D;
	Bid sampleBid;
	Bid lastReceivedBid;
	private int numberOfIssues;
	Map<Integer, double[]> eachIssueAndItsValuesFrequencyArrayMap;
	Map<Integer, double[]> opponentEachIssueAndItsValuesUtilityMap;
	double[] opponentSumOfSquaredErrosOfEachIssueMatrix;
	Map<Integer, Double> opponentEachIssueAndItsUtilityMap;

	@Override
	public void init(NegotiationInfo info) {

		super.init(info);

		System.out.println("Discount Factor is " + getUtilitySpace().getDiscountFactor());
		System.out.println("Reservation Value is " + getUtilitySpace().getReservationValueUndiscounted());
		initOpponentVars();

		// if you need to initialize some variables, please initialize them
		// below

	}

	private void initOpponentVars() {
		initOpponentModelingVars();
		createFrequencyArrays();
	}

	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		double lastReceivedBidUtility = getUtility(lastReceivedBid);
		if (getTimeLine().getTime() < this.time) {
			double minimumUtil1 = 0.9;
			if (lastReceivedBidUtility > 0.75)
					return new Accept(getPartyId(), lastReceivedBid);
				else {
					Bid newBid = generateRandomBidT(minimumUtil1);
					return new Offer(getPartyId(), newBid);
				}
			// return (lastReceivedBidUtility >= 0.75) ? new Accept(getPartyId(), lastReceivedBid)
			// 		: new Offer(getPartyId(), generateRandomBidT(minimumUtil1));
		}
		if (lastReceivedBid != null) {
			if (timeline.getTime() >= 0.99) {
				return new Accept(getPartyId(), lastReceivedBid);
			}
			if (timeline.getTime() >= 0.90) {
				if (lastReceivedBidUtility >= 0.65)
					return new Accept(getPartyId(), lastReceivedBid);
			} else {
				if (lastReceivedBidUtility > 0.85)
					return new Accept(getPartyId(), lastReceivedBid);
				else {
					Bid newBid = generateRandomBidT(mininumUtil());
					return new Offer(getPartyId(), newBid);
				}
			}
		}
		System.out.print("jncjsncjs");  
		return new Offer(getPartyId(), generateRandomBidT(mininumUtil()));
	}

	private Bid generateRandomBidT(double t) {
		Bid bid = null;
		bid = generateRandomBid();
		double util;
		double opputil = 0;
		util = getUtility(bid);
		while (util <= t) {

			bid = generateRandomBid();
			util = getUtility(bid);

		}
		Bid nashbid = bid;
		double nashutilpro = 0;
		int iii = 0;
		System.out.print("jncjsncjs"); 
		while (iii < 30000) {
			iii++;
			while (util <= t) {

				bid = generateRandomBid();
				util = getUtility(bid);

			}
			HashMap<Integer, Value> h = bid.getValues();
			opputil = 0;
			for (int i = 0; i < numberOfIssues; ++i) {
				ValueDiscrete valueDiscrete = (ValueDiscrete) (bid.getValue(i+1));
			int valueIndex = getIndexOfValueInIssue(bid, i, valueDiscrete.getValue());
				opputil = opputil
						+ opponentEachIssueAndItsUtilityMap.get(i) * opponentEachIssueAndItsValuesUtilityMap.get(i)[valueIndex];
			}
			if (util * opputil > nashutilpro) {
				nashbid = bid;
				nashutilpro = util * opputil;
			}
		}
		// Map<Integer, double[]> eachIssueAndItsValuesFrequencyArrayMap;
		// Map<Integer, double[]> opponentEachIssueAndItsValuesUtilityMap;
		// double[] opponentSumOfSquaredErrosOfEachIssueMatrix;
		// Map<Integer, Double> opponentEachIssueAndItsUtilityMap;
		return nashbid;
	}
	public double mininumUtil() {
		double TimeRatio = 1 - timeline.getCurrentTime() / timeline.getTotalTime();
		double minimumUtility = 0;
		double currentTime = timeline.getCurrentTime();
		double totalTime = timeline.getTotalTime();

		if (TimeRatio > 0.5) {
			minimumUtility = 0.9;
		} else if (TimeRatio > 0.1) {
			minimumUtility = 0.7;
		} else {
			minimumUtility = 0.5;
		}
		double alpha = Math.pow(1 - Math.min(currentTime, totalTime) / totalTime, 1 / Math.E);
		double concedingVariable = minimumUtility + (maximumUtility - minimumUtility) * alpha;

		return concedingVariable;
	}

	@Override
	public void receiveMessage(AgentID sender, Action action) {
		try {
			super.receiveMessage(sender, action);
			if (action instanceof Offer) {
				lastReceivedBid = ((Offer) action).getBid();
				updateFrequencyArrays();
				updateIssueValueUtilities();
				fillOpponentMatriceOfSumOfSquaresOfEachIssue();
				updateIsseUtilities();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getDescription() {
		return "example party group N";
	}

	private void initOpponentModelingVars() {
		this.eachIssueAndItsValuesFrequencyArrayMap = new HashMap<Integer, double[]>();
		this.opponentEachIssueAndItsValuesUtilityMap = new HashMap<Integer, double[]>();
		this.opponentSumOfSquaredErrosOfEachIssueMatrix = new double[numberOfIssues];
		this.opponentEachIssueAndItsUtilityMap = new HashMap<Integer, Double>();
	}
	private int getIndexOfValueInIssue(Bid bid, int issueIndex, String value) {
		IssueDiscrete is = (IssueDiscrete) bid.getIssues().get(issueIndex);
		return is.getValueIndex(value);
	}
	private void createFrequencyArrays() {
		for (int i = 0; i < numberOfIssues; i++) {
			IssueDiscrete issueDiscrete = (IssueDiscrete) sampleBid.getIssues().get(i);
			int valueSize = issueDiscrete.getValues().size();
			eachIssueAndItsValuesFrequencyArrayMap.put(i, new double[valueSize]);
		}
	}

	private void updateFrequencyArrays() {
		for (int i = 0; i < numberOfIssues; i++) {
			ValueDiscrete valueDiscrete = (ValueDiscrete) (lastReceivedBid.getValue(i + 1));
			int valueIndex = getIndexOfValueInIssue(lastReceivedBid, i, valueDiscrete.getValue());
			eachIssueAndItsValuesFrequencyArrayMap.get(i)[valueIndex] += 1;
		}
	}

	private void updateIssueValueUtilities() {
		for (int i = 0; i < numberOfIssues; i++) {
			double[] frequencyArr = eachIssueAndItsValuesFrequencyArrayMap.get(i);
			double[] utilityArr = new double[frequencyArr.length];
			for (int j = 0; j < frequencyArr.length; j++) {
				double freqSumOfEachIssue = getSumOfRowInOneDimensionalMatrix(frequencyArr);
				utilityArr[j] = ((double) frequencyArr[j] / freqSumOfEachIssue);
			}
			opponentEachIssueAndItsValuesUtilityMap.put(i, utilityArr);
		}
	}

	private void fillOpponentMatriceOfSumOfSquaresOfEachIssue() {
		for (int i = 0; i < numberOfIssues; i++) {
			double[] frequencyArr = eachIssueAndItsValuesFrequencyArrayMap.get(i);
			double averageOfMatrix = getAverageOfOneDimensionalMatrix(frequencyArr);
			double sumOfSquaresForMatrix = getSumOfSquaredErrorsForMatrix(frequencyArr, averageOfMatrix);
			opponentSumOfSquaredErrosOfEachIssueMatrix[i] = sumOfSquaresForMatrix / frequencyArr.length;
		}
	}

	private double getAverageOfOneDimensionalMatrix(double[] matrix) {
		return getSumOfRowInOneDimensionalMatrix(matrix) / (matrix.length);
	}

	private double getSumOfRowInOneDimensionalMatrix(double[] matrix) {
		double rowSum = 0;
		for (int i = 0; i < matrix.length; i++) {
			rowSum += matrix[i];
		}
		return rowSum;
	}

	private double getSumOfSquaredErrorsForMatrix(double[] frequencyArr, double averageOfMatrix) {
		double totalOfsumOfSquares = 0;
		for (int j = 0; j < frequencyArr.length; j++) {
			totalOfsumOfSquares += Math.pow((frequencyArr[j] - averageOfMatrix), 2);
		}
		return Math.sqrt(totalOfsumOfSquares);
	}

	private void updateIsseUtilities() {
		double totalOfsumOfSquares = 0;
		for (int i = 0; i < numberOfIssues; i++) {
			totalOfsumOfSquares += opponentSumOfSquaredErrosOfEachIssueMatrix[i];
		}
		for (int i = 0; i < numberOfIssues; i++) {
			opponentEachIssueAndItsUtilityMap.put(i,
					opponentSumOfSquaredErrosOfEachIssueMatrix[i] / totalOfsumOfSquares);
		}
	}

}