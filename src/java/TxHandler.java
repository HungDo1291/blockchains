import java.util.ArrayList;
import java.util.Arrays;
public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */

    private UTXOPool utxoPool;

    public TxHandler(UTXOPool uPool) {
        this.utxoPool = new UTXOPool(uPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        //since TXHandler can check many transactions independently. isValidTx will check each tx without altering TxHandler.utxoPool
        //that is why for each tx check, we need to create a copy of TxHandler.utxoPool
        UTXOPool temporary_utxoPool = new UTXOPool(utxoPool);
        ArrayList<Transaction.Input> inputs= tx.getInputs();
        ArrayList<Transaction.Output> outputs= tx.getOutputs();

        Transaction.Input input;
        Transaction.Output previous_output;
        Transaction.Output output;
        UTXO utxo;
        double sum_input_values = 0;
        double sum_output_values = 0;
        //(1) all outputs claimed by {@code tx} are in the current UTXO pool,
        for (int i=0; i < tx.numInputs(); i++){
            input = inputs.get(i);
            utxo = new UTXO(input.prevTxHash, input.outputIndex);
            //(1) all outputs claimed by {@code tx} are in the current UTXO pool,
            if(temporary_utxoPool.contains(utxo) ){
                previous_output = temporary_utxoPool.getTxOutput(utxo);

                //(2) the signatures on each input of {@code tx} are valid,
                if (Crypto.verifySignature(previous_output.address, tx.getRawDataToSign(i), input.signature)){

                }else{
                    return false;
                }

                //(3) no UTXO is claimed multiple times by {@code tx},
                temporary_utxoPool.removeUTXO(utxo);

                sum_input_values += previous_output.value;
            }else{
                return false;
            }

        }
        for (int i=0; i < tx.numOutputs(); i++){
            output = outputs.get(i);
            //(4) all of {@code tx}s output values are non-negative, and
            if(output.value < 0){
                return false;
            }
            sum_output_values += output.value;
        }
        // (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
        // values; and false otherwise.
        if (sum_input_values < sum_output_values){
            return false;
        }else{
            return true;
        }

    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> validTxsList = new ArrayList<>();
        Transaction[] validTxsArray = new Transaction[]{};
        Transaction currentTxs;
        //variables for updating the utxo pool
        UTXO utxo;
        ArrayList<Transaction.Input> inputs;
        Transaction.Input input;
        for (int i = 0; i < possibleTxs.length; i++) {
            currentTxs = possibleTxs[i];
            if (isValidTx(currentTxs)){
                validTxsList.add(currentTxs);
                //update pool
                inputs = currentTxs.getInputs();
                for (int j=0; j < currentTxs.numInputs() ; j++) {
                    input = inputs.get(j);
                    utxo = new UTXO(input.prevTxHash, input.outputIndex);
                    utxoPool.removeUTXO(utxo);
                }
                for (int j =0; j < currentTxs.numOutputs(); j++) {
                    utxo = new UTXO(currentTxs.getHash(), j);
                    utxoPool.addUTXO(utxo, currentTxs.getOutput(j));
                }
            }
        }
        validTxsArray = validTxsList.toArray(validTxsArray);
        return validTxsArray;
    }
}
