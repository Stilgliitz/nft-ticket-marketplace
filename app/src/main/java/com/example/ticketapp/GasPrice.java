package com.example.ticketapp;

import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;

public class GasPrice implements ContractGasProvider {

    //gas limit to ensure no gas errors on mint
    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(1_200_000); //1.2 million

    //gas price for faster transaction confirmation
    private static final BigInteger MAX_PRIORITY_FEE = BigInteger.valueOf(50_000_000_000L); //50 Gwei
    private static final BigInteger MAX_FEE = BigInteger.valueOf(100_000_000_000L); //100 Gwei max

    @Override
    public BigInteger getGasPrice(String contractFunc) {
        return MAX_FEE;
    }

    @Override
    public BigInteger getGasPrice() {
        return MAX_FEE;
    }

    @Override
    public BigInteger getGasLimit(String contractFunc) {
        return GAS_LIMIT;
    }

    @Override
    public BigInteger getGasLimit() {
        return GAS_LIMIT;
    }
}
