import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private HashSet<Integer> setFollowees = new HashSet<>();
    private HashSet<Transaction> pendingTransactions = new HashSet<Transaction>();
    private double pgraph;
    private double p_malicious;
    private double p_txDistribution;
    private int numRounds;
    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.pgraph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.numRounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
        for (int i = 0; i < followees.length; i++) {
            if (followees[i] == true){
                setFollowees.add(i);
            }
        }
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        for(Transaction tx : pendingTransactions){
            this.pendingTransactions.add(tx);
        }
    }

    public Set<Transaction> sendToFollowers() {
        return pendingTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        for(Candidate candidate : candidates){
            if (!pendingTransactions.contains(candidate.tx)){
                pendingTransactions.add(candidate.tx);
            }
        }
    }
}
