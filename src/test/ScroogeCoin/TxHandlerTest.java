package ScroogeCoin;

import ScroogeCoin.Transaction;
import ScroogeCoin.TxHandler;
import ScroogeCoin.UTXO;
import ScroogeCoin.UTXOPool;

import java.math.BigInteger;
import java.security.*;

import static org.junit.Assert.assertTrue;

public class TxHandlerTest {
    private Tx tx;
    private Tx tx2;
    private TxHandler txHandler;
    private UTXOPool utxoPool = new UTXOPool();
    @org.junit.Before
    public void setUp() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        //
        // Generate key pairs, for Scrooge & Alice
        //
        KeyPair pk_scrooge = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        KeyPair pk_alice   = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        //
        // Set up the root transaction:
        //
        // Generating a root transaction tx out of thin air, so that Scrooge owns a coin of value 10
        // By thin air I mean that this tx will not be validated, I just need it to get
        // a proper ScroogeCoin.Transaction.Output which I then can put in the ScroogeCoin.UTXOPool, which will be passed
        // to the TXHandler.
        //
        tx = new Tx();
        tx.addOutput(10, pk_scrooge.getPublic());

        // This value has no meaning, but tx.getRawDataToSign(0) will access it in prevTxHash;
        byte[] initialHash = BigInteger.valueOf(0).toByteArray();
        tx.addInput(initialHash, 0);

        tx.signTx(pk_scrooge.getPrivate(), 0);

        //
        // Set up the ScroogeCoin.UTXOPool
        //
        // The transaction output of the root transaction is the initial unspent output.

        UTXO utxo = new UTXO(tx.getHash(),0);
        utxoPool.addUTXO(utxo, tx.getOutput(0));

        //
        // Set up a test ScroogeCoin.Transaction
        //
        tx2 = new Tx();

        // the ScroogeCoin.Transaction.Output of tx at position 0 has a value of 10
        tx2.addInput(tx.getHash(), 0);

        // I split the coin of value 10 into 3 coins and send all of them for simplicity to
        // the same address (Alice)
        tx2.addOutput(5, pk_alice.getPublic());
        tx2.addOutput(3, pk_alice.getPublic());
        tx2.addOutput(2, pk_alice.getPublic());
        // Note that in the real world fixed-point types would be used for the values, not doubles.
        // Doubles exhibit floating-point rounding errors. This type should be for example BigInteger
        // and denote the smallest coin fractions (Satoshi in Bitcoin).

        // There is only one (at position 0) ScroogeCoin.Transaction.Input in tx2
        // and it contains the coin from Scrooge, therefore I have to sign with the private key from Scrooge
        tx2.signTx(pk_scrooge.getPrivate(), 0);

        /*
         * Start the test
         */
        // Remember that the utxoPool contains a single unspent ScroogeCoin.Transaction.Output which is
        // the coin from Scrooge.
        txHandler = new TxHandler(utxoPool);


    }


    @org.junit.Test
    public void isValidTxTest() {
        boolean bool = txHandler.isValidTx(tx2);
        System.out.println("txHandler.isValidTx(tx2) returns: " + bool);
        assertTrue(bool);
    }

    @org.junit.Test
    public void handleTxs() {
        System.out.println("txHandler.handleTxs(new ScroogeCoin.Transaction[]{tx2}) returns: " +
                txHandler.handleTxs(new Transaction[]{tx2}).length + " transaction(s)");
    }
    public static class Tx extends Transaction {
        public void signTx(PrivateKey sk, int input) throws SignatureException {
            Signature sig = null;
            try {
                sig = Signature.getInstance("SHA256withRSA");
                sig.initSign(sk);
                sig.update(this.getRawDataToSign(input));
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
            this.addSignature(sig.sign(),input);
            // Note that this method is incorrectly named, and should not in fact override the Java
            // object finalize garbage collection related method.
            this.finalize();
        }
    }
}
