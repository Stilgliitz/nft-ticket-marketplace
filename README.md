# NFT Ticket Marketplace (Android)

An Android ticketing app built to tackle ticket fraud. Every ticket bought in the app is minted as an ERC-721 NFT on the Ethereum Sepolia testnet, so ownership can be checked against the blockchain as well as the app's own database.

This was my BSc Software Engineering dissertation project (University of Northampton, 2025).

## What it does

- **Sign up and log in** with Firebase Authentication, with email verification required before the app can be used
- **Browse events** loaded live from Firestore
- **Buy a ticket** with Stripe (test mode). The payment is recorded as `pending` and only completes once the ticket has been minted
- **Mint the ticket as an NFT** through web3j, storing the transaction hash for verification
- **Get a QR code** for each purchased ticket
- **My Tickets** shows only the tickets owned by the signed-in user
- **Verify, resell and transfer** tickets, with ownership checks before a ticket can be listed
- **Link a wallet** address to receive minted tickets

## Tech stack

| Area | Tools |
|---|---|
| App | Java, Android SDK, XML layouts, RecyclerView |
| Backend | Firebase Authentication, Cloud Firestore, Firebase Storage |
| Payments | Stripe Android SDK (test mode) |
| Blockchain | Solidity, OpenZeppelin ERC-721, Hardhat, web3j, Ethereum Sepolia |
| QR codes | ZXing, Google ML Kit |

## The smart contract

`contracts/ticketcontracttestnet.sol` is an `ERC721URIStorage` contract with a capped supply per event. Only the contract owner can mint, and each mint emits a `TicketMinted` event.

## What I learned

- **Testnets are unreliable.** Polygon's testnets were deprecated or had dry faucets, so I moved to Sepolia. Congestion there caused transactions to fail silently, so I added long custom timeouts, fixed gas pricing and clear error messages for the user.
- **Payments and minting have to agree.** Holding the payment as `pending` until the mint succeeds stops a user paying for a ticket that never arrives.
- **Small mismatches cost hours.** Most of my Firestore bugs were collection or field names that did not match.

## Known limitations

This is a prototype, built with test data only.

- The app signs mint transactions itself using a testnet key. A production version would move minting to a server so no key ships inside the app.
- Resale is verified through Firestore ownership records. On-chain transfer through the contract is the next step.
- Firestore rules were left open during development and need locking down.

## Running it

1. Open the project in Android Studio.
2. Create your own Firebase project and add its `google-services.json` to `app/`.
3. Copy `app/src/main/java/com/example/ticketapp/Secrets.java.example` to `Secrets.java` and fill in a Sepolia RPC URL and a **testnet-only** wallet key.
4. To redeploy the contract, copy `.env.example` to `.env`, then run `npx hardhat run scripts/deploy.js --network sepolia`.

`Secrets.java`, `.env`, `google-services.json` and Firebase admin key files are ignored by Git and must never be committed.
