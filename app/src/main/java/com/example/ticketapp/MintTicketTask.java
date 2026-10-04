package com.example.ticketapp;

import android.os.AsyncTask;
import android.util.Log;

import com.example.ticketapp.contracts.TicketContractTestNet;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

public class MintTicketTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = "MintTicketLog";
    private static final String SEPOLIA_INFURA_URL = Secrets.SEPOLIA_RPC_URL;  //Sepolia URL, kept out of version control
    private static final String PRIVATE_KEY = Secrets.MINTER_PRIVATE_KEY;  //kept out of version control
    private static final String CONTRACT_ADDRESS = "0x6Bd88A188BFdcDa3691052C56Dd516A06738Ee31";  // Updated contract address
    public static final long CHAIN_ID = 11155111;  //Sepolia Testnet Chain ID

    private final String walletAddressToMintTo;
    private final String metadataUri;
    private final MintCallback callback;

    public interface MintCallback {
        void onMintResult(String result);
    }

    public MintTicketTask(String walletAddressToMintTo, String metadataUri, MintCallback callback) {
        this.walletAddressToMintTo = walletAddressToMintTo;
        this.metadataUri = metadataUri;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(Void... voids) {
        return mintTicket(walletAddressToMintTo, metadataUri);
    }

    private String mintTicket(String walletAddressToMintTo, String metadataUri) {
        try {
            //Custom timeout added
            okhttp3.OkHttpClient okHttpClient = new okhttp3.OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();

            //Use HttpService with timeout
            HttpService httpService = new HttpService(SEPOLIA_INFURA_URL, okHttpClient, false);
            Web3j web3 = Web3j.build(httpService);

            //Check Sepolia Chain ID
            EthChainId chainIdResponse = web3.ethChainId().send();
            if (chainIdResponse == null || chainIdResponse.getChainId().longValue() != CHAIN_ID) {
                return "Error: Incorrect chain ID";
            }

            Credentials credentials = Credentials.create(PRIVATE_KEY);
            RawTransactionManager txManager = new RawTransactionManager(web3, credentials, CHAIN_ID);

            //Load contract
            TicketContractTestNet contract = TicketContractTestNet.load(
                    CONTRACT_ADDRESS,
                    web3,
                    txManager,
                    new StaticGasProvider(
                            BigInteger.valueOf(20_000_000_000L), //20 Gwei
                            BigInteger.valueOf(800_000)          //800,000 gas
                    )
            );

            //Attempt mint, allow long wait
            PollingTransactionReceiptProcessor receiptProcessor = new PollingTransactionReceiptProcessor(web3, 15_000, 30);
            TransactionReceipt txReceipt = contract.mintTicket(walletAddressToMintTo, metadataUri).sendAsync().get(30, TimeUnit.MINUTES);

            return "Ticket Minted! TxHash: " + txReceipt.getTransactionHash();

        } catch (java.net.SocketTimeoutException timeoutEx) {
            Log.e(TAG, "Socket timeout: " + timeoutEx.getMessage(), timeoutEx);
            return "Network Timeout – Sepolia may be congested. Please try again later.";
        } catch (Exception e) {
            Log.e(TAG, "Minting failed: " + e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }


    @Override
    protected void onPostExecute(String result) {
        if (callback != null) {
            callback.onMintResult(result);
        }
    }
}


