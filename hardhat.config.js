require("@nomiclabs/hardhat-ethers");
require("@nomiclabs/hardhat-waffle");
require("dotenv").config();

module.exports = {
  solidity: "0.8.29",
  networks: {
    sepolia: {
      url: process.env.QUICKNODE_URL,
      accounts: [`0x${process.env.PRIVATE_KEY}`],
    },
  },
};
