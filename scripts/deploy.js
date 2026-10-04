const { ethers } = require("hardhat");
const fs = require("fs");

async function main() {
  const [deployer] = await ethers.getSigners();
  console.log("Deploying contracts with the account:", deployer.address);

  const abi = JSON.parse(fs.readFileSync("./contracts/ticketcontracttestnet.abi", "utf8"));
  const bytecode = fs.readFileSync("./contracts/ticketcontracttestnet.bin", "utf8");

  const contractFactory = new ethers.ContractFactory(abi, bytecode, deployer);

  const maxTicketsPerEvent = 256;
  const contract = await contractFactory.deploy(maxTicketsPerEvent);

  console.log("Contract deployed to address:", contract.address);
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
